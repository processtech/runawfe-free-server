package ru.runa.wfe.rest.dto;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import ru.runa.wfe.commons.Utils;
import ru.runa.wfe.presentation.BatchPresentation;
import ru.runa.wfe.presentation.BatchPresentationFactory;
import ru.runa.wfe.presentation.ClassPresentationType;
import ru.runa.wfe.presentation.filter.FilterCriteria;
import ru.runa.wfe.presentation.filter.FilterCriteriaFactory;
import ru.runa.wfe.presentation.filter.StringFilterCriteria;
import ru.runa.wfe.rest.dto.WfePagedListFilter.Sorting.Order;

@Data
public class WfePagedListFilter {
    public static final String FILTER_IS_EMPTY = "FILTER_IS_EMPTY";
    private int pageSize;
    private int pageNumber;
    // null in value means missed (posted from web), empty string means 'no parameters'
    private Map<String, String> filters = new HashMap<>();
    private List<Sorting> sortings = new ArrayList<>();
    private List<String> variables = new ArrayList<>();

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Sorting {
        private String name;
        private Order order;

        public static enum Order {
            asc,
            desc;
        }
    }

    public BatchPresentation toBatchPresentation(ClassPresentationType classPresentationType) {
        initDeserializedFields();
        BatchPresentation batchPresentation = new BatchPresentationFactory(classPresentationType).createDefault();
        // setRangeSize should go before setPageNumber due to resetting to pageNumber = 1
        batchPresentation.setRangeSize(pageSize);
        batchPresentation.setPageNumber(pageNumber);
        for (Map.Entry<String, String> entry : filters.entrySet()) {
            if (Utils.isNullOrEmpty(entry.getValue()) || variables.contains(entry.getKey())) {
                continue;
            }
            addFilteredField(batchPresentation, classPresentationType.getFieldIndex(entry.getKey()), entry.getValue());
        }
        int[] fieldsToSortIds = new int[sortings.size()];
        boolean[] sortingModes = new boolean[sortings.size()];
        for (int i = 0; i < sortings.size(); i++) {
            Sorting sorting = getSortings().get(i);
            fieldsToSortIds[i] = classPresentationType.getFieldIndex(sorting.getName());
            sortingModes[i] = Order.asc == sorting.getOrder();
        }
        batchPresentation.setFieldsToSort(fieldsToSortIds, sortingModes);
        if (!variables.isEmpty()) {
            int[] fieldsToDisplayIds = new int[variables.size()];
            int variablePrototypeIndex = classPresentationType.getVariablePrototypeIndex();
            for (int i = 0; i < variables.size(); i++) {
                fieldsToDisplayIds[i] = i;
                String variable = variables.get(i);
                String value = filters.get(variable);
                batchPresentation.addDynamicField(variablePrototypeIndex + i, variable);
                if (filters.containsKey(variable) && value != null && !value.isEmpty()) {
                    addFilteredField(batchPresentation, 0, filters.get(variable));
                }
            }
            batchPresentation.setFieldsToDisplayIds(fieldsToDisplayIds);
        }
        return batchPresentation;
    }

    private void initDeserializedFields() {
        if (filters == null) {
            filters = new HashMap<>();
        }
        if (sortings == null) {
            sortings = new ArrayList<>();
        }
        if (variables == null) {
            variables = new ArrayList<>();
        }
        if (pageSize == 0) {
            pageSize = 100;
        }
        if (pageNumber == 0) {
            pageNumber = 1;
        }
    }

    private void addFilteredField(BatchPresentation batchPresentation, int fieldIndex, String value) {
        FilterCriteria filterCriteria = FilterCriteriaFactory.createFilterCriteria(batchPresentation, fieldIndex);
        if (!FILTER_IS_EMPTY.equals(value.isEmpty())) {
            if (value.endsWith("/i") && filterCriteria instanceof StringFilterCriteria) {
                ((StringFilterCriteria) (filterCriteria)).applyCaseSensitive(true);
                value = value.substring(0, value.length() - 2);
            }
            // TODO #2261
            String[] templates = filterCriteria.getTemplatesCount() > 1 ? value.split("\\|", -1) : new String[] { value };
            filterCriteria.applyFilterTemplates(templates);
            batchPresentation.getFilteredFields().put(fieldIndex, filterCriteria);
        }
    }
}
