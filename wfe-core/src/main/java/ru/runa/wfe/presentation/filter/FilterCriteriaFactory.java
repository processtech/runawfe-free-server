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
package ru.runa.wfe.presentation.filter;

import java.util.Date;
import java.util.Map;

import ru.runa.wfe.InternalApplicationException;
import ru.runa.wfe.commons.ClassLoaderUtil;
import ru.runa.wfe.presentation.BatchPresentation;
import ru.runa.wfe.var.Variable;

import com.google.common.collect.Maps;

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
        filterCriterias.put(Long.class, LongFilterCriteria.class);
        filterCriterias.put(Date.class, DateFilterCriteria.class);
        filterCriterias.put(Variable.class, StringFilterCriteria.class);
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
