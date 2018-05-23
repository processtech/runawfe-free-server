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
package ru.runa.wfe.presentation.hibernate;

import java.util.Collection;
import org.hibernate.Query;
import org.hibernate.type.Type;

public class QueryParameterValue {
    private final Object value;
    private final Type type;
    private final boolean isList;

    /**
     * @param type May be null.
     * @param isList If true, setParameterList() will be called on query instead of setParameter().
     */
    QueryParameterValue(Object value, Type type, boolean isList) {
        this.value = value;
        this.type = type;
        this.isList = isList;
    }

    public void apply(Query q, String name) {
        if (isList) {
            if (type != null) {
                if (value instanceof Collection<?>) {
                    q.setParameterList(name, (Collection<?>)value, type);
                } else {
                    q.setParameterList(name, (Object[])value, type);
                }
            } else {
                if (value instanceof Collection<?>) {
                    q.setParameterList(name, (Collection<?>)value);
                } else {
                    q.setParameterList(name, (Object[])value);
                }
            }
        } else {
            if (type != null) {
                q.setParameter(name, value, type);
            } else {
                q.setParameter(name, value);
            }
        }
    }
}
