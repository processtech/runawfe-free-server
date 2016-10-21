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
package ru.runa.wfe.security;

import java.io.Serializable;

import javax.xml.bind.annotation.XmlType;

/**
 * Interface for all identifiable components, which can be secured using
 * permission.
 */
@XmlType
public abstract class Identifiable implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * Return identity for current object. Object with same id and type has same
     * permissions, even if object is not equals. Object with same type but
     * different id has different permissions.
     * 
     * @return Object identity
     */
    public abstract Long getIdentifiableId();

    /**
     * Returns object type identity. Object with same id and type has same
     * permissions, even if object is not equals. Object with same id but
     * different type has different permissions.
     * 
     * @return Object type identity.
     */
    public abstract SecuredObjectType getSecuredObjectType();
}
