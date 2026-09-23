package ru.runa.wfe.extension;

import java.util.List;
import java.util.Map;
import ru.runa.wfe.lang.ParsedProcessDefinition;

public interface StartConditionalHandler extends Configurable {

    List<Map<String, Object>> execute(ParsedProcessDefinition parsedProcessDefinition) throws Exception;

}
