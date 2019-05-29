package ru.runa.wfe.presentation.filter;

import com.google.common.collect.Maps;
import java.util.Date;
import java.util.Map;
import ru.runa.wfe.InternalApplicationException;
import ru.runa.wfe.commons.ClassLoaderUtil;
import ru.runa.wfe.presentation.BatchPresentation;
import ru.runa.wfe.var.ArchivedVariable;
import ru.runa.wfe.var.CurrentVariable;

/**
 * 
 * Created on 12.02.2007
 * 
 */
public class FilterCriteriaFactory {

    private static Map<Class<?>, Class<? extends FilterCriteria>> filterCriterias = Maps.newHashMap();

    static {
        filterCriterias.put(String.class, StringFilterCriteria.class);
        filterCriterias.put(Integer.class, LongFilterCriteria.class);
        filterCriterias.put(Date.class, DateFilterCriteria.class);
        filterCriterias.put(CurrentVariable.class, StringFilterCriteria.class);
        filterCriterias.put(ArchivedVariable.class, StringFilterCriteria.class);
    }

    public static FilterCriteria createFilterCriteria(BatchPresentation batchPresentation, int fieldIndex) {
        String fieldClassName = batchPresentation.getAllFields()[fieldIndex].fieldType;
        return createFilterCriteria(ClassLoaderUtil.loadClass(fieldClassName));
    }

    public static FilterCriteria createFilterCriteria(final Class<?> fieldClass) {
        if (FilterCriteria.class.isAssignableFrom(fieldClass)) {
            return (FilterCriteria) ClassLoaderUtil.instantiate(fieldClass);
        }
        Class<?> testClass = fieldClass;
        while (testClass != Object.class) {
            Class<? extends FilterCriteria> criteriaClass = filterCriterias.get(testClass);
            if (criteriaClass != null) {
                return ClassLoaderUtil.instantiate(criteriaClass);
            }
            testClass = testClass.getSuperclass();
        }
        throw new InternalApplicationException("No FilterCriteria found for " + fieldClass);
    }
}
