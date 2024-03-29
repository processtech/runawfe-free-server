package ru.runa.wfe.service.client;

import com.google.common.base.Objects;
import ru.runa.wfe.service.delegate.Delegates;
import ru.runa.wfe.user.User;
import ru.runa.wfe.var.file.FileVariable;
import ru.runa.wfe.var.file.LinkedWithProcessDefinition;

/**
 * This class eliminates byte[] data transferring without usage. Case 2 added since v4.3.0: used for uploading custom FileVariable implementation
 * classes.
 *
 * @author dofs
 * @since 4.0
 */
public class FileVariableProxy implements FileVariable, LinkedWithProcessDefinition {
    private static final long serialVersionUID = 1L;
    private User user;
    private Long processId;
    private Long definitionId;
    private String variableName;
    private String name;
    private String contentType;
    private byte[] data;
    private String unproxiedClassName;
    private String stringValue;

    public FileVariableProxy(User user, Long processId, String variableName, FileVariable fileVariable) {
        this.user = user;
        this.processId = processId;
        this.variableName = variableName;
        if (fileVariable != null) {
            this.name = fileVariable.getName();
            this.contentType = fileVariable.getContentType();
            this.stringValue = fileVariable.getStringValue();
        }
    }

    public FileVariableProxy(User user, Long processId, Long definitionId, String variableName, FileVariable fileVariable) {
        this(user, processId, variableName, fileVariable);
        this.definitionId = definitionId;
        if (definitionId == null && processId == null) {
            throw new IllegalArgumentException("One of processId or definitionId should not be null");
        }
    }

    /**
     * Used by TNMS.
     */
    @SuppressWarnings("unused")
    public FileVariableProxy(String unproxiedClassName, String stringValue) {
        this.unproxiedClassName = unproxiedClassName;
        this.stringValue = stringValue;
    }

    public String getVariableName() {
        return variableName;
    }

    @Override
    public String getName() {
        return name;
    }

    @Override
    public String getContentType() {
        return contentType;
    }

    @Override
    public byte[] getData() {
        if (data == null) {
            FileVariable fileVariable = getUnproxiedFileVariable();
            data = fileVariable != null ? fileVariable.getData() : new byte[0];
        }
        return data;
    }

    public FileVariable getUnproxiedFileVariable() {
        return processId != null ?
                Delegates.getExecutionService().getFileVariableValue(user, processId, variableName) :
 Delegates
                .getDefinitionService().getFileVariableDefaultValue(user, definitionId, variableName);
    }

    public String getUnproxiedClassName() {
        return unproxiedClassName;
    }

    @Override
    public String getStringValue() {
        return stringValue;
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof FileVariableProxy) {
            FileVariableProxy p = (FileVariableProxy) obj;
            return Objects.equal(processId, p.processId) && Objects.equal(definitionId, p.definitionId)
                    &&
                    Objects.equal(variableName, p.variableName) && Objects.equal(stringValue, p.stringValue);
        }
        return super.equals(obj);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(processId, definitionId, variableName, stringValue);
    }

    @Override
    public Long getDefinitionId() {
        return definitionId;
    }
}
