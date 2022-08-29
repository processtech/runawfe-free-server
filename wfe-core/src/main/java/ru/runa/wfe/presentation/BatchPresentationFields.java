package ru.runa.wfe.presentation;

import com.google.common.base.MoreObjects;
import com.google.common.base.Objects;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import ru.runa.wfe.InternalApplicationException;
import ru.runa.wfe.commons.ArraysCommons;
import ru.runa.wfe.presentation.filter.FilterCriteria;

@XmlAccessorType(XmlAccessType.FIELD)
public class BatchPresentationFields implements Serializable {
    private static final long serialVersionUID = 1L;

    /**
     * Displayed fields indexes in correct order.
     */
    int[] displayIds;

    /**
     * Sorting fields indexes in correct order. Size of array is equals to fieldsToSortModes size.
     */
    int[] sortIds;

    /**
     * Sorting fields modes. Size of array is equals to fieldsToSortIds size.
     */
    boolean[] sortModes;

    /**
     * Grouping fields indexes in correct order.
     */
    int[] groupIds;

    /**
     * {@link Map} from field index to {@link FilterCriteria} for filter.
     */
    final HashMap<Integer, FilterCriteria> filters = Maps.newHashMap();

    /**
     * Filter by variable fields
     */
    final List<DynamicField> dynamics = Lists.newArrayList();

    final List<String> expandedBlocks = Lists.newArrayList();

    public BatchPresentationFields() {
    }

    public boolean setFilteredFields(Map<Integer, FilterCriteria> newFilteredFieldsMap) {
        boolean resetPageNumber = false;
        if (filters.size() == newFilteredFieldsMap.size()) {
            for (Map.Entry<Integer, FilterCriteria> entry : filters.entrySet()) {
                if (!entry.getValue().equals(newFilteredFieldsMap.get(entry.getKey()))) {
                    resetPageNumber = true;
                    break;
                }
            }
        } else {
            resetPageNumber = true;
        }
        filters.clear();
        for (Map.Entry<Integer, FilterCriteria> entry : newFilteredFieldsMap.entrySet()) {
            filters.put(entry.getKey(), entry.getValue());
        }
        return resetPageNumber;
    }

    public void setFieldsToSort(int[] fieldsToSortIds, boolean[] sortingModes, FieldDescriptor[] allFields) {
        if (fieldsToSortIds.length != sortingModes.length) {
            throw new IllegalArgumentException("Arrays size differs");
        }
        sortIds = fieldsToSortIds;
        sortModes = sortingModes;
        setFieldsToGroup(groupIds, allFields);
    }

    public void setFirstFieldToSort(int newSortFieldId, FieldDescriptor[] allFields) {
        int fieldIndex = ArraysCommons.findPosition(sortIds, newSortFieldId);
        boolean alreadyUsed = fieldIndex != -1;
        boolean[] newFieldsToSortModes;
        int[] newFieldsToSortIds;
        // Bug fix
        while (sortIds.length < sortModes.length) {
            sortModes = ArraysCommons.remove(sortModes, 0);
        }
        // Bug fix end
        if (alreadyUsed) {
            newFieldsToSortIds = ArraysCommons.changePosition(sortIds, fieldIndex, 0);
            newFieldsToSortModes = ArraysCommons.changePosition(sortModes, fieldIndex, 0);
            newFieldsToSortModes[0] = !sortModes[fieldIndex];
        } else {
            newFieldsToSortIds = ArraysCommons.insert(sortIds, 0, newSortFieldId);
            newFieldsToSortModes = ArraysCommons.insert(sortModes, 0, BatchPresentationConsts.ASC);
        }
        setFieldsToSort(newFieldsToSortIds, newFieldsToSortModes, allFields);
    }

    public void addDynamicField(long fieldIdx, String fieldValue) {
        fieldIdx = fieldIdx - dynamics.size();
        for (DynamicField dynamo : dynamics) {
            if (dynamo.getDynamicValue().equals(fieldValue) && dynamo.getFieldIdx() == fieldIdx) {
                return;
            }
        }
        dynamics.add(0, new DynamicField(fieldIdx, fieldValue));
        for (int i = 0; i < groupIds.length; ++i) {
            groupIds[i] = groupIds[i] + 1;
        }
        for (int i = 0; i < sortIds.length; ++i) {
            sortIds[i] = sortIds[i] + 1;
        }
        for (int i = 0; i < displayIds.length; ++i) {
            displayIds[i] = displayIds[i] + 1;
        }
        Map<Integer, FilterCriteria> filteredFieldsMap = new HashMap<>();
        for (Map.Entry<Integer, FilterCriteria> entry : filters.entrySet()) {
            filteredFieldsMap.put(entry.getKey() + 1, entry.getValue());
        }
        filters.clear();
        filters.putAll(filteredFieldsMap);
    }

    public void removeDynamicField(long fieldIdx) {
        dynamics.remove((int) fieldIdx);
        if (ArraysCommons.findPosition(groupIds, (int) fieldIdx) != -1) {
            int pos = ArraysCommons.findPosition(groupIds, (int) fieldIdx);
            groupIds = ArraysCommons.remove(groupIds, pos);
        }
        if (ArraysCommons.findPosition(sortIds, (int) fieldIdx) != -1) {
            int pos = ArraysCommons.findPosition(sortIds, (int) fieldIdx);
            sortIds = ArraysCommons.remove(sortIds, pos);
            sortModes = ArraysCommons.remove(sortModes, pos);
        }
        if (ArraysCommons.findPosition(displayIds, (int) fieldIdx) != -1) {
            displayIds = ArraysCommons.remove(displayIds, ArraysCommons.findPosition(displayIds, (int) fieldIdx));
        }
        filters.remove((int) fieldIdx);
        for (int i = 0; i < groupIds.length; ++i) {
            if (groupIds[i] > fieldIdx) {
                groupIds[i] = groupIds[i] - 1;
            }
        }
        for (int i = 0; i < sortIds.length; ++i) {
            if (sortIds[i] > fieldIdx) {
                sortIds[i] = sortIds[i] - 1;
            }
        }
        for (int i = 0; i < displayIds.length; ++i) {
            if (displayIds[i] > fieldIdx) {
                displayIds[i] = displayIds[i] - 1;
            }
        }
        Map<Integer, FilterCriteria> filteredFieldsMap = new HashMap<>();
        for (Map.Entry<Integer, FilterCriteria> entry : filters.entrySet()) {
            if (entry.getKey() > fieldIdx) {
                filteredFieldsMap.put(entry.getKey() - 1, entry.getValue());
            }
        }
        filters.clear();
        filters.putAll(filteredFieldsMap);
    }

    public void setFieldsToGroup(int[] fieldsToGroupIds, FieldDescriptor[] allFields) {
        // calculate newSortingIdList
        List<Integer> sortingIdList = ArraysCommons.createIntegerList(sortIds);
        List<Integer> groupingIdList = ArraysCommons.createIntegerList(fieldsToGroupIds);
        List<Integer> sortingNotGroupingIdList = new ArrayList<>(sortingIdList);
        sortingNotGroupingIdList.removeAll(groupingIdList);
        List<Integer> sortingAndGroupingIdList = new ArrayList<>(sortingIdList);
        sortingAndGroupingIdList.removeAll(sortingNotGroupingIdList);
        List<Integer> groupingNotSortingIdList = new ArrayList<>(groupingIdList);
        groupingNotSortingIdList.removeAll(sortingAndGroupingIdList);

        List<Integer> newSortingIdList = new ArrayList<>(
                sortingAndGroupingIdList.size() + groupingNotSortingIdList.size() + sortingNotGroupingIdList.size());
        newSortingIdList.addAll(sortingAndGroupingIdList);
        newSortingIdList.addAll(groupingNotSortingIdList);
        newSortingIdList.addAll(sortingNotGroupingIdList);
        // end of calculation of newSortingIdList

        // delete groupable row
        Iterator<Integer> iterator = newSortingIdList.iterator();
        while (iterator.hasNext()) {
            Integer id = iterator.next();
            if (allFields[id].groupableByProcessId) {
                iterator.remove();
            }
        }

        // calculate newSortingModes
        boolean[] newSortingModes = new boolean[newSortingIdList.size()];
        for (int i = 0; i < newSortingIdList.size(); i++) {
            int pos = sortingIdList.indexOf(newSortingIdList.get(i));
            newSortingModes[i] = pos < 0 ? BatchPresentationConsts.ASC : sortModes[pos];
        }
        // end of calculation of newSortingModes
        sortIds = ArraysCommons.createIntArray(newSortingIdList);
        sortModes = newSortingModes;

        // calculate newGroupingIds
        List<Integer> newGroupingIdList = new ArrayList<>(sortingAndGroupingIdList.size() + groupingNotSortingIdList.size());
        newGroupingIdList.addAll(sortingAndGroupingIdList);
        newGroupingIdList.addAll(groupingNotSortingIdList);
        // end of calculate newGroupingIds

        // calculate new displayPositionIds
        List<Integer> newDisplayIdList = ArraysCommons.createIntegerList(displayIds);
        List<Integer> oldGroupIdList = ArraysCommons.createIntegerList(groupIds);
        for (Integer newGroupingId : newGroupingIdList) {
            if (!oldGroupIdList.contains(newGroupingId)) {
                newDisplayIdList.remove(newGroupingId);
            }
        }
        displayIds = ArraysCommons.createIntArray(newDisplayIdList);
        // end of calculate new displayPositionIds
        groupIds = ArraysCommons.createIntArray(newGroupingIdList);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(filters, groupIds, sortIds, sortModes, displayIds);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null || !(obj instanceof BatchPresentationFields)) {
            return false;
        }
        BatchPresentationFields f = (BatchPresentationFields) obj;
        return Objects.equal(filters, f.filters) && Arrays.equals(groupIds, f.groupIds) && Arrays.equals(sortIds, f.sortIds)
                && Arrays.equals(sortModes, f.sortModes) && Arrays.equals(displayIds, f.displayIds) && dynamics.equals(f.dynamics);
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this).add("displayIds", displayIds).add("sortIds", sortIds).add("sortModes", sortModes)
                .add("groupIds", groupIds).add("filters", filters).add("dynamics", dynamics).toString();
    }

    public static BatchPresentationFields createDefaultFields(ClassPresentationType type) {
        FieldDescriptor[] fieldDescriptors = type.getFields();
        BatchPresentationFields fields = new BatchPresentationFields();
        fields.groupIds = new int[0];
        int displayedFieldsCount = fieldDescriptors.length;
        for (FieldDescriptor fieldDescriptor : fieldDescriptors) {
            if (fieldDescriptor.isPrototype() || !(fieldDescriptor.isVisible() && fieldDescriptor.isShowable())) {
                displayedFieldsCount--;
            }
        }
        fields.displayIds = new int[displayedFieldsCount];
        for (int i = fieldDescriptors.length - 1; i >= 0; i--) {
            FieldDescriptor fieldDescriptor = fieldDescriptors[i];
            if (fieldDescriptor.isPrototype() || !(fieldDescriptor.isVisible() && fieldDescriptor.isShowable())) {
                continue;
            }
            fields.displayIds[--displayedFieldsCount] = i;
        }
        // Default sorting - creates array of sortIds,
        // which contains indexes of only(!) sorted fields - in order of sorting(!),
        // and synchronized array of sortModes.
        // All based on info from FieldDescriptors in current type ClassPresentation
        // (e.g. TaskClassPresentation, ProcessClassPresentation... - all can be found in ClassPresentations class).
        int sortedByDefaultFieldsCount = 0;
        for (FieldDescriptor field : fieldDescriptors) {
            if (field.defaultSortOrder > 0) {
                sortedByDefaultFieldsCount++;
            }
        }
        fields.sortIds = new int[sortedByDefaultFieldsCount];
        fields.sortModes = new boolean[sortedByDefaultFieldsCount];
        for (int i = 0; i < fieldDescriptors.length; i++) {
            if (fieldDescriptors[i].defaultSortOrder > 0) {
                try {
                    fields.sortIds[fieldDescriptors[i].defaultSortOrder - 1] = i;
                    fields.sortModes[fieldDescriptors[i].defaultSortOrder - 1] = fieldDescriptors[i].defaultSortMode;
                } catch (IndexOutOfBoundsException e) {
                    throw new InternalApplicationException("Sequence of indexes for default sorted fields in class " + type.name()
                            + "-ClassPresentation are broken with index " + fieldDescriptors[i].defaultSortOrder + ". "
                            + "Revise noted class please. " + "Sorted fields indexes must start with 1 and be exactly sequential.");
                }
            }
        }
        return fields;
    }
}
