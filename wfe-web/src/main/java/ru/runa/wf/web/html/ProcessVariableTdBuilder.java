package ru.runa.wf.web.html;

import org.apache.ecs.html.TD;
import ru.runa.common.web.StrutsWebHelper;
import ru.runa.common.web.html.TdBuilder;
import ru.runa.wf.web.ftl.component.ViewUtil;
import ru.runa.wfe.execution.dto.WfProcess;
import ru.runa.wfe.var.dto.WfVariable;
import ru.runa.wfe.var.format.VariableFormat;

/**
 * @author Konstantinov Aleksey
 */
public class ProcessVariableTdBuilder implements TdBuilder {
    private final String variableName;

    public ProcessVariableTdBuilder(String variableName) {
        this.variableName = variableName;
    }

    @Override
    public TD build(Object object, Env env) {
        TD td = new TD();
        td.setClass(ru.runa.common.web.Resources.CLASS_LIST_TABLE_TD);
        td.addElement(getDisplayValue(object, env));
        return td;
    }

    //now used for excel export only
    @Override
    public String getValue(Object object, Env env) {
        WfProcess process = (WfProcess) object;
        WfVariable variable = process.getVariable(variableName);
        if (variable != null && variable.getValue() != null) {
            VariableFormat format = variable.getDefinition().getFormatNotNull();
            return format.formatExcelCell(variable.getValue());
        }
        return "";
    }

    @Override
    public String[] getSeparatedValues(Object object, Env env) {
        return new String[] { getDisplayValue(object, env) };
    }
    
    private String getDisplayValue(Object object, Env env) {
        WfProcess process = (WfProcess) object;
        WfVariable variable = process.getVariable(variableName);
        if (variable != null && variable.getValue() != null) {
            return ViewUtil.getOutput(env.getUser(), new StrutsWebHelper(env.getPageContext()), process.getId(), variable);
        }
        return "";
    }

    @Override
    public int getSeparatedValuesCount(Object object, Env env) {
        return 1;
    }
}
