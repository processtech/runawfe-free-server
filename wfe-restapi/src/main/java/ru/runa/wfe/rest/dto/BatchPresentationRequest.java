package ru.runa.wfe.rest.dto;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.Data;
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
            int fieldIndex = classPresentationType.getFieldIndex(entry.getKey());
            // only strings are supported now
            batchPresentation.getFilteredFields().put(fieldIndex, new StringFilterCriteria(entry.getValue()));
        }
        int[] fieldsToSortIds = new int[sortings.size()];
        boolean[] sortingModes = new boolean[sortings.size()];
        for (int i = 0; i < sortings.size(); i++) {
            Sorting sorting = getSortings().get(i);
            //TODO Добавил костыль, надо думать как лучше сделать для остальных полей сортировки
            if (sorting.getName().equals("processId")) {
                sorting.setName("batch_presentation.task.process_id");
            }
            fieldsToSortIds[i] = classPresentationType.getFieldIndex(sorting.getName());
            sortingModes[i] = Order.asc == sorting.getOrder();
        }
        batchPresentation.setFieldsToSort(fieldsToSortIds, sortingModes);
        if (!variables.isEmpty()) {
            int[] fieldsToDisplayIds = new int[variables.size()];
            for (int i = 0; i < variables.size(); i++) {
                fieldsToDisplayIds[i] = i;
                // TODO now hardcoded field name for tasks only
                int dynamicFieldIndex = classPresentationType.getFieldIndex(TaskClassPresentation.TASK_VARIABLE);
                // TODO seems like there is a bug if we will add more than one variable
                batchPresentation.addDynamicField(dynamicFieldIndex, variables.get(i));
            }
            batchPresentation.setFieldsToDisplayIds(fieldsToDisplayIds);
        }
        return batchPresentation;
    }
}
