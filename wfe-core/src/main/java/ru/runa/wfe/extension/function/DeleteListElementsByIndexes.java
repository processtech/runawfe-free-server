package ru.runa.wfe.extension.function;

import java.util.Collections;
import java.util.List;

public class DeleteListElementsByIndexes extends Function<List<Object>> {

    public DeleteListElementsByIndexes() {
        super(Param.required(List.class), Param.required(List.class));
    }

    @Override
    protected List<Object> doExecute(Object... parameters) {
        List<Object> list = (List<Object>) parameters[0];
        List<Long> indexes = (List<Long>) parameters[1];
        Collections.sort(indexes);
        Collections.reverse(indexes);
        for (Long index : indexes) {
            if (index.intValue() >= list.size()) {
                throw new ArrayIndexOutOfBoundsException("Index " + index + " in " + list);
            }
            list.remove(index.intValue());
        }
        return list;
    }

    @Override
    public String getName() {
        return this.getClass().getSimpleName();
    }

}
