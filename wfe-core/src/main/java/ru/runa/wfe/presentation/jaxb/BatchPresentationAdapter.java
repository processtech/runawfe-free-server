package ru.runa.wfe.presentation.jaxb;

import com.google.common.base.Strings;
import java.util.List;
import javax.xml.bind.annotation.adapters.XmlAdapter;
import ru.runa.wfe.presentation.BatchPresentation;
import ru.runa.wfe.presentation.BatchPresentationFactory;
import ru.runa.wfe.presentation.filter.FilterCriteria;
import ru.runa.wfe.presentation.filter.FilterCriteriaFactory;
import ru.runa.wfe.presentation.jaxb.WfBatchPresentation.Filter;
import ru.runa.wfe.presentation.jaxb.WfBatchPresentation.Sorting;

public class BatchPresentationAdapter extends XmlAdapter<WfBatchPresentation, BatchPresentation> {

    @Override
    public WfBatchPresentation marshal(BatchPresentation batchPresentation) throws Exception {
        throw new UnsupportedOperationException();
    }

    @Override
    public BatchPresentation unmarshal(WfBatchPresentation wfBatchPresentation) throws Exception {
        BatchPresentation batchPresentation = new BatchPresentationFactory(wfBatchPresentation.getClassPresentationType()).createDefault();
        batchPresentation.setPageNumber(wfBatchPresentation.getPageNumber());
        batchPresentation.setRangeSize(wfBatchPresentation.getPageSize());
        for (Filter filter : wfBatchPresentation.getFilters()) {
            if (Strings.isNullOrEmpty(filter.getValue()) || wfBatchPresentation.getVariables().contains(filter.getName())) {
                continue;
            }
            addFilteredField(batchPresentation, wfBatchPresentation.getClassPresentationType().getFieldIndex(filter.getName()), filter);
        }
        int[] fieldsToSortIds = new int[wfBatchPresentation.getSortings().size()];
        boolean[] sortingModes = new boolean[wfBatchPresentation.getSortings().size()];
        for (int i = 0; i < wfBatchPresentation.getSortings().size(); i++) {
            Sorting sorting = wfBatchPresentation.getSortings().get(i);
            fieldsToSortIds[i] = wfBatchPresentation.getClassPresentationType().getFieldIndex(sorting.getName());
            sortingModes[i] = Sorting.Order.asc == sorting.getOrder();
        }
        batchPresentation.setFieldsToSort(fieldsToSortIds, sortingModes);
        int[] fieldsToDisplayIds = new int[wfBatchPresentation.getVariables().size() + wfBatchPresentation.getSwimlanes().size()];
        int variablePrototypeIndex = wfBatchPresentation.getClassPresentationType().getVariablePrototypeIndex();
        int swimlanePrototypeIndex = wfBatchPresentation.getClassPresentationType().getSwimlanePrototypeIndex();
        for (int i = 0; i < wfBatchPresentation.getVariables().size(); i++) {
            fieldsToDisplayIds[i] = i;
            String variable = wfBatchPresentation.getVariables().get(i);
            batchPresentation.addDynamicField(variablePrototypeIndex + i, variable);
            Filter filter = getFilter(wfBatchPresentation.getFilters(), variable);
            if (filter != null) {
                addFilteredField(batchPresentation, 0, filter);
            }
        }
        for (int i = 0; i < wfBatchPresentation.getSwimlanes().size(); i++) {
            fieldsToDisplayIds[wfBatchPresentation.getVariables().size() + i] = wfBatchPresentation.getVariables().size() + i;
            String swimlane = wfBatchPresentation.getSwimlanes().get(i);
            batchPresentation.addDynamicField(wfBatchPresentation.getVariables().size() + swimlanePrototypeIndex + i, swimlane);
            Filter filter = getFilter(wfBatchPresentation.getFilters(), swimlane);
            if (filter != null) {
                addFilteredField(batchPresentation, 0, filter);
            }
        }
        batchPresentation.setFieldsToDisplayIds(fieldsToDisplayIds);
        return batchPresentation;
    }

    private void addFilteredField(BatchPresentation batchPresentation, int fieldIndex, Filter filter) {
        FilterCriteria filterCriteria = FilterCriteriaFactory.createFilterCriteria(batchPresentation, fieldIndex);
        // TODO #2261
        String[] templates = filterCriteria.getTemplatesCount() > 1 ? filter.getValue().split("\\|", -1) : new String[] { filter.getValue() };
        filterCriteria.applyFilterTemplates(templates);
        filterCriteria.setExclusive(filter.isExclusive());
        batchPresentation.getFilteredFields().put(fieldIndex, filterCriteria);
    }

    private Filter getFilter(List<Filter> map, String key) {
        for (Filter entry : map) {
            if (entry.getName().equals(key)) {
                return entry;
            }
        }
        return null;
    }

}
