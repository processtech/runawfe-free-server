/*
 * JBoss, Home of Professional Open Source
 * Copyright 2005, JBoss Inc., and individual contributors as indicated
 * by the @authors tag. See the copyright.txt in the distribution for a
 * full listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
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
