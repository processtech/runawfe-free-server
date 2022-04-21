package ru.runa.wfe.rest.dto;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.Data;
import ru.runa.wfe.presentation.BatchPresentation;
import ru.runa.wfe.presentation.BatchPresentationFactory;
import ru.runa.wfe.presentation.ClassPresentationType;
import ru.runa.wfe.presentation.filter.FilterCriteria;
import ru.runa.wfe.presentation.filter.FilterCriteriaFactory;
import ru.runa.wfe.presentation.filter.StringFilterCriteria;
import ru.runa.wfe.rest.dto.BatchPresentationRequest.Sorting.Order;

@Data
public class BatchPresentationRequest {
    private int pageSize = 100;
    private int pageNumber = 1;
    private Map<String, String> filters = new HashMap<>();
    private List<Sorting> sortings = new ArrayList<>();
    private List<String> variables = new ArrayList<>();

    @Data
    public static class Sorting {
        private String name;
        private Order order;

        public static enum Order {
            asc,
            desc;
        }
    }

    public BatchPresentation toBatchPresentation(ClassPresentationType classPresentationType) {
        BatchPresentation batchPresentation = new BatchPresentationFactory(classPresentationType).createDefault();
        // setRangeSize перед setPageNumber т.к. по дефолту сбрасывает значение pageNumber = 1
        batchPresentation.setRangeSize(pageSize);
        batchPresentation.setPageNumber(pageNumber);
        for (Map.Entry<String, String> entry : filters.entrySet()) {
            if (entry.getValue() == null || entry.getValue().isEmpty() || variables.contains(entry.getKey())) {
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

    private void addFilteredField(BatchPresentation batchPresentation, int fieldIndex, String value) {
        FilterCriteria filterCriteria = FilterCriteriaFactory.createFilterCriteria(batchPresentation, fieldIndex);
        // TODO
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
