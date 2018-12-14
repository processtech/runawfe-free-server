package ru.runa.wf.web.html;

import org.apache.ecs.html.TD;
import ru.runa.common.web.html.TdBuilder;
import ru.runa.wfe.execution.dto.WfProcess;

public class ProcessErrorReasonTdBuilder implements TdBuilder {

    @Override
    public TD build(Object object, Env env) {
        TD td = new TD();
        td.setClass(ru.runa.common.web.Resources.CLASS_LIST_TABLE_TD);
        td.addElement(getValue(object, env));
        return td;
    }

    @Override
    public String getValue(Object object, Env env) {
        WfProcess process = (WfProcess) object;
        return process.getProcessErrors();
    }

    @Override
    public String[] getSeparatedValues(Object object, Env env) {
        return new String[] { getValue(object, env) };
    }

    @Override
    public int getSeparatedValuesCount(Object object, Env env) {
        return 1;
    }

}
