package ru.runa.wfe.var.file;

import java.io.Serializable;

/**
 * Represents file variable value.
 * 
 * @author dofs
 * @since 4.2.0
 */
public interface IFileVariable extends Serializable {

    /**
     * @return file name, not <code>null</code>
     */
    public String getName();

    /**
     * @return mime type, not <code>null</code>
     */
    public String getContentType();

    /**
     * @return file data, not <code>null</code>
     */
    public byte[] getData();

    /**
     * @return string representation of external storage, can be
     *         <code>null</code>
     */
    public String getStringValue();

}
