package ru.runa.wfe.extension.function;

import java.util.List;

import com.google.common.collect.Lists;

public class GetListMismatchedIndexes extends Function<List<Long>> {

    public GetListMismatchedIndexes() {
        super(Param.required(List.class), Param.required(List.class));
    }

    @Override
    protected List<Long> doExecute(Object... parameters) {
        List<Object> list1 = (List<Object>) parameters[0];
        List<Object> list2 = (List<Object>) parameters[1];
        List<Long> result = Lists.newArrayList();
        for (int i=0; i<list1.size(); i++) {
            if (!list2.contains(list1.get(i))) {
                result.add(Long.valueOf(i));
            }
        }
        return result;
    }

}
