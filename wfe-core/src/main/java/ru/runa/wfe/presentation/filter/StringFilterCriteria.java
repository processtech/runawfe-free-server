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
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import ru.runa.wfe.presentation.hibernate.QueryParameter;

public class StringFilterCriteria extends FilterCriteria {
    public static final String ANY_SYMBOLS = "*";
    public static final String ANY_SYMBOL = "?";
    private static final long serialVersionUID = -1849845246809052465L;
    private static final String QUOTED_ANY_SYMBOLS = Pattern.quote(ANY_SYMBOLS);
    private static final String QUOTED_ANY_SYMBOL = Pattern.quote(ANY_SYMBOL);
    private static final String DB_ANY_SYMBOLS = Matcher.quoteReplacement("%");
    private static final String DB_ANY_SYMBOL = Matcher.quoteReplacement("_");
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
        final StringLikeFilter likeFilter = calcUseLike(getFilterTemplate(0));
        String searchFilter = likeFilter.getSearchFilter();

        String alias = persistentObjectQueryAlias + fieldName.replaceAll("\\.", "");
        String where = "";
        if (ignoreCase) {
            where += "lower(";
        }
        where += persistentObjectQueryAlias + "." + fieldName;
        if (ignoreCase) {
            where += ")";
            searchFilter = searchFilter.toLowerCase();
        }
        where += " ";
        where += likeFilter.isUseLike() ? "like" : "=";
        where += " :" + alias + " ";
        placeholders.put(alias, new QueryParameter(alias, searchFilter));
        return where;
    }

    public static StringLikeFilter calcUseLike(String parSearchFilter) {
        boolean useLike = false;
        String searchFilter = parSearchFilter;
        if (searchFilter.contains(ANY_SYMBOLS)) {
            searchFilter = searchFilter.replaceAll(QUOTED_ANY_SYMBOLS, DB_ANY_SYMBOLS);
            useLike = true;
        }
        if (searchFilter.contains(ANY_SYMBOL)) {
            searchFilter = searchFilter.replaceAll(QUOTED_ANY_SYMBOL, DB_ANY_SYMBOL);
            useLike = true;
        }
        return new StringLikeFilter(searchFilter, useLike);
    }
}