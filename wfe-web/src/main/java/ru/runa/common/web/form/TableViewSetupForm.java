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
package ru.runa.common.web.form;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.apache.struts.action.ActionMapping;

import ru.runa.wfe.presentation.BatchPresentationConsts;

/**
 * Created on 26.01.2005
 * 
 * @struts:form name = "tableViewSetupForm"
 */
public class TableViewSetupForm extends IdsForm {
    private static final long serialVersionUID = -9191693914255904764L;

    public static final String VIEW_SIZE_NAME = "viewSize";
    public static final String FORM_NAME = "tableViewSetupForm";
    public static final String SAVE_AS_NAME = "saveAsBatchPresentationName";
    public static final String RETURN_ACTION = "returnAction";
    public static final String DISPLAY_POSITIONS = "displayPositionsIds";
    public static final String SORTING_POSITIONS = "sortPositionsIds";
    public static final String SORTING_MODE_NAMES = "sortingModeNames";
    public static final String ASC_SORTING_MODE = "ASC";
    public static final String DSC_SORTING_MODE = "DSC";
    public static final String BATCH_PRESENTATION_ID = "batchPresentationId";
    public static final String FILTER_CRITERIA = "fieldsToFilterCriterias";
    public static final String FILTER_CRITERIA_ID = "fieldsToFilterIds";
    public static final String GROUPING_POSITIONS = "fieldsToGroupIds";
    public static final String FILTER_POSITIONS = "filterPositionsIds";
    public static final String EDITABLE_FIELDS = "editableFieldsValues";
    public static final String SORTING_FIELD_IDS = "sortingIds";
    public static final String FILTERING_FIELD_IDS = "filteringIds";
    public static final String REMOVABLE_FIELD_IDS = "removableIds";

    private int viewSize;
    private String saveAsBatchPresentationName;
    private String batchPresentationId;

    private int[] displayPositionsIds;
    private int[] sortPositionsIds;
    private String[] sortingModeNames;
    private int[] fieldsToGroupIds;

    private int[] filterPositionsIds;
    private int[] fieldsToFilterIds;
    private String[] fieldsToFilterCriterias;

    private String returnAction;

    private String[] editableFieldsValues;

    private Long[] sortingIds;
    private Long[] filteringIds;
    private int[] removableIds;

    public void setSortingIds(Long[] ids) {
        sortingIds = ids;
    }

    public Long[] getFilteringIds() {
        return filteringIds;
    }

    public void setFilteringIds(Long[] ids) {
        filteringIds = ids;
    }

    public String getSaveAsBatchPresentationName() {
        return saveAsBatchPresentationName;
    }

    public void setSaveAsBatchPresentationName(String saveAsBatchPresentationName) {
        this.saveAsBatchPresentationName = saveAsBatchPresentationName;
    }

    public int[] getFieldsToGroupIds() {
        if (fieldsToGroupIds == null) {
            return new int[0];
        }
        return fieldsToGroupIds;
    }

    public void setFieldsToGroupIds(int[] fieldsToGroup) {
        fieldsToGroupIds = fieldsToGroup;
    }

    public int[] getRemovableIds() {
        if (removableIds == null) {
            return new int[0];
        }
        return removableIds;
    }

    public void setRemovableIds(int[] removableIds) {
        this.removableIds = removableIds;
    }

    public int getViewSize() {
        return viewSize;
    }

    public void setViewSize(int viewSize) {
        this.viewSize = viewSize;
    }

    public int[] getFieldsToFilterIds() {
        if (fieldsToFilterIds == null) {
            return new int[0];
        }
        /*
         * long[] ids = getIds(); // dumb convertion from long[] to int[] int[]
         * tmpFieldsToFilterIds = new int[fieldsToFilterIds.length]; for (int i
         * = 0; i < tmpFieldsToFilterIds.length; i++) { tmpFieldsToFilterIds[i]
         * = (int)ids[fieldsToFilterIds[i]]; } return tmpFieldsToFilterIds;
         */
        return fieldsToFilterIds;
    }

    public void setFieldsToFilterIds(int[] filterCriteriaIds) {
        fieldsToFilterIds = filterCriteriaIds;
    }

    public int[] getDisplayPositionsIds() {
        return createActualPositionIds(displayPositionsIds, getIds());
    }

    public void setDisplayPositionsIds(int[] positionIds) {
        displayPositionsIds = positionIds;
    }

    public int[] getSortPositionsIds() {
        return createActualPositionIds(sortPositionsIds, sortingIds);
    }

    public void setSortPositionsIds(int[] positionIds) {
        sortPositionsIds = positionIds;
    }

    public int[] getFilterPositionsIds() {
        return filterPositionsIds;
    }

    public void setFilterPositionsIds(int[] filterPositionsIds) {
        this.filterPositionsIds = filterPositionsIds;
    }

    @Override
    public void reset(ActionMapping mapping, HttpServletRequest request) {
        super.reset(mapping, request);
        displayPositionsIds = new int[0];
    }

    public boolean[] getSortingModes() {
        // dumb method to convert map to boolean[]
        int[] actualSortedPositionIds = getSortPositionsIds();
        boolean[] sortingModeIds = new boolean[actualSortedPositionIds.length];
        Map<Long, Integer> idPositionsMap = getIdPositionsMap(sortingIds);
        for (int i = 0; i < sortingModeIds.length; i++) {
            int index = idPositionsMap.get(new Long(actualSortedPositionIds[i]));
            if (sortingModeNames[index].equals(ASC_SORTING_MODE)) {
                sortingModeIds[i] = BatchPresentationConsts.ASC;
            } else if (sortingModeNames[index].equals(DSC_SORTING_MODE)) {
                sortingModeIds[i] = BatchPresentationConsts.DSC;
            }
        }
        return sortingModeIds;
    }

    private Map<Long, Integer> getIdPositionsMap(Long[] ids) {
        Map<Long, Integer> map = new HashMap<Long, Integer>();
        for (int i = 0; i < ids.length; i++) {
            map.put(new Long(ids[i]), new Integer(i));
        }
        return map;
    }

    public void setSortingModeNames(String[] sortingModeNames) {
        this.sortingModeNames = sortingModeNames;

    }

    public String getBatchPresentationId() {
        return batchPresentationId;
    }

    public void setBatchPresentationId(String batchPresentationId) {
        this.batchPresentationId = batchPresentationId;
    }

    private int[] createActualPositionIds(int[] positionIds, Long[] ids) {
        List<IdPos> specifiedPositionList = new ArrayList<IdPos>(ids.length);
        for (int i = 0; i < positionIds.length; i++) {
            if (positionIds[i] >= 0) {
                specifiedPositionList.add(new IdPos(ids[i], positionIds[i]));
            }
        }
        Collections.sort(specifiedPositionList);
        int[] result = new int[specifiedPositionList.size()];
        for (int i = 0; i < result.length; i++) {
            result[i] = (int) (specifiedPositionList.get(i)).getKey();
        }
        return result;
    }

    public String getReturnAction() {
        return returnAction;
    }

    public void setReturnAction(String forwardName) {
        returnAction = forwardName;
    }

    private static class IdPos implements Comparable<IdPos> {
        private final long key;

        private final int pos;

        public IdPos(long key, int pos) {
            this.key = key;
            this.pos = pos;
        }

        @Override
        public int compareTo(IdPos o) {
            int p = o.getPos();
            if (pos < p) {
                return -1;
            } else if (pos == p) {
                return 0;
            } else {
                return 1;
            }
        }

        public long getKey() {
            return key;
        }

        public int getPos() {
            return pos;
        }
    }

    /*
     * Map of types (Integer, String[]) Here Integer - fieldId in class
     * presentation String[] - filter criterias for field
     */
    public Map<Integer, String[]> getFieldsToFilterCriteriasMap() {
        Map<Integer, String[]> filterCriterias = new HashMap<Integer, String[]>();
        if (fieldsToFilterIds != null) {
            for (int i = 0; i < fieldsToFilterIds.length; i++) {
                filterCriterias.put(new Integer(fieldsToFilterIds[i]), getCriteriasForField(fieldsToFilterIds[i]));
            }
        }
        return filterCriterias;
    }

    private String[] getCriteriasForField(int fieldId) {
        ArrayList<String> criterias = new ArrayList<String>();
        for (int i = 0; i < filterPositionsIds.length; i++) {
            if (filterPositionsIds[i] == fieldId) {
                criterias.add(fieldsToFilterCriterias[i]);
            }
        }
        return criterias.toArray(new String[0]);
    }

    public void setFieldsToFilterCriterias(String[] filterCriterias) {
        fieldsToFilterCriterias = filterCriterias;
    }

    public String[] getFieldsToFilterCriterias() {
        return fieldsToFilterCriterias;
    }

    public void setEditableFieldsValues(String[] editableFieldsValues) {
        this.editableFieldsValues = editableFieldsValues;
    }

    public String[] getEditableFieldsValues() {
        return editableFieldsValues;
    }
}
