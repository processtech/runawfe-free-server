package ru.runa.wf.web.ftl.component.legacy;

import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import org.json.simple.JSONArray;
import ru.runa.wf.web.ftl.component.GenerateHtmlForVariable;
import ru.runa.wf.web.ftl.component.ViewUtil;
import ru.runa.wfe.commons.ClassLoaderUtil;
import ru.runa.wfe.commons.ftl.FormComponent;
import ru.runa.wfe.commons.web.WebHelper;
import ru.runa.wfe.commons.web.WebUtils;
import ru.runa.wfe.service.client.FileVariableProxy;
import ru.runa.wfe.user.User;
import ru.runa.wfe.util.OrderedJsonObject;
import ru.runa.wfe.var.UserTypeMap;
import ru.runa.wfe.var.VariableDefinition;
import ru.runa.wfe.var.dto.WfVariable;
import ru.runa.wfe.var.format.FileFormat;
import ru.runa.wfe.var.format.FormatCommons;
import ru.runa.wfe.var.format.VariableFormat;

public abstract class LegacyAbstractListUserVariables extends FormComponent {
    private static final long serialVersionUID = 1L;
    private static final Random random = new Random(System.currentTimeMillis());

    protected List<UserTypeMap> list;
    protected String variableName;
    protected String dectVariableName;
    protected DisplayMode displayMode;
    protected String sortField;

    protected void initFields() {
        if (getClass().equals(LegacyDisplayListUserVariables.class)) {
            variableName = getParameterAsString(0);
            displayMode = DisplayMode.fromString(getParameterAsString(1));
            sortField = getParameterAsString(2);
        } else if (getClass().equals(LegacyMultipleSelectFromListUserVariables.class)) {
            variableName = getParameterAsString(1);
            list = variableProvider.getValue(List.class, variableName);
            dectVariableName = getParameterAsString(0);
            displayMode = DisplayMode.fromString(getParameterAsString(2));
            sortField = getParameterAsString(3);
        }
    }

    @Override
    abstract protected Object renderRequest() throws Exception;

    public enum DisplayMode {
        TWO_DIMENTIONAL_TABLE("two-dimentional"),
        MULTI_DIMENTIONAL_TABLE("multi-dimentional");

        private final String mode;

        private DisplayMode(String s) {
            mode = s;
        }

        public static final DisplayMode fromString(String md) {
            for (DisplayMode dm : DisplayMode.values()) {
                if (!dm.mode.equals(md)) {
                    continue;
                }
                return dm;
            }
            return null;
        }
    }

    public static final String getUserTypeListTable(User user, WebHelper webHelper, WfVariable variable, WfVariable dectSelectVariable,
            String sortFieldName, boolean isMultiDim) {
        if (!(variable.getValue() instanceof List)) {
            return "";
        }
        JSONArray objectsList = new JSONArray();
        List<?> values = (List<?>) variable.getValue();
        for (Object value : values) {
            if (!(value instanceof UserTypeMap)) {
                return "";
            }
            UserTypeMap userTypeMap = (UserTypeMap) value;
            OrderedJsonObject cvarObj = new OrderedJsonObject();
            for (VariableDefinition varDef : userTypeMap.getUserType().getAttributes()) {
                if (userTypeMap.get(varDef.getName()) == null) {
                    cvarObj.put(varDef.getName(), "");
                    continue;
                }
                VariableFormat format = FormatCommons.create(varDef);
                if (dectSelectVariable == null) {
                    if (format instanceof FileFormat) {
                        FileVariableProxy proxy = (FileVariableProxy) userTypeMap.get(varDef.getName());
                        cvarObj.put(varDef.getName(), GenerateHtmlForVariable.getFileComponent(webHelper, proxy.getName(), proxy, false, false));
                    } else {
                        cvarObj.put(varDef.getName(), format.format(userTypeMap.get(varDef.getName())));
                    }
                } else {
                    cvarObj.put(varDef.getName(), format.format(userTypeMap.get(varDef.getName())));
                }
            }
            objectsList.add(cvarObj);
        }
        String uniquename = String.format("%s_%x", variable.getDefinition().getScriptingNameWithoutDots(), random.nextInt());
        String result = "<script src=\"/wfe/js/tidy-table.js\"></script>\n";
        InputStream javascriptStream = ClassLoaderUtil.getAsStreamNotNull("scripts/legacy/ViewUtil.UserTypeListTable.js", ViewUtil.class);
        Map<String, String> substitutions = new HashMap<String, String>();
        substitutions.put("UNIQUENAME", uniquename);
        substitutions.put("JSONDATATEMPLATE", objectsList.toJSONString());
        substitutions.put("SORTFIELDNAMEVALUE", String.format("%s", sortFieldName));
        substitutions.put("DIMENTIONALVALUE", String.format("%s", isMultiDim));
        substitutions.put("SELECTABLEVALUE", String.format("%s", dectSelectVariable != null));
        if (dectSelectVariable != null) {
            substitutions.put("DECTSELECTNAME", dectSelectVariable.getDefinition().getName());
        } else {
            substitutions.put("DECTSELECTNAME", "");
        }
        result += WebUtils.getFormComponentScript(javascriptStream, substitutions);
        result += "<link rel=\"stylesheet\" type=\"text/css\" href=\"/wfe/css/tidy-table.css\">\n";
        result += String.format("<div id=\"container%s\"></div>", uniquename);
        return result;
    }

}
