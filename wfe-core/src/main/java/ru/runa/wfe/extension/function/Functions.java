package ru.runa.wfe.extension.function;

import java.util.Map;

import com.google.common.collect.Maps;

/**
 * 
 * @author Dmitry Kononov
 * @since 28.02.2018
 *
 */
public abstract class Functions {

    private static final Map<String, Function<? extends Object>> functions = Maps.newHashMap();

    static {
        register(new ListToString());
        register(new GetListMatchedIndexes());
        register(new GetListMismatchedIndexes());
        register(new CreateSubListByIndexes());
        register(new DeleteListElementsByIndexes());
        register(new ToList());
        register(new GetSize());
        register(new CurrentDate());
        register(new CurrentTime());
        register(new FormattedDate());
        register(new FormattedTime());
        register(new HoursRoundUp());
        register(new Mapping());
        register(new NameCaseRussian());
        register(new NameCaseRussianWithFixedSex());
        register(new NumberToShortStringRu());
        register(new NumberToStringRu());
        register(new Round());
        register(new RoundDown());
        register(new RoundUp());
    }

    public static Object execute(String name, Object... parameters) {
        return get(name).execute(parameters);
    }

    public static Function<? extends Object> get(String name) {
        return functions.get(name);
    }

    private static void register(Function<? extends Object> function) {
        functions.put(function.getName(), function);
    }

}
