package ru.runa.wfe.definition.logic;

import ru.runa.wfe.commons.BackCompatibilityClassNames;
import ru.runa.wfe.commons.dao.LocalizationDAO;
import ru.runa.wfe.lang.SwimlaneDefinition;

public class SwimlaneUtils {

    /**
     * Sets organization function display label.
     * 
     * @param swimlaneDefinition
     * @param localizationDAO
     */
    public static void setOrgFunctionLabel(SwimlaneDefinition swimlaneDefinition, LocalizationDAO localizationDAO) {
        if (swimlaneDefinition.getDelegation() != null && swimlaneDefinition.getDelegation().getConfiguration() != null) {
            String conf = swimlaneDefinition.getDelegation().getConfiguration();
            swimlaneDefinition.setOrgFunctionLabel(conf);
            String[] orgFunctionParts = conf.split("\\(");
            if (orgFunctionParts.length == 2) {
                String className = BackCompatibilityClassNames.getClassName(orgFunctionParts[0].trim());
                String localized = localizationDAO.getLocalized(className);
                if (localized != null) {
                    swimlaneDefinition.setOrgFunctionLabel(localized + " (" + orgFunctionParts[1]);
                }
            }
        }
    }
}
