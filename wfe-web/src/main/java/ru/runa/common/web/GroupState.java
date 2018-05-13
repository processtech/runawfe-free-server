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
package ru.runa.common.web;

import java.util.Arrays;
import java.util.List;

import ru.runa.common.web.html.TDBuilder;
import ru.runa.wfe.presentation.BatchPresentation;
import ru.runa.wfe.presentation.FieldDescriptor;

/**
 * @author Gritsenko_S Created 08.07.2005
 */
public class GroupState {

    public static enum StateType {
        TYPE_NO_MORE_ELEMENTS,
        TYPE_START_STATE,
        TYPE_EMPTY_STATE,
        TYPE_NORMAL_STATE
    };

    public static final GroupState STATE_NO_MORE_ELEMENTS = new GroupState(GroupState.StateType.TYPE_NO_MORE_ELEMENTS);

    private final BatchPresentation batchPresentation;

    private int groupIndex = 0;

    private boolean isGroupHeader = false;

    private boolean isVisible = false;

    private int itemIndex = 0;

    private List<? extends Object> items;

    private StateType stateType;

    private GroupState stateBefore = null;

    private int[] separatedValueNum = null;

    private final TDBuilder.Env env;
    private TDBuilder[] builders;

    public int getAdditionalColumn() {
        int retVal = 0;
        for (int n : separatedValueNum) {
            retVal += n;
        }
        return retVal;
    }

    private void setSeparatedValueNum(int[] val) {
        separatedValueNum = val.clone();
    }

    public static GroupState createStartState(List<? extends Object> items, BatchPresentation batchPresentation, TDBuilder[] builders,
            TDBuilder.Env env) {
        GroupState startState;
        if (items.size() > 0) {
            startState = new GroupState(items, batchPresentation, builders, env);
            startState.setGroupIndex(0);
            startState.setItemIndex(0);
            startState.setSeparatedValueNum(new int[batchPresentation.getGrouppedFields().length]);
            if (startState.isGroupHeader()) {
                startState.setVisible(batchPresentation.isGroupBlockExpanded(startState.getGroupId()));
            } else {
                startState.setVisible(true);
            }
            startState.setStateType(GroupState.StateType.TYPE_START_STATE);
        } else {
            startState = GroupState.STATE_NO_MORE_ELEMENTS;
        }
        return startState;
    }

    private GroupState(StateType stateType) {
        this.stateType = stateType;
        batchPresentation = null;
        env = null;
    }

    private GroupState(List<? extends Object> items, BatchPresentation batchPresentation, TDBuilder[] builders, TDBuilder.Env env) {
        this.items = items;
        this.batchPresentation = batchPresentation;
        stateType = GroupState.StateType.TYPE_NORMAL_STATE;
        this.env = env;
        this.builders = builders;
    }

    public StateType getStateType() {
        return stateType;
    }

    private void setStateType(StateType stateType) {
        this.stateType = stateType;
    }

    public int getCurrentGrouppedColumnIdx() {
        return batchPresentation.getGrouppedFields()[groupIndex].fieldIdx;
    }

    public String getGroupId() {
        StringBuilder buf = new StringBuilder();
        for (int i = 0; i <= groupIndex; i++) {
            buf.append(batchPresentation.getGrouppedFields()[i].dbSources[0].getValueDBPath(null, null));
            buf.append(((TDBuilder) batchPresentation.getGrouppedFields()[i].getTDBuilder()).getSeparatedValues(items.get(itemIndex), env)[separatedValueNum[i]]);
        }
        return buf.toString();
    }

    public String getCurrentGrouppedColumnValue() {
        return getCurrentGrouppedColumnValue(groupIndex);
    }

    public String getCurrentGrouppedColumnValue(int propertyIndex) {
        return ((TDBuilder) batchPresentation.getGrouppedFields()[propertyIndex].getTDBuilder()).getSeparatedValues(items.get(itemIndex), env)[separatedValueNum[propertyIndex]];
    }

    public int getItemIndex() {
        return itemIndex;
    }

    private void incrementItemIndex() {
        itemIndex++;
    }

    private void setItemIndex(int itemIndex) {
        this.itemIndex = itemIndex;
    }

    public int getGroupIndex() {
        return groupIndex;
    }

    /**
     * @return true if groupping nested all properties and this group state is item in last nesting level
     */
    private boolean isItemHasntVisibleFields() {
        return ((!isGroupHeader()) && (getGroupIndex() == batchPresentation.getAllFields().length));
    }

    /**
     * Increments group index in group state
     */
    private void incrementGroupIndex() {
        int sepCount = ((TDBuilder) batchPresentation.getGrouppedFields()[groupIndex].getTDBuilder()).getSeparatedValuesCount(items.get(itemIndex),
                env);
        separatedValueNum[groupIndex]++;
        if (separatedValueNum[groupIndex] != sepCount) {
            return;
        }

        separatedValueNum[groupIndex]--;
        setGroupIndex(groupIndex + 1);
    }

    /**
     * Sets group index in group state and changes groupHeader status if needed
     * 
     * @param groupIndex
     *            to set
     */
    private void setGroupIndex(int groupIndex) {
        this.groupIndex = groupIndex;
        isGroupHeader = (this.groupIndex < batchPresentation.getGrouppedFields().length);
    }

    public boolean isGroupHeader() {
        return isGroupHeader;
    }

    public boolean isVisible() {
        return isVisible;
    }

    private void setVisible(boolean isVisible) {
        this.isVisible = isVisible;
    }

    /**
     * Factiry method for creating next group state based on batchPresentation and collection of items
     */
    public GroupState buildNextState(BatchPresentation batchPresentation) {
        GroupState nextState = new GroupState(items, batchPresentation, builders, env);
        nextState.setGroupIndex(groupIndex);
        nextState.setSeparatedValueNum(separatedValueNum);
        if (!setItemIndexOnGroupStateIfPossible(nextState)) {
            return GroupState.STATE_NO_MORE_ELEMENTS;
        }
        if (!calcItemAndGroupIndexesIfPossible(nextState)) {
            return GroupState.STATE_NO_MORE_ELEMENTS;
        }
        updateVisibility(batchPresentation, nextState);
        return nextState;
    }

    /**
     * Find next item index to set in next group state
     * 
     * @param items
     * @param nextState
     * @return status, false means that no more items to iterate.
     */
    private boolean setItemIndexOnGroupStateIfPossible(GroupState nextState) {
        if (isGroupHeader()) {
            nextState.setItemIndex(getItemIndex());
        } else {
            nextState.setItemIndex(getItemIndex() + 1);
        }
        if (nextState.getItemIndex() < items.size()) {
            return true;
        }
        return false;
    }

    /**
     * Adjusts group and item indexes in next group state based on visibility and other parameters of this group state
     * 
     * @param items
     *            to iterate
     * @param nextState
     *            to adjust
     * @return status, false means that no more items to iterate.
     */
    private boolean calcItemAndGroupIndexesIfPossible(GroupState nextState) {
        if (isGroupHeader()) {
            if (isVisible()) {
                nextState.incrementGroupIndex();
                nextState.stateBefore = this;
            } else {
                if (!findItemIndexForNextStateBasedOnInvisibleGroupState(nextState)) {
                    return false;
                }
                if (nextState.getGroupIndex() > 0) {
                    findGroupIndexForNextStateBasedOnItemValues(nextState);
                }
            }
        } else {
            findGroupIndexForNextStateBasedOnItemValues(nextState);
        }
        return true;
    }

    /**
     * Sets next group state visible or no based on this group state and batchPresentation
     * 
     * @param batchPresentation
     *            to use
     * @param nextState
     *            to apply visibility
     */
    private void updateVisibility(BatchPresentation batchPresentation, GroupState nextState) {
        if (nextState.isGroupHeader()) {
            nextState.setVisible(batchPresentation.isGroupBlockExpanded(nextState.getGroupId()));
        } else {
            if (nextState.isItemHasntVisibleFields()) {
                nextState.setStateType(GroupState.StateType.TYPE_EMPTY_STATE);
            } else {
                nextState.setVisible(isVisible());
            }
        }
    }

    /**
     * Method must be invoked when current group state is: group header and not visible Then we iterate through items to find next item index for
     * which next group state would be visible
     */
    private boolean findItemIndexForNextStateBasedOnInvisibleGroupState(GroupState nextState) {
        while (isNextStateIsSubstateOfState(this, nextState)) {
            if (nextState.getItemIndex() + 1 == items.size()) {
                return false;
            }
            nextState.incrementItemIndex();
        }
        while (stateBefore != null) {
            if (isNextStateIsSubstateOfState(stateBefore, nextState)) {
                nextState.stateBefore = stateBefore;
                nextState.incrementGroupIndex();
                return true;
            }
            // rolling back
            stateBefore = stateBefore.stateBefore;
        }
        nextState.setSeparatedValueNum(new int[batchPresentation.getGrouppedFields().length]);
        nextState.setGroupIndex(0);
        return true;
    }

    /**
     * Find group index for next group state based on equality of values of items for which group states are building
     * 
     * @param nextState
     */
    private void findGroupIndexForNextStateBasedOnItemValues(GroupState nextState) {
        if (batchPresentation.getGrouppedFields().length > 0) {
            if (isNextStateIsSubstateOfState(this, nextState)) {
                nextState.stateBefore = this;
                nextState.incrementGroupIndex();
                return;
            }
            while (stateBefore != null) {
                if (isNextStateIsSubstateOfState(stateBefore, nextState)) {
                    nextState.stateBefore = stateBefore;
                    nextState.incrementGroupIndex();
                    return;
                }
                stateBefore = stateBefore.stateBefore;
            }
            nextState.setSeparatedValueNum(new int[batchPresentation.getGrouppedFields().length]);
            nextState.setGroupIndex(0);
        }
    }

    private boolean isNextStateIsSubstateOfState(GroupState state, GroupState nextState) {
        if (!state.isGroupHeader) {
            return false;
        }
        nextState.setSeparatedValueNum(state.separatedValueNum);
        nextState.setGroupIndex(state.getGroupIndex());
        for (int i = 0; i < state.getGroupIndex(); ++i) {
            TDBuilder tdBuilder = (TDBuilder) batchPresentation.getGrouppedFields()[i].getTDBuilder();
            if (!Arrays.deepEquals(tdBuilder.getSeparatedValues(items.get(state.itemIndex), env),
                    tdBuilder.getSeparatedValues(items.get(nextState.itemIndex), env))) {
                return false;
            }
        }
        String[] stateSeparatedValues = ((TDBuilder) batchPresentation.getGrouppedFields()[state.getGroupIndex()].getTDBuilder()).getSeparatedValues(
                items.get(state.itemIndex), env);
        String[] nextSeparatedValues = ((TDBuilder) batchPresentation.getGrouppedFields()[nextState.getGroupIndex()].getTDBuilder())
                .getSeparatedValues(items.get(nextState.itemIndex), env);
        int subCount = state.separatedValueNum[state.getGroupIndex()];
        if (subCount >= stateSeparatedValues.length || subCount >= nextSeparatedValues.length) {
            return false;
        }
        for (int i = 0; i <= subCount; ++i) {
            String currentValue = stateSeparatedValues[i] != null ? stateSeparatedValues[i] : "";
            String nextValue = nextSeparatedValues[i] != null ? nextSeparatedValues[i] : "";
            if (!currentValue.equals(nextValue)) {
                return false;
            }
        }
        return true;
    }

    static public int getMaxAdditionalCellsNum(BatchPresentation batchPresentation, List<?> items, TDBuilder.Env env) {
        FieldDescriptor[] grouppedFields = batchPresentation.getGrouppedFields();
        if (grouppedFields == null || grouppedFields.length == 0) {
            return 0;
        }

        int[] maxAddCells = new int[grouppedFields.length];
        for (int i = 0; i < grouppedFields.length; ++i) {
            maxAddCells[i] = 1;
            TDBuilder tdBuilder = (TDBuilder) grouppedFields[i].getTDBuilder();
            for (Object obj : items) {
                int num = tdBuilder.getSeparatedValuesCount(obj, env);
                if (num > maxAddCells[i]) {
                    maxAddCells[i] = num;
                }
            }
        }
        int retVal = -grouppedFields.length;
        for (int i = 0; i < grouppedFields.length; ++i) {
            retVal += maxAddCells[i];
        }
        return retVal;
    }
}
