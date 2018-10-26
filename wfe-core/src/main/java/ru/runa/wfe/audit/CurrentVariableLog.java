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
import ru.runa.wfe.audit.presentation.ExecutorNameValue;
import ru.runa.wfe.audit.presentation.FileValue;
import ru.runa.wfe.user.Executor;
import ru.runa.wfe.var.CurrentVariable;
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
public abstract class CurrentVariableLog extends CurrentProcessLog implements VariableLog {
    private static final long serialVersionUID = 1L;

    public CurrentVariableLog() {
    }

    public CurrentVariableLog(CurrentVariable<?> variable) {
        setVariableName(variable.getName());
    }

    public void setVariableName(String variableName) {
        addAttribute(ATTR_VARIABLE_NAME, variableName);
    }

    protected void setVariableNewValue(CurrentVariable<?> variable, Object newValue, VariableDefinition variableDefinition) {
        String newValueString;
        if (newValue instanceof Executor) {
            newValueString = ((Executor) newValue).getName();
            addAttribute(ATTR_IS_EXECUTOR_VALUE, ATTR_VALUE_TRUE);
        } else {
            newValueString = variable.toString(newValue, variableDefinition);
            if (newValue instanceof FileVariable) {
                addAttribute(ATTR_IS_FILE_VALUE, ATTR_VALUE_TRUE);
            }
            if (variable.getStorableValue() instanceof byte[]) {
                setBytes((byte[]) variable.getStorableValue());
            }
        }
        addAttributeWithTruncation(ATTR_NEW_VALUE, newValueString);
    }

    @Override
    @Transient
    public Type getType() {
        return Type.VARIABLE;
    }

    @Override
    @Transient
    public String getVariableName() {
        return getAttributeNotNull(ATTR_VARIABLE_NAME);
    }

    @Override
    @Transient
    public String getVariableNewValueAttribute() {
        return getAttribute(ATTR_NEW_VALUE);
    }

    @Override
    @Transient
    public boolean isFileValue() {
        return ATTR_VALUE_TRUE.equals(getAttribute(ATTR_IS_FILE_VALUE));
    }

    @Override
    @Transient
    public boolean isExecutorValue() {
        return ATTR_VALUE_TRUE.equals(getAttribute(ATTR_IS_EXECUTOR_VALUE));
    }

    @Override
    @Transient
    public Object getVariableNewValue() {
        return CurrentAndArchiveCommons.variableLog_getVariableNewValue(this);
    }

    @Transient
    public Object getVariableNewValueForPattern() {
        return CurrentAndArchiveCommons.variableLog_getVariableNewValueForPattern(this);
    }

    @Transient
    public CurrentVariableLog getContentCopy() {
        CurrentVariableLog copyLog;
        if (this instanceof CurrentVariableCreateLog) {
            copyLog = new CurrentVariableCreateLog();
        } else if (this instanceof CurrentVariableUpdateLog) {
            copyLog = new CurrentVariableUpdateLog();
        } else if (this instanceof CurrentVariableDeleteLog) {
            copyLog = new CurrentVariableDeleteLog();
        } else {
            throw new InternalApplicationException("Unexpected " + this);
        }
        copyLog.setBytes(getBytes());
        copyLog.setContent(getContent());
        return copyLog;
    }
}
