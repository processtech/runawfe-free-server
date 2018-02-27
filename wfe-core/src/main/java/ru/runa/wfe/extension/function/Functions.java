package ru.runa.wfe.extension.function;

import java.util.Map;

import com.google.common.collect.Maps;

/**
 * 
 * @author Dmitry Kononov
 * @since 28.02.2018
 *
 */
public class Functions {

    private static final Map<String, Function<? extends Object>> functions = Maps.newHashMap();

    static {
        registerFunction(new ListToString());
        registerFunction(new GetListMatchedIndexes());
        registerFunction(new GetListMismatchedIndexes());
        registerFunction(new CreateSubListByIndexes());
        registerFunction(new DeleteListElementsByIndexes());
        registerFunction(new ToList());
        registerFunction(new GetSize());
        registerFunction(new CurrentDate());
        registerFunction(new CurrentTime());
        registerFunction(new FormattedDate());
        registerFunction(new FormattedTime());
        registerFunction(new HoursRoundUp());
        registerFunction(new Mapping());
        registerFunction(new NameCaseRussian());
        registerFunction(new NumberToShortStringRu());
        registerFunction(new NumberToStringRu());
        registerFunction(new RoundDouble());
        registerFunction(new RoundDownDouble());
        registerFunction(new RoundDownLong());
        registerFunction(new RoundLong());
        registerFunction(new RoundUpDouble());
        registerFunction(new RoundUpLong());
    }

    public static Object executeFunction(String name, Object... parameters) {
        return getFunction(name).execute(parameters);
    }

    public static Function<? extends Object> getFunction(String name) {
        return functions.get(name);
    }

    private static void registerFunction(Function<? extends Object> function) {
        functions.put(function.getName(), function);
    }

}
