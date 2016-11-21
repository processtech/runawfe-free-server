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
 * {@link DBSource} implementation, which cast field value to CHAR(128). Must be used to access potentially large strings (effect is of this source is substring of value).
 */
public class SubstringDBSource extends DefaultDBSource {

    /**
     * Creates {@link DBSource} implementation, which cast field value to CHAR(128).
     * 
     * @param sourceObject
     *            Persistent object of field. Property will be accessed throw this object instance.
     * @param valueDBPath
     *            HQL path to access property value.
     */
    public SubstringDBSource(Class<?> sourceObject, String valueDBPath) {
        super(sourceObject, valueDBPath);
    }

    @Override
    public String getValueDBPath(String alias) {
        return alias == null ? valueDBPath : "CAST(" + alias + "." + valueDBPath + " AS CHAR(128))";
    }
}
