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
package ru.runa.wfe.audit;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.Transient;
import ru.runa.wfe.InternalApplicationException;
import ru.runa.wfe.user.Executor;
import ru.runa.wfe.var.Variable;
import ru.runa.wfe.var.VariableDefinition;
import ru.runa.wfe.var.converter.FileVariableToByteArrayConverter;
import ru.runa.wfe.var.converter.SerializableToByteArrayConverter;
import ru.runa.wfe.var.converter.StringToByteArrayConverter;
import ru.runa.wfe.var.file.FileVariable;

/**
 * Variables base logging class.
 *
 * @author Dofs
 */
@Entity
@DiscriminatorValue(value = "0")
public abstract class VariableLog extends ProcessLog {
    private static final long serialVersionUID = 1L;

    public VariableLog() {
    }

    public VariableLog(Variable<?> variable) {
        setVariableName(variable.getName());
    }

    @Transient
    public String getVariableName() {
        return getAttributeNotNull(ATTR_VARIABLE_NAME);
    }

    public void setVariableName(String variableName) {
        addAttribute(ATTR_VARIABLE_NAME, variableName);
    }

    @Transient
    public String getVariableNewValueAttribute() {
        return getAttribute(ATTR_NEW_VALUE);
    }

    protected void setVariableNewValue(Variable<?> variable, Object newValue, VariableDefinition variableDefinition) {
        String newValueString = newValue instanceof Executor ? ((Executor) newValue).getName() : variable.toString(newValue, variableDefinition);
        addAttributeWithTruncation(ATTR_NEW_VALUE, newValueString);
        boolean file = newValue instanceof FileVariable;
        // TODO FileVariableMatcher
        addAttribute(ATTR_IS_FILE_VALUE, String.valueOf(file));
        if (variable.getStorableValue() instanceof byte[]) {
            setBytes((byte[]) variable.getStorableValue());
        }
    }

    @Transient
    public boolean isFileValue() {
        return "true".equals(getAttribute(ATTR_IS_FILE_VALUE));
    }

    @Transient
    public Object getVariableNewValue() {
        byte[] bytes = getBytes();
        if (bytes != null) {
            if (isFileValue()) {
                return new FileVariableToByteArrayConverter().revert(bytes);
            }
            try {
                return new SerializableToByteArrayConverter().revert(bytes);
            } catch (Exception e) {
                return new StringToByteArrayConverter().revert(bytes);
            }
        }
        return getVariableNewValueAttribute();
    }

    @Transient
    public VariableLog getContentCopy() {
        VariableLog copyLog;
        if (this instanceof VariableCreateLog) {
            copyLog = new VariableCreateLog();
        } else if (this instanceof VariableUpdateLog) {
            copyLog = new VariableUpdateLog();
        } else if (this instanceof VariableDeleteLog) {
            copyLog = new VariableDeleteLog();
        } else {
            throw new InternalApplicationException("Unexpected " + this);
        }
        copyLog.setBytes(getBytes());
        copyLog.setContent(getContent());
        return copyLog;
    }
}
