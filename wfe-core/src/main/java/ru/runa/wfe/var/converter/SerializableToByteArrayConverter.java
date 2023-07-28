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
import ru.runa.wfe.var.Variable;
import ru.runa.wfe.var.file.FileVariable;
import ru.runa.wfe.var.file.FileVariableImpl;

public class SerializableToByteArrayConverter implements Converter {
    private static final long serialVersionUID = 1L;

    @Override
    public boolean supports(Object value) {
        return value instanceof Serializable;
    }

    @Override
    public Object convert(ExecutionContext executionContext, Variable variable, Object o) {
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

    @SuppressWarnings("resource")
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
        protected ObjectStreamClass readClassDescriptor() throws IOException, ClassNotFoundException {
            final ObjectStreamClass osc = super.readClassDescriptor();
            final String className = BackCompatibilityClassNames.getClassName(osc.getName());
            if (FileVariable.class.getName().equals(className)) {
                // rm787 v4.4.0: special case after rename IFileVariable -> FileVariable, FileVariable -> FileVariableImpl
                return ObjectStreamClass.lookup(FileVariableImpl.class);
            }
            return osc;
        }

        @Override
        protected Class<?> resolveClass(ObjectStreamClass desc) throws IOException, ClassNotFoundException {
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
