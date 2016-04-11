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
 * Default implementation of {@link DBSource} interface. 
 * Reference directly to field value.
 * E.q. id property will be referenced as 'alias.id'. 
 * This {@link DBSource} not contains join restrictions, so can be used only to access properties of root persistence object.
 */
public class DefaultDBSource implements DBSource {

    /**
     * Persistent object of field. Property will be accessed throw this object instance. 
     */
    protected final Class<?> sourceObject;

    /**
     * HQL path to access property value.
     * For example for id property this path is 'id'; for id property of field child is 'child.id'. 
     */
    protected final String valueDBPath;

    /**
     * Creates default implementation of {@link DBSource}. This implementation reference directly to field value.
     * @param sourceObject Persistent object of field. Property will be accessed throw this object instance. 
     * @param valueDBPath HQL path to access property value.
     */
    public DefaultDBSource(Class<?> sourceObject, String valueDBPath) {
        this.valueDBPath = valueDBPath;
        this.sourceObject = sourceObject;
    }

    public Class<?> getSourceObject() {
        return sourceObject;
    }

    public String getValueDBPath(String alias) {
        return alias == null ? valueDBPath : alias + "." + valueDBPath;
    }

    public String getJoinExpression(String alias) {
        return "";
    }
}
