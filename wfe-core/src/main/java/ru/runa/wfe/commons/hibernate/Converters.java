package ru.runa.wfe.commons.hibernate;

import java.util.Map;

import org.springframework.beans.factory.annotation.Required;

import ru.runa.wfe.var.Converter;

import com.google.common.collect.BiMap;
import com.google.common.collect.HashBiMap;

/**
 * provides access to the list of converters and ensures that the converter objects are unique.
 */
public class Converters {
    private final BiMap<String, Converter> mappings = HashBiMap.create();

    @Required
    public void setMappings(Map<String, Converter> mappings) {
        this.mappings.putAll(mappings);
    }
    
    public Converter getConverterByDatabaseId(String converterDatabaseId) {
        return mappings.get(converterDatabaseId);
    }

    public String getConverterId(Converter converter) {
        return mappings.inverse().get(converter);
    }

}
