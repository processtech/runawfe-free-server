package ru.runa.wf.logic.bot.updatepermission;

import java.util.List;

import org.dom4j.Document;
import org.dom4j.Element;

import ru.runa.wfe.commons.xml.XmlUtils;
import ru.runa.wfe.security.ApplicablePermissions;
import ru.runa.wfe.security.Permission;
import ru.runa.wfe.security.SecuredObjectType;

public class UpdatePermissionsXmlParser {
    private static final String CONDITION_ELEMENT_NAME = "condition";
    private static final String CONDITION_VAR_NAME_ATTRIBUTE_NAME = "varName";
    private static final String CONDITION_VAR_VALUE_ATTRIBUTE_NAME = "varValue";
    private static final String ORGFUNCTIONS_ELEMENT_NAME = "orgFunctions";
    private static final String ORGFUNCTION_ELEMENT_NAME = "orgFunction";
    private static final String METHOD_ELEMENT_NAME = "method";
    private static final String PERMISSIONS_ELEMENT_NAME = "permissions";
    private static final String PERMISSION_ELEMENT_NAME = "permission";

    public static UpdatePermissionsSettings read(String configuration) {
        UpdatePermissionsSettings settings = new UpdatePermissionsSettings();
        Document document = XmlUtils.parseWithXSDValidation(configuration, "update-permissions.xsd");
        Element root = document.getRootElement();
        List<Element> orgFunctionElements = root.element(ORGFUNCTIONS_ELEMENT_NAME).elements(ORGFUNCTION_ELEMENT_NAME);
        for (Element element : orgFunctionElements) {
            settings.getSwimlaneInitializers().add(element.getTextTrim());
        }
        settings.setMethod(Method.valueOf(root.elementTextTrim(METHOD_ELEMENT_NAME)));
        List<Element> permissionElements = root.element(PERMISSIONS_ELEMENT_NAME).elements(PERMISSION_ELEMENT_NAME);
        for (Element element : permissionElements) {
            Permission p = Permission.valueOf(element.getTextTrim());
            ApplicablePermissions.check(SecuredObjectType.PROCESS, p);
            settings.getPermissions().add(p);
        }
        Element conditionElement = root.element(CONDITION_ELEMENT_NAME);
        if (conditionElement != null) {
            settings.setCondition(conditionElement.attributeValue(CONDITION_VAR_NAME_ATTRIBUTE_NAME),
                    conditionElement.attributeValue(CONDITION_VAR_VALUE_ATTRIBUTE_NAME));
        }
        return settings;
    }

}
