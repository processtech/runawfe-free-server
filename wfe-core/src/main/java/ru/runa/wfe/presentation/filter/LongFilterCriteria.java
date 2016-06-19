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

import ru.runa.wfe.presentation.hibernate.QueryParameter;

/**
 * Created on 01.09.2005
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
            Long.parseLong(newTemplates[0]);
        } catch (NumberFormatException nfe) {
            throw new FilterFormatException(nfe.getMessage());
        }
    }

    @Override
    public String buildWhereCondition(String aliasedFieldName, Map<String, QueryParameter> placeholders) {
        final String placeHolderName = makePlaceHolderName(aliasedFieldName);
        final StringBuilder whereStringBuilder = new StringBuilder(aliasedFieldName);
        whereStringBuilder.append(" = :").append(placeHolderName);
        whereStringBuilder.append(" ");
        placeholders.put(placeHolderName, new QueryParameter(placeHolderName, Long.valueOf(getFilterTemplate(0))));
        return whereStringBuilder.toString();
    }

}
