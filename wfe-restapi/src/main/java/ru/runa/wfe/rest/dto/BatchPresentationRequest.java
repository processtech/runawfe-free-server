package ru.runa.wfe.rest.dto;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.Data;
import ru.runa.wfe.definition.DefinitionClassPresentation;
import ru.runa.wfe.execution.CurrentProcessClassPresentation;
import ru.runa.wfe.presentation.BatchPresentation;
import ru.runa.wfe.presentation.BatchPresentationFactory;
import ru.runa.wfe.presentation.ClassPresentationType;
import ru.runa.wfe.presentation.filter.StringFilterCriteria;
import ru.runa.wfe.rest.dto.BatchPresentationRequest.Sorting.Order;
import ru.runa.wfe.task.TaskClassPresentation;

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

    // TODO classPresentationType contains not friendly field names
    public BatchPresentation toBatchPresentation(ClassPresentationType classPresentationType) {
        BatchPresentation batchPresentation = new BatchPresentationFactory(classPresentationType).createDefault();
        batchPresentation.setPageNumber(pageNumber);
        batchPresentation.setRangeSize(pageSize);
        for (Map.Entry<String, String> entry : filters.entrySet()) {
            if (entry.getValue() == null || entry.getValue().isEmpty()) {
                continue;
            }
            
            //TODO Добавил костыль со switch, после доработок в ядре руны нужно удалить
            String entryKey = "";
            switch (entry.getKey()) {
            case "id":
                entryKey = CurrentProcessClassPresentation.PROCESS_ID;
                break;
            case "name":
                entryKey = CurrentProcessClassPresentation.DEFINITION_NAME;
                break;
//            case "category":
//                entryKey = CurrentProcessClassPresentation.PROCESS_EXECUTION_STATUS;
//                break;
            case "executionStatus":
                entryKey = CurrentProcessClassPresentation.PROCESS_EXECUTION_STATUS;
                break;
            case "startDate":
                entryKey = CurrentProcessClassPresentation.PROCESS_START_DATE;
                break;
            case "endDate":
                entryKey = CurrentProcessClassPresentation.PROCESS_END_DATE;
                break;
            }
            int fieldIndex = classPresentationType.getFieldIndex(entryKey);
            
            // int fieldIndex = classPresentationType.getFieldIndex(entry.getKey());
            // only strings are supported now
            batchPresentation.getFilteredFields().put(fieldIndex, new StringFilterCriteria(entry.getValue()));
        }
        int[] fieldsToSortIds = new int[sortings.size()];
        boolean[] sortingModes = new boolean[sortings.size()];
        for (int i = 0; i < sortings.size(); i++) {
            Sorting sorting = getSortings().get(i);
            //TODO Добавил костыль со switch, после доработок в ядре руны нужно удалить
            switch(sorting.getName()) {
            case "processId": 
                sorting.setName(TaskClassPresentation.PROCESS_ID);
                break;
            case "name":
                sorting.setName(TaskClassPresentation.NAME);
                break;
            case "definitionName":
                sorting.setName(TaskClassPresentation.DEFINITION_NAME);
                break;    
            case "creationDate":
                sorting.setName(TaskClassPresentation.TASK_CREATE_DATE);
                break;
            case "deadlineDate":
                sorting.setName(TaskClassPresentation.TASK_DEADLINE);
                break;
            //TODO Сделать сортировку по типу процесса, пока не думал как
            case "category":
                sorting.setName(DefinitionClassPresentation.TYPE);
                break;
            case "description":
                sorting.setName(TaskClassPresentation.DESCRIPTION);
                break;
            }
            fieldsToSortIds[i] = classPresentationType.getFieldIndex(sorting.getName());
            sortingModes[i] = Order.asc == sorting.getOrder();
        }
        batchPresentation.setFieldsToSort(fieldsToSortIds, sortingModes);
        if (!variables.isEmpty()) {
            int[] fieldsToDisplayIds = new int[variables.size()];
            // TODO now hardcoded field name for tasks only
            int dynamicFieldIndex = classPresentationType.getFieldIndex(TaskClassPresentation.TASK_VARIABLE);
            for (int i = 0; i < variables.size(); i++) {
                fieldsToDisplayIds[i] = i;
                batchPresentation.addDynamicField(dynamicFieldIndex + i, variables.get(i));
            }
            batchPresentation.setFieldsToDisplayIds(fieldsToDisplayIds);
        }
        return batchPresentation;
    }
}
