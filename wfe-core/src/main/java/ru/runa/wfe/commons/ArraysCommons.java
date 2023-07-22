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
package ru.runa.wfe.commons;

import java.lang.reflect.Array;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;

/**
 * Created on 02.12.2005
 */
public class ArraysCommons {

    public static Object[] sum(Object[] a, Object[] b) {
        if (a.getClass() != b.getClass()) {
            throw new IllegalArgumentException("a and b arrays types differes.");
        }
        Object[] result = (Object[]) Array.newInstance(a.getClass().getComponentType(), a.length + b.length);
        System.arraycopy(a, 0, result, 0, a.length);
        System.arraycopy(b, 0, result, a.length, b.length);
        return result;
    }

    public static List<Integer> createIntegerList(int[] array) {
        List<Integer> list = new ArrayList<Integer>(array.length);
        for (int i = 0; i < array.length; i++) {
            list.add(array[i]);
        }
        return list;
    }

    public static int[] createIntArray(Collection<Integer> collection) {
        int[] array = new int[collection.size()];
        int i = 0;
        for (Integer itereger : collection) {
            array[i++] = itereger;
        }
        return array;
    }

    public static Object[] createArrayValuesByIndex(Object[] values, int[] indexes) {
        Object[] result = (Object[]) Array.newInstance(values.getClass().getComponentType(), indexes.length);
        for (int i = 0; i < result.length; i++) {
            result[i] = values[indexes[i]];
        }
        return result;
    }

    public static List<Integer> createArrayListFilledIncrement(int count) {
        List<Integer> fieldIdList = new ArrayList<Integer>(count);
        for (int i = 0; i < count; i++) {
            fieldIdList.add(i);
        }
        return fieldIdList;
    }

    /**
     * Finds position of value in array of values.
     * 
     * @param values
     * @param value
     * @return position of value in array of value of -1 if position not found
     */
    public static int findPosition(int[] values, int value) {
        for (int i = 0; i < values.length; i++) {
            if (values[i] == value) {
                return i;
            }
        }
        return -1;
    }

    public static boolean contains(int[] values, int value) {
        return findPosition(values, value) == -1 ? false : true;
    }

    /**
     * Insets value into values array at given position.
     * 
     * @param values
     * @param position
     * @param value
     * @return new array
     */
    public static int[] insert(int[] values, int position, int value) {
        int[] result = new int[values.length + 1];
        result[position] = value;
        System.arraycopy(values, 0, result, 0, position);
        System.arraycopy(values, position, result, position + 1, values.length - position);
        return result;
    }

    /**
     * Removes value at possition from values array
     * 
     * @param values
     * @param position
     * @return
     */
    public static int[] remove(int[] values, int position) {
        int[] result = new int[values.length - 1];
        System.arraycopy(values, 0, result, 0, position);
        System.arraycopy(values, position + 1, result, position, values.length - position - 1);
        return result;
    }

    /**
     * Changes position of an element in array
     * 
     * @param values
     * @param oldPosition
     * @param newPosition
     * @return
     */
    public static int[] changePosition(int[] values, int oldPosition, int newPosition) {
        int value = values[oldPosition];
        int result[] = remove(values, oldPosition);
        return insert(result, newPosition, value);
    }

    /**
     * Insets value into values array at given position.
     * 
     * @param values
     * @param position
     * @param value
     * @return new array
     */
    public static boolean[] insert(boolean[] values, int position, boolean value) {
        boolean[] result = new boolean[values.length + 1];
        result[position] = value;
        System.arraycopy(values, 0, result, 0, position);
        System.arraycopy(values, position, result, position + 1, values.length - position);
        return result;
    }

    /**
     * Removes value at possition from values array
     * 
     * @param values
     * @param position
     * @return
     */
    public static boolean[] remove(boolean[] values, int position) {
        boolean[] result = new boolean[values.length - 1];
        System.arraycopy(values, 0, result, 0, position);
        System.arraycopy(values, position + 1, result, position, values.length - position - 1);
        return result;
    }

    /**
     * Changes position of an element in array
     * 
     * @param values
     * @param oldPosition
     * @param newPosition
     * @return
     */
    public static boolean[] changePosition(boolean[] values, int oldPosition, int newPosition) {
        boolean value = values[oldPosition];
        boolean result[] = remove(values, oldPosition);
        return insert(result, newPosition, value);
    }

    /**
     * Insets value into values array at given position.
     * 
     * @param values
     * @param position
     * @param value
     * @return new array
     */
    public static Object[] insert(Object[] values, int position, Object value) {
        Object[] result = (Object[]) Array.newInstance(values.getClass().getComponentType(), values.length + 1);
        result[position] = value;
        System.arraycopy(values, 0, result, 0, position);
        System.arraycopy(values, position, result, position + 1, values.length - position);
        return result;
    }

    /**
     * Removes value at possition from values array
     * 
     * @param values
     * @param position
     * @return
     */
    public static Object[] remove(Object[] values, int position) {
        Object[] result = (Object[]) Array.newInstance(values.getClass().getComponentType(), values.length - 1);
        System.arraycopy(values, 0, result, 0, position);
        System.arraycopy(values, position + 1, result, position, values.length - position - 1);
        return result;
    }

    /**
     * Fills array of objects with object
     * 
     * @param objects
     * @param object
     * @return
     */
    public static Object[] fillArray(Object[] objects, Object object) {
        Arrays.fill(objects, object);
        return objects;
    }
}
