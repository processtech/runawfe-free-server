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
package ru.runa.wfe.presentation.filter;

import java.util.Map;

import ru.runa.wfe.commons.SQLCommons;
import ru.runa.wfe.commons.SQLCommons.StringEqualsExpression;
import ru.runa.wfe.presentation.hibernate.QueryParameter;

public class StringFilterCriteria extends FilterCriteria {
    public static final String ANY_SYMBOLS = SQLCommons.ANY_SYMBOLS;
    public static final String ANY_SYMBOL = SQLCommons.ANY_SYMBOL;
    private static final long serialVersionUID = -1849845246809052465L;
    private boolean ignoreCase;

    public StringFilterCriteria() {
        super(1);
    }

    public StringFilterCriteria(String filterValue) {
        super(new String[] { filterValue });
    }

    public StringFilterCriteria(String filterValue, boolean ignoreCase) {
        this(filterValue);
        this.ignoreCase = ignoreCase;
    }

    @Override
    public String buildWhereCondition(String fieldName, String persistentObjectQueryAlias, Map<String, QueryParameter> placeholders) {
        StringEqualsExpression expression = SQLCommons.getStringEqualsExpression(getFilterTemplate(0));
        String searchValue = expression.getValue();
        String alias = persistentObjectQueryAlias + fieldName.replaceAll("\\.", "");
        String where = "";
        if (ignoreCase) {
            where += "lower(";
        }
        // Let "((" be the mark of very special case, in which [field] is complicated expression, which holds parameter inside, and not here.
        if (!fieldName.startsWith("((")) {
            where += persistentObjectQueryAlias + "." + fieldName;
        } else {
            // So, we simply add expression to query
            where += fieldName;
            // Set parameter with predefined name here
            alias = "param_extra_case";
        }
        if (ignoreCase) {
            where += ")";
            searchValue = searchValue.toLowerCase();
        }
        where += " ";
     // Ordinal case - not special noted above. For that case - skip this.
        if (!fieldName.startsWith("((")) {
            where += expression.getComparisonOperator();
            where += " :" + alias + " ";
        }
        placeholders.put(alias, new QueryParameter(alias, searchValue));
        return where;
    }
}
