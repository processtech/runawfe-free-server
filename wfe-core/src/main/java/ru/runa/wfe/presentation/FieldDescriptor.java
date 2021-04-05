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
package ru.runa.wfe.presentation;

import com.google.common.base.Objects;
import java.sql.Date;
import java.util.Arrays;
import java.util.Calendar;
import ru.runa.wfe.InternalApplicationException;
import ru.runa.wfe.commons.ClassLoaderUtil;
import ru.runa.wfe.presentation.filter.FilterCriteria;

/**
 * Description for field, available via {@link ClassPresentation}. Contains almost all aspects of field behavior.
 */
public class FieldDescriptor {

    /**
     * Field name also used for struts property generation.
     */
    public final String name;

    /**
     * Field type as class name (i. e. String.class.getName()). Used to get appreciate {@link FilterCriteria} and FilterTDFormatter (see web project).
     * So, filter representation is depends on this field. {@link Calendar} will be created for {@link Date}, editor field for String and so on.
     */
    public final String fieldType;

    /**
     * Flag, equals true, if this field visible by default; false otherwise.
     */
    private boolean visible = true;

    /**
     * Flag, equals true, if this field can be grouped or sorted; false otherwise.
     */
    public final boolean sortable;

    /**
     * Flag, equals true, if this field can be showed; false otherwise.
     */
    private boolean showable = true;

    /**
     * The sort order, if the field is used for default batch sorting. Sorted fields indexes must start with 1 and be exactly sequential. Are set to
     * -1 if not participate in default sorting.
     */
    public final int defaultSortOrder;
    private static final int notUsedSortOrder = -1;

    /**
     * The sort mode if the field is used for default batch sorting. BatchPresentationConsts.ASC or BatchPresentationConsts.DSC considered in place.
     */
    public final boolean defaultSortMode;

    /**
     * If field has this prefix, when it must be showed as check box and it use for grouping.
     * Mapped directly to batch_presentation.process.group_by_id; so currently its name implies this.
     * See ClassPresentation.filterable_prefix in old code version.
     */
    public boolean groupableByProcessId = false;

    /**
     * Field filter mode.
     */
    public final FieldFilterMode filterMode;

    /**
     * Preferred way to get value of this field and show this field in web interface. (Class name)
     */
    public final String tdBuilder;

    /**
     * Parameters, passed to tdBuilder constructor.
     */
    public final Object[] tdBuilderParams;

    /**
     * Components, to access field values from HQL/SQL. If more then one components supplied, then first component must describe access to base class
     * and other components must describe access to inherited objects.
     */
    public final DbSource[] dbSources;

    /**
     * Ordinal field index in {@link BatchPresentation}. All fields in {@link ClassPresentation} has -1, but {@link BatchPresentation} creates fields
     * with indexes using createConcretteField.
     */
    public final int fieldIdx;

    /**
     * Field display and HQL/SQL affecting state.
     */
    public final FieldState fieldState;

    /**
     * Means that this field can be used as variable filter.
     * See ClassPresentation.editable_prefix in old code version.
     */
    public boolean variablePrototype = false;

    public String variableValue;

    /**
     * Means that this field is used as variable filter.
     * See ClassPresentation.removable_prefix in old code version.
     */
    public boolean filterByVariable = false;

    public FieldDescriptor(String name, String fieldType, DbSource dbSource, boolean sortable, FieldFilterMode filterMode,
            FieldState fieldState) {
        this(name, fieldType, new DbSource[] { dbSource }, sortable, notUsedSortOrder, BatchPresentationConsts.ASC, filterMode, null, null,
                -1, fieldState);
    }

    public FieldDescriptor(String name, String fieldType, DbSource dbSource, boolean sortable, FieldFilterMode filterMode, String tdBuilder,
            Object[] tdBuilderParams) {
        this(name, fieldType, new DbSource[] { dbSource }, sortable, notUsedSortOrder, BatchPresentationConsts.ASC, filterMode, tdBuilder,
                tdBuilderParams, -1, null);
    }

    public FieldDescriptor(String name, String fieldType, DbSource dbSource, boolean sortable, int defaultSortOrder, boolean defaultSortMode,
            FieldFilterMode filterMode, String tdBuilder, Object[] tdBuilderParams) {
        this(name, fieldType, new DbSource[] { dbSource }, sortable, defaultSortOrder, defaultSortMode, filterMode, tdBuilder,
                tdBuilderParams, -1, null);
    }

    public FieldDescriptor(String name, String fieldType, DbSource[] dbSources, boolean sortable, FieldFilterMode filterMode,
            String tdBuilder, Object[] tdBuilderParams) {
        this(name, fieldType, dbSources, sortable, notUsedSortOrder, BatchPresentationConsts.ASC, filterMode, tdBuilder, tdBuilderParams, -1,
                null);
    }

    private FieldDescriptor(String name, String fieldType, DbSource[] dbSources, boolean sortable, int defaultSortOrder,
            boolean defaultSortMode, FieldFilterMode filterMode, String tdBuilder, Object[] tdBuilderParams, int fieldIdx, FieldState fieldState) {
        this.name = name;
        this.fieldType = fieldType;
        this.sortable = sortable;
        this.defaultSortOrder = defaultSortOrder;
        this.defaultSortMode = defaultSortMode;
        this.filterMode = filterMode;
        this.tdBuilder = tdBuilder;
        this.tdBuilderParams = tdBuilderParams;
        this.dbSources = dbSources;
        this.fieldIdx = fieldIdx;
        this.fieldState = fieldState == null ? FieldState.ENABLED : fieldState;
        if (filterMode == FieldFilterMode.DATABASE_ID_RESTRICTION && sortable) {
            throw new InternalApplicationException("DATABASE_ID_RESTRICTION must not be used on filterable fields.");
        }
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null || !(obj instanceof FieldDescriptor)) {
            return false;
        }
        if (obj == this) {
            return true;
        }
        FieldDescriptor other = (FieldDescriptor) obj;
        return Objects.equal(name, other.name) && Objects.equal(fieldType, other.fieldType)
                && Arrays.equals(dbSources, other.dbSources) && Objects.equal(variableValue, other.variableValue);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(name);
    }

    /**
     * Creates {@link FieldDescriptor} instance with same parameters as current, but with provided field index.
     *
     * @param fieldIdx
     *            Index, assigned to field.
     * @return {@link FieldDescriptor} instance with provided index.
     */
    public FieldDescriptor createConcreteField(int fieldIdx) {
        return new FieldDescriptor(name, fieldType, dbSources, sortable, defaultSortOrder, defaultSortMode, filterMode, tdBuilder, tdBuilderParams,
                fieldIdx, fieldState).setVisible(visible).setShowable(showable).setGroupableByProcessId(groupableByProcessId).setVariablePrototype(variablePrototype);
    }

    /**
     * Creates filter by variable field. If this method called not to variable prototype field, null will be returned.
     *
     * @param variableValue
     *            Value, inserted by user to editable field editor.
     * @param fieldIdx
     *            New field index.
     */
    public FieldDescriptor createConcreteEditableField(String variableValue, int fieldIdx) {
        if (!variablePrototype) {
            throw new InternalApplicationException("Field '" + name + "' is not a prototype");
        }
        FieldDescriptor fieldDescriptor = new FieldDescriptor(name, fieldType, dbSources, sortable, defaultSortOrder, defaultSortMode, filterMode,
                tdBuilder, tdBuilderParams, fieldIdx, fieldState).setFilterByVariable(true);
        fieldDescriptor.variableValue = variableValue;
        return fieldDescriptor;
    }

    private Object loadedTdBuilder;

    /**
     * Returns preferred object to display this field value in web interface.
     *
     * @return TdBuilder instance.
     */
    public Object getTdBuilder() {
        if (loadedTdBuilder == null) {
            loadedTdBuilder = loadTdBuilder();
        }
        return loadedTdBuilder;
    }

    public boolean isVisible() {
        return visible;
    }

    public FieldDescriptor setVisible(boolean visible) {
        this.visible = visible;
        return this;
    }

    public boolean isShowable() {
        return showable;
    }

    public FieldDescriptor setShowable(boolean showable) {
        this.showable = showable;
        return this;
    }

    public FieldDescriptor setGroupableByProcessId(boolean groupableByProcessId) {
        this.groupableByProcessId = groupableByProcessId;
        return this;
    }

    public FieldDescriptor setVariablePrototype(boolean variablePrototype) {
        this.variablePrototype = variablePrototype;
        return this;
    }

    public FieldDescriptor setFilterByVariable(boolean filterByVariable) {
        this.filterByVariable = filterByVariable;
        return this;
    }

    /**
     * Loads preferred object to display this field value in web interface.
     *
     * @return TdBuilder instance.
     */
    private Object loadTdBuilder() {
        Object builder = null;
        if (filterByVariable) {
            Object[] params = new Object[tdBuilderParams.length + 1];
            for (int idx = 0; idx < tdBuilderParams.length; ++idx) {
                params[idx] = tdBuilderParams[idx];
            }
            params[params.length - 1] = variableValue;
            builder = ClassLoaderUtil.instantiate(tdBuilder, params);
        } else if (variablePrototype) {
            Object[] params = new Object[tdBuilderParams.length + 1];
            for (int idx = 0; idx < tdBuilderParams.length; ++idx) {
                params[idx] = tdBuilderParams[idx];
            }
            params[params.length - 1] = "";
            builder = ClassLoaderUtil.instantiate(tdBuilder, params);
        } else {
            builder = ClassLoaderUtil.instantiate(tdBuilder, tdBuilderParams);
        }
        return builder;
    }

}
