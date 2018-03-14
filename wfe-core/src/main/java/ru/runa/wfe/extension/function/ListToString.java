package ru.runa.wfe.extension.function;

import java.util.List;

public class ListToString extends Function<String> {

    public ListToString() {
        super(Param.required(List.class), Param.optional(String.class, "\r\n"));
    }

    @Override
    protected String doExecute(Object... parameters) {
        List<?> list = (List<?>) parameters[0];
        String delimiter = (String) parameters[1];
        StringBuffer buffer = new StringBuffer();
        for (Object object : list) {
            if (buffer.length() > 0) {
                buffer.append(delimiter);
            }
            buffer.append(object);
        }
        return buffer.toString();
    }

}
