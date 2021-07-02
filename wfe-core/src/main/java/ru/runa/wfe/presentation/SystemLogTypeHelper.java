/*
 * This file is part of the RUNA WFE project.
 * 
 * This program is free software; you can redistribute it and/or 
 * modify it under the terms of the GNU Lesser General Public License 
 * as published by the Free Software Foundation; version 2.1 
 * of the License. 
 * 
 * This program is distributed in the hope that it will be useful, 
 * but WITHOUT ANY WARRANTY; without even the implied warranty of 
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the 
 * GNU Lesser General Public License for more details. 
 * 
 * You should have received a copy of the GNU Lesser General Public License 
 * along with this program; if not, write to the Free Software 
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA.
 */
package ru.runa.wfe.presentation;

import java.util.HashMap;
import java.util.Map;

import ru.runa.wfe.audit.ProcessDefinitionDeleteLog;
import ru.runa.wfe.audit.ProcessDeleteLog;
import ru.runa.wfe.audit.ProcessLogsCleanLog;
import ru.runa.wfe.audit.SystemLog;

import com.google.common.collect.Maps;

/**
 * Helper class to map {@link SystemLog} descriminator value to display name.
 */
public final class SystemLogTypeHelper {
    /**
     * {@link Map} from enumerated value (type descriminator value) to property
     * display name (struts property).
     */
    private static final Map<String, String> enumerationValues = new HashMap<String, String>();

    /**
     * {@link Map} from type to property display name (struts property).
     */
    private static final Map<Class<? extends SystemLog>, String> typeValues = Maps.newHashMap();

    static {
        addType(ProcessDefinitionDeleteLog.class, "PDDel", "history.system.type.process_definition_delete");
        addType(ProcessDeleteLog.class, "PIDel", "history.system.type.process_delete");
        addType(ProcessLogsCleanLog.class, "PLDel", "history.system.type.process_logs_cleaned");
    }

    /**
     * {@link Map} from enumerated value ({@link SystemLog} discriminator value)
     * to property display name (struts property).
     */
    public static Map<String, String> getValues() {
        return new HashMap<String, String>(enumerationValues);
    }

    /**
     * {@link Map} from type to property display name (struts property).
     */
    public static Map<Class<? extends SystemLog>, String> getClasses() {
        return typeValues;
    }

    /**
     * Add class and it's descriminator value to maps.
     * 
     * @param clazz
     *            Class, inherited from {@link SystemLog}.
     * @param discriminator
     *            Class discriminator value.
     * @param displayProperty
     *            Struts property, to display for this class.
     */
    private static void addType(Class<? extends SystemLog> clazz, String discriminator, String displayProperty) {
        enumerationValues.put(discriminator, displayProperty);
        typeValues.put(clazz, displayProperty);
    }
}
