/*
 * This file is part of the RUNA WFE project.
 * Copyright (C) 2004-2006, Joint stock company "RUNA Technology"
 * All rights reserved.
 *
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 */
package ru.runa.wfe.presentation;

import ru.runa.wfe.commons.Utils;
import ru.runa.wfe.var.Variable;
import ru.runa.wfe.var.impl.DateVariable;
import ru.runa.wfe.var.impl.DoubleVariable;
import ru.runa.wfe.var.impl.LongVariable;
import ru.runa.wfe.var.impl.StringVariable;

/**
 * Implementation of {@link DBSource} interface for referencing variable values.
 * 
 * @author Dofs
 * @see #394
 */
public class VariableDBSources {

    /**
     * Creates DB sources for variable search
     * 
     * @param processPath
     *            process path join expression, can be <code>null</code>
     */
    public static DBSource[] get(String processPath) {
        return new DBSource[] { new BaseVariableDBSource(Variable.class, processPath), new StorableVariableDBSource(DateVariable.class),
                new StorableVariableDBSource(DoubleVariable.class), new StorableVariableDBSource(LongVariable.class),
                new StringVariableDBSource(StringVariable.class) };
    }

    /**
     * Used as inheritance root and for filtering.
     */
    public static class BaseVariableDBSource extends DefaultDBSource {
        public static final String STRING_VALUE = "stringValue";
        private final String processPath;

        public BaseVariableDBSource(Class<?> sourceObject, String processPath) {
            super(sourceObject, null);
            this.processPath = processPath;
        }

        // classNameSQL + ".id=" + alias + ".process";
        // classNameSQL + ".id=" + alias + ".process"
        // classNameSQL + ".process=" + alias + ".process";

        @Override
        public String getJoinExpression(String alias) {
            StringBuilder join = new StringBuilder(ClassPresentation.classNameSQL);
            if (!Utils.isNullOrEmpty(processPath)) {
                join.append(".").append(processPath);
            }
            join.append("=").append(alias).append(".process");
            return join.toString();
        }

        @Override
        public String getValueDBPath(AccessType accessType, String alias) {
            if (accessType == AccessType.FILTER) {
                return alias == null ? STRING_VALUE : alias + "." + STRING_VALUE;
            }
            return super.getValueDBPath(accessType, alias);
        }

    }

    /**
     * Used only for sorting
     */
    public static class StorableVariableDBSource extends DefaultDBSource {

        public StorableVariableDBSource(Class<?> sourceObject) {
            super(sourceObject, "storableValue");
        }

    }

    /**
     * Used only for sorting
     */
    public static class StringVariableDBSource extends DefaultDBSource {

        public StringVariableDBSource(Class<?> sourceObject) {
            super(sourceObject, BaseVariableDBSource.STRING_VALUE);
        }

    }

}
