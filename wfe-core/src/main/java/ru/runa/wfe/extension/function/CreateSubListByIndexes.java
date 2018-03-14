package ru.runa.wfe.extension.function;

import java.util.List;

import com.google.common.collect.Lists;

public class CreateSubListByIndexes extends Function<List<Object>> {

    public CreateSubListByIndexes() {
        super(Param.required(List.class), Param.required(List.class));
    }

    @Override
    protected List<Object> doExecute(Object... parameters) {
        List<Object> list = (List<Object>) parameters[0];
        List<Long> indexes = (List<Long>) parameters[1];
        List<Object> result = Lists.newArrayList();
        for (Long index : indexes) {
            if (index.intValue() >= list.size()) {
                throw new ArrayIndexOutOfBoundsException("Index " + index + " in " + list);
            }
            result.add(list.get(index.intValue()));
        }
        return result;
    }

    @Override
    public String getName() {
        return this.getClass().getSimpleName();
    }

}
