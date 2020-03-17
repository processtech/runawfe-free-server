package ru.runa.wfe.service.jaxb;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class StringKeyValueConverter {

    public static Map<String, String> unmarshal(List<StringKeyValue> list) {
        Map<String, String> map = new HashMap<>();
        if (list != null) {
            for (StringKeyValue keyValue : list) {
                map.put(keyValue.key, keyValue.value);
            }
        }
        return map;
    }

}
