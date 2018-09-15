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
import lombok.val;
import ru.runa.wfe.presentation.BatchPresentation;
import ru.runa.wfe.presentation.filter.FilterCriteria;
import ru.runa.wfe.presentation.filter.FilterCriteriaFactory;

public class FilterParser {

    /**
     * Returns Map (Integer field index, FilterCriteria filterCriteria for the field)
     */
    public Map<Integer, FilterCriteria> parse(BatchPresentation batchPresentation, Map<Integer, String[]> fieldsToFilterTemplatedMap) {
        val newFilteredFieldsMap = new HashMap<Integer, FilterCriteria>();
        for (val entry : fieldsToFilterTemplatedMap.entrySet()) {
            int fieldIndex = entry.getKey();
            String[] templates = entry.getValue();
            FilterCriteria filterCriteria = FilterCriteriaFactory.createFilterCriteria(batchPresentation, fieldIndex);
            filterCriteria.applyFilterTemplates(templates);
            newFilteredFieldsMap.put(fieldIndex, filterCriteria);
        }
        return newFilteredFieldsMap;
    }
}
