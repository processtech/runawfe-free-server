package ru.runa.common.web.form;

import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionMapping;

/**
 * Created on 26.08.2014
 * 
 * @struts:form name = "propertiesFileForm"
 */
public class SettingsFileForm extends ActionForm {

    private static final long serialVersionUID = 67L;
    private final HashMap<String, PropertyForm> properties = new HashMap<String, PropertyForm>();

    public static final String RESOURCE_INPUT_NAME = "resource";

    public static String oldValueInputName(String property) {
        return "property(" + property + ").oldValue";
    }

    public static String newValueInputName(String property) {
        return "property(" + property + ").newValue";
    }

    private String resource;

    public String getResource() {
        return resource;
    }

    public void setResource(String resource) {
        this.resource = resource;
    }

    public Map<String, String> getModifiedSettings() {
        Map<String, String> res = new HashMap<String, String>();
        for (Map.Entry<String, PropertyForm> entry : properties.entrySet()) {
            PropertyForm f = entry.getValue();
            if (!f.getNewValue().equals(f.getOldValue())) {
                res.put(entry.getKey(), f.getNewValue());
            }
        }
        return res;
    }

    @Override
    public void reset(ActionMapping mapping, HttpServletRequest request) {
        super.reset(mapping, request);
        properties.clear();
    }

    public Object getProperty(String property) {
        if (!properties.containsKey(property)) {
            properties.put(property, new PropertyForm());
        }
        return properties.get(property);
    }

    public static class PropertyForm {
        private String oldValue;
        private String newValue;

        public String getOldValue() {
            return oldValue;
        }

        public void setOldValue(String oldValue) {
            this.oldValue = oldValue;
        }

        public String getNewValue() {
            return newValue;
        }

        public void setNewValue(String newValue) {
            this.newValue = newValue;
        }
    }
}
