/*
 * This file is part of the RUNA WFE project.
 * 
 * This program is free software; you can redistribute it and/or 
 * modify it under the terms of the GNU Lesser General Public License 
 * as published by the Free Software Foundation; version 2.1 
 * of the License. 
 * 
 * This program is distributed in the hope that it will be useful, 
 * but WITHOUT ANY WARRANTY; without even the implied warranty of 
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the 
 * GNU Lesser General Public License for more details. 
 * 
 * You should have received a copy of the GNU Lesser General Public License 
 * along with this program; if not, write to the Free Software 
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA.
 */
package ru.runa.wfe.var.file;

import java.util.Arrays;
import java.util.Objects;

/**
 * Represents value of file variable in process context.
 * 
 * @author Dofs
 * @since 2.0
 */
public class FileVariableImpl implements FileVariable {
    private static final long serialVersionUID = -5664995254436423315L;
    private byte[] data;
    private String name;
    private String contentType;

    public FileVariableImpl() {
    }

    public FileVariableImpl(String name, byte[] data, String contentType) {
        this.data = data;
        this.name = name;
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

    @Override
    public int hashCode() {
        return Objects.hash(data, name, contentType);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }
        if (obj == null || getClass() != obj.getClass()) {
            return false;
        }
        FileVariableImpl other = (FileVariableImpl) obj;
        return Objects.equals(contentType, other.contentType) && Arrays.equals(data, other.data)
                && Objects.equals(name, other.name);
    }
}
