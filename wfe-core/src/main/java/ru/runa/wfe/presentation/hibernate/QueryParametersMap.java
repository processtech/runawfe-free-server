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
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import org.hibernate.Hibernate;
import org.hibernate.Query;
import org.hibernate.type.Type;

public class QueryParametersMap {
    private final HashMap<String, QueryParameterValue> map = new HashMap<>();

    public Set<String> getNames() {
        return map.keySet();
    }

    public void add(String name, String value) {
        map.put(name, new QueryParameterValue(value, Hibernate.STRING, false));
    }

    public void add(String name, Long value) {
        map.put(name, new QueryParameterValue(value, Hibernate.LONG, false));
    }

    public void add(String name, Date value) {
        map.put(name, new QueryParameterValue(value, Hibernate.DATE, false));
    }

    public void add(String name, Object[] value, Type type) {
        map.put(name, new QueryParameterValue(value, type, true));
    }

    public void add(String name, Collection<?> value) {
        map.put(name, new QueryParameterValue(value, null, true));
    }

    public void apply(Query q) {
        for (Map.Entry<String, QueryParameterValue> kv : map.entrySet()) {
            kv.getValue().apply(q, kv.getKey());
        }
    }
}
