/*
 * JBoss, Home of Professional Open Source
 * Copyright 2005, JBoss Inc., and individual contributors as indicated
 * by the @authors tag. See the copyright.txt in the distribution for a
 * full listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
package ru.runa.wfe.var.converter;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.ObjectStreamClass;
import java.io.Serializable;

import ru.runa.wfe.InternalApplicationException;
import ru.runa.wfe.commons.BackCompatibilityClassNames;
import ru.runa.wfe.commons.ClassLoaderUtil;
import ru.runa.wfe.execution.ExecutionContext;
import ru.runa.wfe.var.Converter;
import ru.runa.wfe.var.CurrentVariable;

public class SerializableToByteArrayConverter implements Converter {
    private static final long serialVersionUID = 1L;

    @Override
    public boolean supports(Object value) {
        return value instanceof Serializable;
    }

    @Override
    public Object convert(ExecutionContext executionContext, CurrentVariable<?> variable, Object o) {
        try {
            ByteArrayOutputStream memoryStream = new ByteArrayOutputStream();
            ObjectOutputStream objectStream = new ObjectOutputStream(memoryStream);
            objectStream.writeObject(o);
            objectStream.flush();
            return memoryStream.toByteArray();
        } catch (IOException e) {
            throw new InternalApplicationException("couldn't serialize '" + o + "'", e);
        }
    }

    @Override
    public Object revert(Object o) {
        try {
            byte[] bytes = (byte[]) o;
            InputStream memoryStream = new ByteArrayInputStream(bytes);
            return new BackCompatibleObjectInputStream(memoryStream).readObject();
        } catch (IOException ex) {
            throw new InternalApplicationException("failed to read object", ex);
        } catch (ClassNotFoundException ex) {
            throw new InternalApplicationException("serialized object class not found", ex);
        }
    }

    private static class BackCompatibleObjectInputStream extends ObjectInputStream {

        BackCompatibleObjectInputStream(InputStream in) throws IOException {
            super(in);
        }

        @Override
        protected Class<?> resolveClass(ObjectStreamClass desc) throws IOException {
            try {
                return super.resolveClass(desc);
            } catch (ClassNotFoundException ex) {
                String className = desc.getName();
                if (className.startsWith("[L")) {
                    // arrays
                    String componentClassName = className.substring(2, className.length() - 1);
                    componentClassName = BackCompatibilityClassNames.getClassName(componentClassName);
                    className = "[L" + componentClassName + ";";
                }
                int childClassIndex = className.indexOf("$");
                if (childClassIndex > 0) {
                    String surroundingClassName = className.substring(0, childClassIndex);
                    String childClassName = className.substring(childClassIndex + 1);
                    surroundingClassName = BackCompatibilityClassNames.getClassName(surroundingClassName);
                    className = surroundingClassName + "$" + childClassName;
                }
                return ClassLoaderUtil.loadClass(className);
            }
        }
    }
}
