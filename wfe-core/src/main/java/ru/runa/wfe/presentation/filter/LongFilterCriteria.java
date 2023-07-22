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
import ru.runa.wfe.presentation.hibernate.QueryParametersMap;

/**
 * Created on 01.09.2005 TODO add BETWEEN support
 */
public class LongFilterCriteria extends FilterCriteria {
    private static final long serialVersionUID = 642103915780987672L;

    public LongFilterCriteria() {
        super(1);
    }

    public LongFilterCriteria(Long value) {
        super(new String[] { value != null ? value.toString() : "" });
    }

    @Override
    protected void validate(String[] newTemplates) throws FilterFormatException {
        super.validate(newTemplates);
        try {
            String[] values = newTemplates[0].split(",");
            if (values.length == 1) {
                values = newTemplates[0].split("-");
            }
            for (String value : values) {
                Long.parseLong(value.trim());
            }
        } catch (NumberFormatException nfe) {
            throw new FilterFormatException(nfe.getMessage());
        }
    }

    @Override
    public String buildWhereCondition(String aliasedFieldName, QueryParametersMap placeholders) {
        final String template = getFilterTemplate(0);
        if (template.contains(",")) {
            return buildInOperator(aliasedFieldName);
        } else if (template.contains("-")) {
            return buildBetweenOperator(aliasedFieldName);
        }
        final String placeHolderName = makePlaceHolderName(aliasedFieldName);
        final StringBuilder sb = new StringBuilder(aliasedFieldName);
        sb.append(" = :").append(placeHolderName);
        sb.append(" ");
        placeholders.add(placeHolderName, Long.valueOf(template));
        return sb.toString();
    }

    private String buildInOperator(String aliasedFieldName) {
        String where = "";
        String[] values = getFilterTemplate(0).split(",");
        if (values.length > 0) {
            for (String value : values) {
                if (Strings.isNullOrEmpty(where)) {
                    where = aliasedFieldName + " IN (";
                } else {
                    where += ",";
                }
                where += "'" + value.trim() + "'";
            }
            where += ")";
        }
        return where;
    }

    private String buildBetweenOperator(String aliasedFieldName) {
        String where = "";
        String[] values = getFilterTemplate(0).split("-");
        if (values.length == 2) {
            where = aliasedFieldName + " BETWEEN '" + values[0].trim() + "' AND '" + values[1].trim() + "'";
        }
        return where;
    }

}
