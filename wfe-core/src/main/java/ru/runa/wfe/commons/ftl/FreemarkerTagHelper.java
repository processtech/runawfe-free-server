package ru.runa.wfe.commons.ftl;

public class FreemarkerTagHelper {
    private static final String PLACE_HOLDER_START = "${";
    private static final String PLACE_HOLDER_FINISH = "}";

    private static final String PLACE_HOLDER_PARAM_START = "(";
    private static final String PLACE_HOLDER_PARAM_FINISH = ")";

    private static final String PLACE_HOLDER_WRAP_SYMBOL = "\"";

    public static final String build(String tag, String variableName, String... params) {
        StringBuffer stringBuffer = new StringBuffer();
        stringBuffer.append(PLACE_HOLDER_START);
        stringBuffer.append(tag);
        stringBuffer.append(PLACE_HOLDER_PARAM_START);
        stringBuffer.append(wrapValue(variableName));
        if (params != null && params.length > 0) {
            for (String param : params) {
                stringBuffer.append(", ");
                stringBuffer.append(wrapValue(param));
            }
        }
        stringBuffer.append(PLACE_HOLDER_PARAM_FINISH);
        stringBuffer.append(PLACE_HOLDER_FINISH);

        return stringBuffer.toString();
    }

    private static final String wrapValue(String value) {
        if (value == null) {
            return "";
        }

        return PLACE_HOLDER_WRAP_SYMBOL + value + PLACE_HOLDER_WRAP_SYMBOL;
    }
}
