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

package ru.runa.junit;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import junit.framework.Assert;

import com.google.common.collect.Lists;

/*
 * ArrayAssert class proposed by Vladimir Bossicard see details at http://sourceforge.net/mailarchive/forum.php?thread_id=113876&forum_id=3274
 */
public class ArrayAssert extends Assert {

    public static void assertEqualArrays(String message, byte[] expected, byte[] actual) {
        if (Arrays.equals(expected, actual)) {
            return;
        }
        String formatted = "";
        if (message != null) {
            formatted = message + " ";
        }
        assertNotNull(formatted + "expected array: <not null> but was <null>", actual);
        assertNotNull(formatted + "expected array: <null> but was <not null>", expected);

        assertEquals(formatted + "[array length] ", expected.length, actual.length);
        for (int i = 0; i < expected.length; i++) {
            assertEquals(formatted + "[position " + i + "]", expected[i], actual[i]);
        }
    }

//    static public void assertEqualArrays(Object message, Object[] expected, Object[] actual) {
//        if (Arrays.equals(expected, actual)) {
//            return;
//        }
//
//        String formatted = "";
//        if (message != null) {
//            formatted = message + " ";
//        }
//        assertNotNull(formatted + "expected array: <not null> but was <null>", actual);
//        assertNotNull(formatted + "expected array: <null> but was <not null>", expected);
//
//        assertEquals(formatted + "[array length] ", expected.length, actual.length);
//        for (int i = 0; i < expected.length; i++) {
//            assertEquals(formatted + "[position " + i + "]", expected[i], actual[i]);
//        }
//    }
//
//    static public void assertEqualArrays(Object message, int[] expected, int[] actual) {
//        if (Arrays.equals(expected, actual)) {
//            return;
//        }
//
//        String formatted = "";
//        if (message != null) {
//            formatted = message + " ";
//        }
//        assertNotNull(formatted + "expected array: <not null> but was <null>", actual);
//        assertNotNull(formatted + "expected array: <null> but was <not null>", expected);
//
//        assertEquals(formatted + "[array length] ", expected.length, actual.length);
//        for (int i = 0; i < expected.length; i++) {
//            assertEquals(formatted + "[position " + i + "]", expected[i], actual[i]);
//        }
//    }
//
//    static public void assertEqualArrays(Object message, long[] expected, long[] actual) {
//        if (Arrays.equals(expected, actual)) {
//            return;
//        }
//
//        String formatted = "";
//        if (message != null) {
//            formatted = message + " ";
//        }
//        assertNotNull(formatted + "expected array: <not null> but was <null>", actual);
//        assertNotNull(formatted + "expected array: <null> but was <not null>", expected);
//
//        assertEquals(formatted + "[array length] ", expected.length, actual.length);
//        for (int i = 0; i < expected.length; i++) {
//            assertEquals(formatted + "[position " + i + "]", expected[i], actual[i]);
//        }
//    }
//
//    static public void assertEqualArrays(Object message, boolean[] expected, boolean[] actual) {
//        if (Arrays.equals(expected, actual)) {
//            return;
//        }
//
//        String formatted = "";
//        if (message != null) {
//            formatted = message + " ";
//        }
//        assertNotNull(formatted + "expected array: <not null> but was <null>", actual);
//        assertNotNull(formatted + "expected array: <null> but was <not null>", expected);
//
//        assertEquals(formatted + "[array length] ", expected.length, actual.length);
//        for (int i = 0; i < expected.length; i++) {
//            assertEquals(formatted + "[position " + i + "]", expected[i], actual[i]);
//        }
//    }
//
//    static public void assertWeakEqualArrays(String message, Object[] expected, Object[] actual) {
//        if (Arrays.equals(expected, actual)) {
//            return;
//        }
//
//        String formatted = "";
//        if (message != null) {
//            formatted = message + " ";
//        }
//        assertNotNull(formatted + "expected array: <not null> but was <null>", actual);
//        assertNotNull(formatted + "expected array: <null> but was <not null>", expected);
//
//        assertEquals(formatted + "[array length] ", expected.length, actual.length);
//        int matchedElements = 0;
//        for (int i = 0; i < expected.length; i++) {
//            for (int j = 0; j < actual.length; j++) {
//                if (expected[i].equals(actual[j])) {
//                    matchedElements++;
//                    break;
//                }
//            }
//        }
//        assertEquals(formatted + "expected array: <" + ArrayAssert.arrayToString(expected) + "> but was <" + ArrayAssert.arrayToString(actual) + ">",
//                expected.length, matchedElements);
//
//    }
//
//    private static String arrayToString(Object[] o) {
//        StringBuilder sb = new StringBuilder();
//        for (int i = 0; i < o.length; i++) {
//            sb.append(o[i].toString());
//            if (i != o.length - 1) {
//                sb.append(", ");
//            }
//        }
//        return sb.toString();
//    }
//
//    static public void assertEqualArrays(Object[] expected, Object[] actual) {
//        ArrayAssert.assertEqualArrays(null, expected, actual);
//    }

    public static Object getFirst(Collection<? extends Object> collection) {
        return collection.iterator().next();
    }

//    static public void assertEqualArrays(String message, byte[] expected, byte[] actual) {
//        if (Arrays.equals(expected, actual)) {
//            return;
//        }
//        String formatted = "";
//        if (message != null) {
//            formatted = message + " ";
//        }
//        assertNotNull(formatted + "expected array: <not null> but was <null>", actual);
//        assertNotNull(formatted + "expected array: <null> but was <not null>", expected);
//
//        assertEquals(formatted + "[array length] ", expected.length, actual.length);
//        for (int i = 0; i < expected.length; i++) {
//            assertEquals(formatted + "[position " + i + "]", expected[i], actual[i]);
//        }
//    }

    public static void assertEqualArrays(String message, List<? extends Object> expected, List<? extends Object> actual) {
        String formatted = "";
        if (message != null) {
            formatted = message + " ";
        }
        assertNotNull(formatted + "expected array: <not null> but was <null>", actual);
        assertNotNull(formatted + "expected array: <null> but was <not null>", expected);

        assertEquals(formatted + "[array length] ", expected.size(), actual.size());
        for (int i = 0; i < expected.size(); i++) {
            assertEquals(formatted + "[position " + i + "]", expected.get(i), actual.get(i));
        }
    }

    public static void assertEqualArrays(List<? extends Object> expected, List<? extends Object> actual) {
        ArrayAssert.assertEqualArrays(null, expected, actual);
    }

    public static void assertEqualArrays(Object message, Object[] expected, Object[] actual) {
        if (Arrays.equals(expected, actual)) {
            return;
        }

        String formatted = "";
        if (message != null) {
            formatted = message + " ";
        }
        assertNotNull(formatted + "expected array: <not null> but was <null>", actual);
        assertNotNull(formatted + "expected array: <null> but was <not null>", expected);

        assertEquals(formatted + "[array length] ", expected.length, actual.length);
        for (int i = 0; i < expected.length; i++) {
            assertEquals(formatted + "[position " + i + "]", expected[i], actual[i]);
        }
    }

//    static public void assertEqualArrays(Object message, int[] expected, int[] actual) {
//        if (Arrays.equals(expected, actual)) {
//            return;
//        }
//
//        String formatted = "";
//        if (message != null) {
//            formatted = message + " ";
//        }
//        assertNotNull(formatted + "expected array: <not null> but was <null>", actual);
//        assertNotNull(formatted + "expected array: <null> but was <not null>", expected);
//
//        assertEquals(formatted + "[array length] ", expected.length, actual.length);
//        for (int i = 0; i < expected.length; i++) {
//            assertEquals(formatted + "[position " + i + "]", expected[i], actual[i]);
//        }
//    }

//    static public void assertEqualArrays(Object message, long[] expected, long[] actual) {
//        if (Arrays.equals(expected, actual)) {
//            return;
//        }
//
//        String formatted = "";
//        if (message != null) {
//            formatted = message + " ";
//        }
//        assertNotNull(formatted + "expected array: <not null> but was <null>", actual);
//        assertNotNull(formatted + "expected array: <null> but was <not null>", expected);
//
//        assertEquals(formatted + "[array length] ", expected.length, actual.length);
//        for (int i = 0; i < expected.length; i++) {
//            assertEquals(formatted + "[position " + i + "]", expected[i], actual[i]);
//        }
//    }

    public static void assertEqualArrays(Object message, boolean[] expected, boolean[] actual) {
        if (Arrays.equals(expected, actual)) {
            return;
        }

        String formatted = "";
        if (message != null) {
            formatted = message + " ";
        }
        assertNotNull(formatted + "expected array: <not null> but was <null>", actual);
        assertNotNull(formatted + "expected array: <null> but was <not null>", expected);

        assertEquals(formatted + "[array length] ", expected.length, actual.length);
        for (int i = 0; i < expected.length; i++) {
            assertEquals(formatted + "[position " + i + "]", expected[i], actual[i]);
        }
    }

    public static void assertWeakEqualArrays(String message, Object[] expected, Object[] actual) {
        if (Arrays.equals(expected, actual)) {
            return;
        }

        String formatted = "";
        if (message != null) {
            formatted = message + " ";
        }
        assertNotNull(formatted + "expected array: <not null> but was <null>", actual);
        assertNotNull(formatted + "expected array: <null> but was <not null>", expected);

        assertEquals(formatted + "[array length] ", expected.length, actual.length);
        int matchedElements = 0;
        for (int i = 0; i < expected.length; i++) {
            for (int j = 0; j < actual.length; j++) {
                if (expected[i].equals(actual[j])) {
                    matchedElements++;
                    break;
                }
            }
        }
        assertEquals(formatted + "expected array: <" + ArrayAssert.arrayToString(expected) + "> but was <" + ArrayAssert.arrayToString(actual) + ">",
                expected.length, matchedElements);

    }

    public static void assertWeakEqualArrays(String message, Collection<? extends Object> expected, Collection<? extends Object> actual) {
        if (expected == null && actual == null) {
            return;
        }

        String formatted = "";
        if (message != null) {
            formatted = message + " ";
        }
        assertNotNull(formatted + "expected array: <not null> but was <null>", actual);
        assertNotNull(formatted + "expected array: <null> but was <not null>", expected);

        assertEquals(formatted + "[array length] ", expected.size(), actual.size());

        List<Object> tmp = Lists.newArrayList(actual);
        tmp.removeAll(expected);
        if (tmp.size() > 0) {
            assertEquals(formatted + "expected array: <" + expected + "> but was <" + actual + ">", expected.size(), expected.size() - tmp.size());
        }
        tmp = Lists.newArrayList(expected);
        tmp.removeAll(actual);
        if (tmp.size() > 0) {
            assertEquals(formatted + "expected array: <" + expected + "> but was <" + actual + ">", expected.size(), expected.size() - tmp.size());
        }
    }

    private static String arrayToString(Object[] o) {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < o.length; i++) {
            sb.append(o[i].toString());
            if (i != o.length - 1) {
                sb.append(", ");
            }
        }
        return sb.toString();
    }
}
