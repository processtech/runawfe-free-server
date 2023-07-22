package ru.runa.wfe.service.interceptors;

import java.util.List;

import javax.interceptor.InvocationContext;

import com.google.common.base.Joiner;
import com.google.common.collect.Lists;

public class DebugUtils {

    public static String getDebugString(InvocationContext ic, boolean includeArguments) {
        String s = ic.getMethod().getDeclaringClass().getSimpleName() + "." + ic.getMethod().getName();
        if (includeArguments) {
            s += "(" + Joiner.on(", ").join(getDebugArguments(ic.getParameters())) + ")";
        }
        return s;
    }

    private static List<String> getDebugArguments(Object[] values) {
        List<String> strings = Lists.newArrayList();
        if (values != null) {
            for (Object object : values) {
                String string;
                if (object == null) {
                    string = "null";
                } else {
                    string = object.toString();
                }
                strings.add(string);
            }
        }
        return strings;
    }

}
