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
