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

import com.google.common.base.Strings;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import ru.runa.wfe.commons.SqlCommons;
import ru.runa.wfe.commons.SqlCommons.StringEqualsExpression;
import ru.runa.wfe.presentation.hibernate.QueryParametersMap;

public class StringFilterCriteria extends FilterCriteria {
    public static final String ANY_SYMBOLS = SqlCommons.ANY_SYMBOLS;
    public static final String ANY_SYMBOL = SqlCommons.ANY_SYMBOL;
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
    public String buildWhereCondition(String aliasedFieldName, QueryParametersMap placeholders) {
        final String template = getFilterTemplate(0);
        try {
            Object object = new JSONParser().parse(template);
            if (object instanceof JSONArray) {
                return buildInOperator(aliasedFieldName, (JSONArray) object);
            } else if (object instanceof JSONObject) {
                return buildBetweenOperator(aliasedFieldName, (JSONObject) object);
            }
        } catch (ParseException e) {
            // do nothing
        }
        StringEqualsExpression expression = SqlCommons.getStringEqualsExpression(template);
        String searchValue = expression.getValue();
        String alias = makePlaceHolderName(aliasedFieldName);
        String where = "";
        if (ignoreCase) {
            where += "lower(";
        }
        where += aliasedFieldName;
        if (ignoreCase) {
            where += ")";
            searchValue = searchValue.toLowerCase();
        }
        where += " ";
        where += expression.getComparisonOperator();
        where += " :" + alias + " ";
        placeholders.add(alias, searchValue);
        return where;
    }

    private String buildInOperator(String aliasedFieldName, JSONArray array) {
        String where = "";
        if (array.size() > 0) {
            for (int i = 0; i < array.size(); i++) {
                if (Strings.isNullOrEmpty(where)) {
                    where = aliasedFieldName + " IN (";
                } else {
                    where += ",";
                }
                where += "'" + array.get(i).toString().trim() + "'";
            }
            where += ")";
        }
        return where;
    }

    private String buildBetweenOperator(String aliasedFieldName, JSONObject object) {
        String where = "";
        String min = (String) object.get("min");
        String max = (String) object.get("max");
        if (object.size() == 1) {
            if (min == null) {
                where = aliasedFieldName + " <= '" + max.trim() + "'";
            } else {
                where = aliasedFieldName + " >= '" + min.trim() + "'";
            }
        } else {
            where = aliasedFieldName + " BETWEEN '" + min.trim() + "' AND '" + max.toString().trim() + "'";
        }
        return where;
    }

}
