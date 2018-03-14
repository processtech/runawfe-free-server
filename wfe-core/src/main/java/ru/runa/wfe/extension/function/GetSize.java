package ru.runa.wfe.extension.function;

import java.lang.reflect.Array;
import java.util.List;
import java.util.Map;

public class GetSize extends Function<Long> {

    public GetSize() {
        super(Param.required(Object.class));
    }

    @Override
    protected Long doExecute(Object... parameters) {
        if (parameters[0].getClass().isArray()) {
            return (long) Array.getLength(parameters[0]);
        } else if (parameters[0] instanceof List) {
            return (long) ((List<?>) parameters[0]).size();
        } else if (parameters[0] instanceof Map) {
            return (long) ((Map<?, ?>) parameters[0]).size();
        } else {
            return 1L;
        }
    }

    @Override
    public String getName() {
        return this.getClass().getSimpleName();
    }

}
