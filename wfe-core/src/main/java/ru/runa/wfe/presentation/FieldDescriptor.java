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

import java.sql.Date;
import java.util.Arrays;
import java.util.Calendar;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import ru.runa.wfe.InternalApplicationException;
import ru.runa.wfe.commons.ClassLoaderUtil;
import ru.runa.wfe.presentation.filter.FilterCriteria;

import com.google.common.base.Objects;

/**
 * Description for field, available via {@link ClassPresentation}. Contains
 * almost all aspects of field behavior.
 */
public class FieldDescriptor {
    private static final Log log = LogFactory.getLog(FieldDescriptor.class);

    /**
     * Struts property, which will be used to get field display name.<br/>
     * ATTENTION: If this field contains {@link ClassPresentation} editable or
     * removable prefix, it must be treated in special way (see
     * {@link ClassPresentation} for more details).
     */
    public final String displayName;

    /**
     * Field type as class name (i. e. String.class.getName()). Used to get
     * appreciate {@link FilterCriteria} and FilterTDFormatter (see web
     * project). So, filter representation is depends on this field.
     * {@link Calendar} will be created for {@link Date}, editor field for
     * String and so on.
     */
    public final String fieldType;

    /**
     * Flag, equals true, if this field can be grouped or sorted; false
     * otherwise.
     */
    public final boolean isSortable;

    /**
     * The sort order, if the field is used for default batch sorting.
     * Sorted fields indexes must start with 1 and be exactly sequential. 
     * Are set to -1 if not participate in default sorting.
     */
    public final int defaultSortOrder;
    private static final int notUsedSortOrder = -1; 

    /**
     * The sort mode if the field is used for default batch sorting.  
     * BatchPresentationConsts.ASC or BatchPresentationConsts.DSC considered in place.
     */
    public final boolean defaultSortMode;

    /**
     * Field filter mode.
     */
    public final FieldFilterMode filterMode;

    /**
     * Preferred way to get value of this field and show this field in web
     * interface. (Class name)
     */
    public final String tdBuilder;

    /**
     * Parameters, passed to tdBuilder constructor.
     */
    public final Object[] tdBuilderParams;

    /**
     * Components, to access field values from HQL/SQL. If more then one
     * components supplied, then first component must describe access to base
     * class and other components must describe access to inherited objects.
     */
    public final DBSource[] dbSources;

    /**
     * If this field is true, JoinExpression (field.getJoinExpression()) is
     * applied only if this field is sorting/filtering/grouping. TODO: It's
     * seems, what all weak field is a field with persistent class, differs from
     * root and vice verse.
     */
    public final boolean isWeakJoin;

    /**
     * Ordinal field index in {@link BatchPresentation}. All fields in
     * {@link ClassPresentation} has -1, but {@link BatchPresentation} creates
     * fields with indexes using createConcretteField.
     */
    public final int fieldIdx;

    /**
     * Field display and HQL/SQL affecting state.
     */
    public final FieldState fieldState;

    /**
     * Creates field description.
     * 
     * @param displayName
     *            Struts property, which will be used to get field display name.
     * @param fieldType
     *            Field type as class name (i. e. String.class.getName()).
     * @param dbSources
     *            Components, to access field values from HQL/SQL.
     * @param isSortable
     *            Flag, equals true, if this field can be grouped or sorted;
     *            false otherwise.
     * @param filterMode
     *            Field filter mode.
     * @param fieldState
     *            Field display and HQL/SQL affecting state.
     */
    public FieldDescriptor(String displayName, String fieldType, DBSource dbSources, boolean isSortable, 
    		FieldFilterMode filterMode, FieldState fieldState) {
        this(displayName, fieldType, new DBSource[] { dbSources }, isSortable, notUsedSortOrder, BatchPresentationConsts.ASC, 
        		filterMode, null, null, false, -1, fieldState);
    }

    /**
     * Creates field description.
     * 
     * @param displayName
     *            Struts property, which will be used to get field display name.
     * @param fieldType
     *            Field type as class name (i. e. String.class.getName()).
     * @param dbSource
     *            Component, to access field values from HQL/SQL.
     * @param isSortable
     *            Flag, equals true, if this field can be grouped or sorted;
     *            false otherwise.
     * @param filterMode
     *            Field filter mode.
     * @param tdBuilder
     *            Preferred way to get value of this field and show this field
     *            in web interface. (Class name)
     * @param tdBuilderParams
     *            Parameters, passed to tdBuilder constructor.
     */
    public FieldDescriptor(String displayName, String fieldType, DBSource dbSource, boolean isSortable,
    		FieldFilterMode filterMode, String tdBuilder, Object[] tdBuilderParams) {
        this(displayName, fieldType, new DBSource[] { dbSource }, isSortable, notUsedSortOrder, BatchPresentationConsts.ASC, 
        		filterMode, tdBuilder, tdBuilderParams, false, -1, null);
    }
    
    /**
     * Creates field description.
     * 
     * @param displayName
     *            Struts property, which will be used to get field display name.
     * @param fieldType
     *            Field type as class name (i. e. String.class.getName()).
     * @param dbSource
     *            Component, to access field values from HQL/SQL.
     * @param isSortable
     *            Flag, equals true, if this field can be grouped or sorted;
     *            false otherwise.
     * @param defaultSortOrder
     * 			  The sort order, if the field is used for default batch sorting. 
     * @param defaultSortMode
     * 		      The sort mode, if the field is used for default batch sorting.
     * @param filterMode
     *            Field filter mode.
     * @param tdBuilder
     *            Preferred way to get value of this field and show this field
     *            in web interface. (Class name)
     * @param tdBuilderParams
     *            Parameters, passed to tdBuilder constructor.
     */
    public FieldDescriptor(String displayName, String fieldType, DBSource dbSource, boolean isSortable, int defaultSortOrder, boolean defaultSortMode, 
    		FieldFilterMode filterMode, String tdBuilder, Object[] tdBuilderParams) {
        this(displayName, fieldType, new DBSource[] { dbSource }, isSortable, defaultSortOrder, defaultSortMode, 
        		filterMode, tdBuilder, tdBuilderParams, false, -1, null);
    }


    /**
     * Creates field description.
     * 
     * @param displayName
     *            Struts property, which will be used to get field display name.
     * @param fieldType
     *            Field type as class name (i. e. String.class.getName()).
     * @param dbSource
     *            Component, to access field values from HQL/SQL.
     * @param isSortable
     *            Flag, equals true, if this field can be grouped or sorted;
     *            false otherwise.
     * @param filterMode
     *            Field filter mode.
     * @param tdBuilder
     *            Preferred way to get value of this field and show this field
     *            in web interface. (Class name)
     * @param tdBuilderParams
     *            Parameters, passed to tdBuilder constructor.
     * @param isWeakJoin
     *            If this field is true, JoinExpression
     *            (field.getJoinExpression()) is applied only if this field is
     *            sorting/filtering/grouping.
     */
    public FieldDescriptor(String displayName, String fieldType, DBSource dbSource, boolean isSortable, 
    		FieldFilterMode filterMode, String tdBuilder, Object[] tdBuilderParams, boolean isWeakJoin) {
        this(displayName, fieldType, new DBSource[] { dbSource }, isSortable, notUsedSortOrder, BatchPresentationConsts.ASC, 
        		filterMode, tdBuilder, tdBuilderParams, isWeakJoin, -1, null);
    }

    /**
     * Creates field description.
     * 
     * @param displayName
     *            Struts property, which will be used to get field display name.
     * @param fieldType
     *            Field type as class name (i. e. String.class.getName()).
     * @param dbSources
     *            Components, to access field values from HQL/SQL.
     * @param isSortable
     *            Flag, equals true, if this field can be grouped or sorted;
     *            false otherwise.
     * @param filterMode
     *            Field filter mode.
     * @param tdBuilder
     *            Preferred way to get value of this field and show this field
     *            in web interface. (Class name)
     * @param tdBuilderParams
     *            Parameters, passed to tdBuilder constructor.
     */
    public FieldDescriptor(String displayName, String fieldType, DBSource[] dbSources, boolean isSortable,  
    		FieldFilterMode filterMode, String tdBuilder, Object[] tdBuilderParams) {
        this(displayName, fieldType, dbSources, isSortable, notUsedSortOrder, BatchPresentationConsts.ASC, 
        		filterMode, tdBuilder, tdBuilderParams, false, -1, null);
    }

    /**
     * Creates field description.
     * 
     * @param displayName
     *            Struts property, which will be used to get field display name.
     * @param fieldType
     *            Field type as class name (i. e. String.class.getName()).
     * @param dbSources
     *            Components, to access field values from HQL/SQL.
     * @param isSortable
     *            Flag, equals true, if this field can be grouped or sorted;
     *            false otherwise.
     * @param filterMode
     *            Field filter mode.
     * @param tdBuilder
     *            Preferred way to get value of this field and show this field
     *            in web interface. (Class name)
     * @param tdBuilderParams
     *            Parameters, passed to tdBuilder constructor.
     * @param isWeakJoin
     *            If this field is true, JoinExpression
     *            (field.getJoinExpression()) is applied only if this field is
     *            sorting/filtering/grouping.
     */
    public FieldDescriptor(String displayName, String fieldType, DBSource[] dbSources, boolean isSortable,
    		FieldFilterMode filterMode, String tdBuilder, Object[] tdBuilderParams, boolean isWeakJoin) {
        this(displayName, fieldType, dbSources, isSortable, notUsedSortOrder, BatchPresentationConsts.ASC, 
        		filterMode, tdBuilder, tdBuilderParams, isWeakJoin, -1, null);
    }

    /**
     * Creates field description.
     * 
     * @param displayName
     *            Struts property, which will be used to get field display name.
     * @param fieldType
     *            Field type as class name (i. e. String.class.getName()).
     * @param dbSources
     *            Components, to access field values from HQL/SQL.
     * @param isSortable
     *            Flag, equals true, if this field can be grouped or sorted;
     *            false otherwise.
     * @param filterMode
     *            Field filter mode.
     * @param tdBuilder
     *            Preferred way to get value of this field and show this field
     *            in web interface. (Class name)
     * @param tdBuilderParams
     *            Parameters, passed to tdBuilder constructor.
     * @param isWeakJoin
     *            If this field is true, JoinExpression
     *            (field.getJoinExpression()) is applied only if this field is
     *            sorting/filtering/grouping.
     * @param fieldIdx
     *            Ordinal field index in {@link BatchPresentation}.
     * @param fieldState
     *            Field display and HQL/SQL affecting state.
     */
    private FieldDescriptor(String displayName, String fieldType, DBSource[] dbSources, boolean isSortable, int defaultSortOrder, boolean defaultSortMode, 
    		FieldFilterMode filterMode, String tdBuilder, Object[] tdBuilderParams, boolean isWeakJoin, int fieldIdx, FieldState fieldState) {
        this.displayName = displayName;
        this.fieldType = fieldType;
        this.isSortable = isSortable;
        this.defaultSortOrder = defaultSortOrder;
        this.defaultSortMode = defaultSortMode;
        this.filterMode = filterMode;
        this.tdBuilder = tdBuilder;
        this.tdBuilderParams = tdBuilderParams;
        this.dbSources = dbSources;
        this.isWeakJoin = isWeakJoin;
        this.fieldIdx = fieldIdx;
        this.fieldState = fieldState == null ? loadFieldState(displayName) : fieldState;
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
        return Objects.equal(displayName, other.displayName) && Objects.equal(fieldType, other.fieldType)
                && Arrays.equals(dbSources, other.dbSources);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(displayName);
    }

    /**
     * Creates {@link FieldDescriptor} instance with same parameters as current,
     * but with provided field index.
     * 
     * @param fieldIdx
     *            Index, assigned to field.
     * @return {@link FieldDescriptor} instance with provided index.
     */
    public FieldDescriptor createConcreteField(int fieldIdx) {
        return new FieldDescriptor(displayName, fieldType, dbSources, isSortable, defaultSortOrder, defaultSortMode, filterMode, tdBuilder, tdBuilderParams, isWeakJoin, fieldIdx,
                fieldState);
    }

    /**
     * Creates removable field for editable field. If this method called not to
     * editable field, null will be returned.
     * 
     * @param value
     *            Value, inserted by user to editable field editor.
     * @param fieldIdx
     *            New removable field index.
     * @return {@link FieldDescriptor} for removable field, constructed based on
     *         editable field.
     */
    public FieldDescriptor createConcreteEditableField(String value, int fieldIdx) {
        if (!displayName.startsWith(ClassPresentation.editable_prefix)) {
            throw new InternalApplicationException("Field '" + displayName + "' is not editable");
        }
        return new FieldDescriptor(displayName.replace(ClassPresentation.editable_prefix, ClassPresentation.removable_prefix) + ":" + value,
                fieldType, dbSources, isSortable, defaultSortOrder, defaultSortMode, filterMode, tdBuilder, tdBuilderParams, isWeakJoin, fieldIdx, fieldState);
    }

    private Object loadedTDBuilder;

    /**
     * Returns preferred object to display this field value in web interface.
     * 
     * @return TDBuilder instance.
     */
    public Object getTDBuilder() {
        if (loadedTDBuilder == null) {
            loadedTDBuilder = loadTDBuilder();
        }
        return loadedTDBuilder;
    }

    /**
     * Loads preferred object to display this field value in web interface.
     * 
     * @return TDBuilder instance.
     */
    private Object loadTDBuilder() {
        Object builder = null;
        if (displayName.startsWith(ClassPresentation.removable_prefix)) {
            Object[] params = new Object[tdBuilderParams.length + 1];
            for (int idx = 0; idx < tdBuilderParams.length; ++idx) {
                params[idx] = tdBuilderParams[idx];
            }
            params[params.length - 1] = displayName.substring(displayName.lastIndexOf(':') + 1);
            builder = ClassLoaderUtil.instantiate(tdBuilder, params);
        } else if (displayName.startsWith(ClassPresentation.editable_prefix)) {
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

    /**
     * Load field state from properties file. If property loading fails, return
     * ENABLED.
     * 
     * @param displayName
     *            Field display name.
     * @return Field state, loaded from properties file.
     */
    private FieldState loadFieldState(String displayName) {
        try {
            return new ClassPresentationResources().getFieldState(displayName);
        } catch (Exception e) {
            log.warn("Can't load state for field " + displayName, e);
        }
        return FieldState.ENABLED;
    }
}
