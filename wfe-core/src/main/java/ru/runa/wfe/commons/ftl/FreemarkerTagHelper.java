package ru.runa.wfe.commons.ftl;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class FreemarkerTagHelper {
    private static final String PLACE_HOLDER_START = "${";
    private static final String PLACE_HOLDER_FINISH = "}";

    private static final String PLACE_HOLDER_PARAM_START = "(";
    private static final String PLACE_HOLDER_PARAM_FINISH = ")";

    private static final String PLACE_HOLDER_WRAP_SYMBOL = "\"";

    public static final String build(String tag, String variableName, List<Object> params) {
        StringBuffer stringBuffer = new StringBuffer();
        stringBuffer.append(PLACE_HOLDER_START);
        stringBuffer.append(tag);
        stringBuffer.append(PLACE_HOLDER_PARAM_START);
        stringBuffer.append(String.join(", ", params.stream().flatMap(param -> paramAsStream(param)).collect(Collectors.toList())));
        stringBuffer.append(PLACE_HOLDER_PARAM_FINISH);
        stringBuffer.append(PLACE_HOLDER_FINISH);
        return stringBuffer.toString();
    }

    private static final Stream<String> paramAsStream(Object param) {
        // параметры бывают 2 типов: либо String, либо List<String>
        if (param instanceof List<?>) {
            return ((List<String>) param).stream().map(i -> wrapValue(i));
        } else {
            return Stream.of(wrapValue((String) param));
        }
    }

    private static final String wrapValue(String value) {
        if (value == null) {
            return "";
        }
        return PLACE_HOLDER_WRAP_SYMBOL + value + PLACE_HOLDER_WRAP_SYMBOL;
    }
}
