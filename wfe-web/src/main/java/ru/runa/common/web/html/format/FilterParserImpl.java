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
package ru.runa.common.web.html.format;

import java.util.HashMap;
import java.util.Map;

import ru.runa.wfe.presentation.BatchPresentation;
import ru.runa.wfe.presentation.filter.FilterCriteria;
import ru.runa.wfe.presentation.filter.FilterCriteriaFactory;

/**
 * 
 * Created on 09.02.2007
 * 
 */
public class FilterParserImpl implements FiltersParser {

    private FilterCriteria createFilterCriteria(String fieldType) {
        return FilterCriteriaFactory.createFilterCriteria(fieldType);
    }

    @Override
    public Map<Integer, FilterCriteria> parse(BatchPresentation batchPresentation, Map<Integer, String[]> fieldsToFilterTemplatedMap) {
        Map<Integer, FilterCriteria> newFilteredFieldsMap = new HashMap<Integer, FilterCriteria>();
        for (Map.Entry<Integer, String[]> entry : fieldsToFilterTemplatedMap.entrySet()) {
            int fieldIndex = entry.getKey();
            String[] templates = entry.getValue();
            String fieldType = batchPresentation.getAllFields()[fieldIndex].fieldType;
            FilterCriteria filterCriteria = createFilterCriteria(fieldType);
            filterCriteria.applyFilterTemplates(templates);
            newFilteredFieldsMap.put(fieldIndex, filterCriteria);
        }
        return newFilteredFieldsMap;
    }
}
