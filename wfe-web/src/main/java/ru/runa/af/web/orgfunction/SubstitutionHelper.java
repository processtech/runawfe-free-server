package ru.runa.af.web.orgfunction;

import ru.runa.wfe.execution.logic.OrgFunctionSwimlaneInitializer;
import ru.runa.wfe.execution.logic.SwimlaneInitializerHelper;
import ru.runa.wfe.extension.orgfunction.ParamRenderer;
import ru.runa.wfe.user.User;

public class SubstitutionHelper {

    public static String getUserFriendlyOrgFunction(User user, String swimlaneConfiguration) {
        StringBuffer result = new StringBuffer();
        OrgFunctionSwimlaneInitializer swimlaneInitializer = (OrgFunctionSwimlaneInitializer) SwimlaneInitializerHelper.parse(swimlaneConfiguration);
        FunctionDef functionDef = SubstitutionDefinitions.getByClassNameNotNull(swimlaneInitializer.getOrgFunctionClassName());
        result.append(functionDef.getLabel());
        result.append("(");
        for (int i = 0; i < functionDef.getParams().size(); i++) {
            if (i != 0) {
                result.append(", ");
            }
            ParamRenderer renderer = functionDef.getParams().get(i).getRenderer();
            result.append(renderer.getDisplayLabel(user, swimlaneInitializer.getParameterNames()[i]));
        }
        result.append(")");
        return result.toString();
    }
}
