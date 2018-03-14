package ru.runa.wfe.extension.function;

import java.util.List;

import com.google.common.collect.Lists;

public class ToList extends Function<List<Object>> {

    public ToList() {
        super(Param.multiple(List.class));
    }

    @Override
    protected List<Object> doExecute(Object... parameters) {
        List<Object> result = Lists.newArrayList();
        for (Object parameter : parameters) {
            result.addAll((List<?>) parameter);
        }
        return result;
    }

    @Override
    public String getName() {
        return this.getClass().getSimpleName();
    }

}
