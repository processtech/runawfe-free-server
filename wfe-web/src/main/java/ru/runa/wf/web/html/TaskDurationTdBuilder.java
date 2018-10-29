package ru.runa.wf.web.html;

import java.util.Date;

import org.apache.ecs.html.TD;

import ru.runa.common.web.html.TdBuilder;
import ru.runa.wfe.task.TaskDeadlineUtils;
import ru.runa.wfe.task.dto.WfTask;

/**
 * Class for displaying task duration period (current_date - Task.createDate) in the task list table
 * 
 * @author Vladimir Shevtsov
 *
 */
public class TaskDurationTdBuilder implements TdBuilder {

    @Override
    public TD build(Object object, Env env) {
        TD td = new TD();
        td.setClass(ru.runa.common.web.Resources.CLASS_LIST_TABLE_TD);
        td.addElement(getValue(object, env));
        return td;
    }

    @Override
    public String getValue(Object object, Env env) {
        WfTask task = (WfTask) object;
        return TaskDeadlineUtils.calculateTimeDuration(task.getCreationDate(), new Date());
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
