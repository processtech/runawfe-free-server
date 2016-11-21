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
package ru.runa.wfe.presentation;

/**
 * Interface for components, describes HQL statement to access stored field value.
 */
public interface DBSource {

    /**
     * Source persistent object of this field. Alias to this object will be passed to other function. Persistent class will be added to HQL query then required.
     * 
     * @return Persistent class of this field.
     */
    public Class<?> getSourceObject();

    /**
     * Returns HQL expression to access field value. Use alias parameter to access field persistent object (May be null, see parameter description). For example to access id
     * property with alias != null return 'alias.id' and with alias = null return 'id'.
     * 
     * @param alias
     *            Alias, assigned to field persistent class in HQL query. May be null, when return value must be HQL path to access value without root persistent class.
     * @return HQL expression to access field value. See alias parameter for more information.
     */
    public String getValueDBPath(String alias);

    /**
     * How to join root {@link ClassPresentation} persistent object with field persistent object. If field is a property of root persistent object, then return empty string. If
     * field persistent object must be joined by id with root, then return: ClassPresentation.classNameSQL + ".id=" + alias + ".id"
     * 
     * @param alias
     *            Alias, assigned to field persistent class in HQL query.
     * @return HQL expression to join root persistent object with field persistent object.
     */
    public String getJoinExpression(String alias);
}
