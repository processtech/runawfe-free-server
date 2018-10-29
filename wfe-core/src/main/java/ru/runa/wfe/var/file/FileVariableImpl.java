package ru.runa.wfe.var.file;

/**
 * Represents value of file variable in process context.
 * 
 * @author Dofs
 * @since 2.0
 */
public class FileVariableImpl implements FileVariable {
    private static final long serialVersionUID = -5664995254436423315L;
    private String name;
    private byte[] data;
    private String contentType;

    public FileVariableImpl() {
    }

    public FileVariableImpl(String name, byte[] data, String contentType) {
        this.name = name;
        this.data = data;
        this.contentType = contentType;
    }

    public FileVariableImpl(FileVariable fileVariable) {
        this(fileVariable.getName(), fileVariable.getData(), fileVariable.getContentType());
    }

    public FileVariableImpl(String name, String contentType) {
        this(name, null, contentType);
    }

    public void setData(byte[] data) {
        this.data = data;
    }

    @Override
    public byte[] getData() {
        return data;
    }

    @Override
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Override
    public String getContentType() {
        return contentType;
    }

    public void setContentType(String contentType) {
        this.contentType = contentType;
    }

    @Override
    public String toString() {
        return name;
    }

    @Override
    public String getStringValue() {
        return null;
    }
}
