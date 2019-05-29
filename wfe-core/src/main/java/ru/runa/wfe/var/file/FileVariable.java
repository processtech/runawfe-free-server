package ru.runa.wfe.var.file;

import java.io.Serializable;

/**
 * Represents file variable value.
 * 
 * @author dofs
 */
public interface FileVariable extends Serializable {

    /**
     * @return file name, not <code>null</code>
     */
    String getName();

    /**
     * @return mime type, not <code>null</code>
     */
    String getContentType();

    /**
     * @return file data, not <code>null</code>
     */
    byte[] getData();

    /**
     * @return string representation of external storage, can be <code>null</code>
     */
    String getStringValue();
}
