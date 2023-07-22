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

import java.lang.reflect.InvocationTargetException;
import java.util.Comparator;

import org.apache.commons.beanutils.PropertyUtils;

import ru.runa.wfe.user.Executor;

public class BatchPresentationComparator implements Comparator<Executor> {

    private final BatchPresentation batchPresentation;

    public BatchPresentationComparator(BatchPresentation batchPresentation) {
        this.batchPresentation = batchPresentation;
    }

    @Override
    public int compare(Executor o1, Executor o2) {
        int result = 0;
        FieldDescriptor[] sortingFields = batchPresentation.getSortedFields();
        boolean[] sortingOrder = batchPresentation.getFieldsToSortModes();

        try {
            for (int i = 0; i < sortingFields.length; i++) {
                Comparable<Comparable<?>> field1 = (Comparable<Comparable<?>>) PropertyUtils.getProperty(o1,
                        sortingFields[i].dbSources[0].getValueDBPath(null, null));
                Comparable<Comparable<?>> field2 = (Comparable<Comparable<?>>) PropertyUtils.getProperty(o2,
                        sortingFields[i].dbSources[0].getValueDBPath(null, null));
                result = field1.compareTo(field2);
                if (!sortingOrder[i]) {
                    result *= -1;
                }
                if (result != 0) {
                    break;
                }
            }
        } catch (NoSuchMethodException e) {
            throw new IllegalArgumentException(o1 + " of class" + o1.getClass() + ", " + o2 + " of class" + o2.getClass());
        } catch (IllegalAccessException e) {
            throw new IllegalArgumentException(o1 + " of class" + o1.getClass() + ", " + o2 + " of class" + o2.getClass());
        } catch (InvocationTargetException e) {
            throw new IllegalArgumentException(o1 + " of class" + o1.getClass() + ", " + o2 + " of class" + o2.getClass());
        }
        return result;
    }
}
