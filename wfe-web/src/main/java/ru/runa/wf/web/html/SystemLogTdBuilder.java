package ru.runa.wf.web.html;

import javax.servlet.jsp.PageContext;

import org.apache.ecs.html.TD;

import ru.runa.common.web.Messages;
import ru.runa.common.web.MessagesOther;
import ru.runa.common.web.Resources;
import ru.runa.common.web.html.TdBuilder;
import ru.runa.wfe.audit.ProcessDefinitionDeleteLog;
import ru.runa.wfe.audit.ProcessDeleteLog;
import ru.runa.wfe.audit.ProcessLogsCleanLog;
import ru.runa.wfe.audit.SystemLog;
import ru.runa.wfe.commons.CalendarUtil;

/**
 * {@link TdBuilder} implementation to show system log as human readable
 * message.
 */
public class SystemLogTdBuilder implements TdBuilder {

    /**
     * Process place holder name. All occurrences of this place holder will be
     * replaced with process instance identity.
     */
    private static String placeHolderProcess = null;

    /**
     * Process definition name place holder name. All occurrences of this place
     * holder will be replaced with process definition name.
     */
    private static String placeHolderProcessDefinition = null;

    /**
     * Process definition version place holder name. All occurrences of this
     * place holder will be replaced with process definition version.
     */
    private static String placeHolderVersion = null;

    /**
     * Place holder name for date before which to delete the process log.
     */
    private static String placeHolderBeforeDate = null;
    
    private static synchronized void initPlaceholders(PageContext pageContext) {
        if (placeHolderProcess != null) {
            return;
        }
        placeHolderProcess = MessagesOther.HISTORY_SYSTEM_PH_PI.message(pageContext);
        placeHolderProcessDefinition = MessagesOther.HISTORY_SYSTEM_PH_PD.message(pageContext);
        placeHolderVersion = MessagesOther.HISTORY_SYSTEM_PH_VERSION.message(pageContext);
        placeHolderBeforeDate = MessagesOther.PROCESS_LOG_CLEAN_BEFORE_DATE.message(pageContext);
    }

    @Override
    public TD build(Object object, Env env) {
        TD result = new TD(getValue(object, env));
        result.setClass(Resources.CLASS_LIST_TABLE_TD);
        return result;
    }

    @Override
    public String getValue(Object object, Env env) {
        initPlaceholders(env.getPageContext());
        SystemLog systemLog = (SystemLog) object;
        if (systemLog instanceof ProcessDeleteLog) {
            ProcessDeleteLog log = (ProcessDeleteLog) systemLog;
            return MessagesOther.SYSTEM_LOG_PROCESS_DELETED.message(env.getPageContext())
                    .replaceAll("\\{" + placeHolderProcessDefinition + "\\}", log.getName() != null ? log.getName() : "")
                    .replaceAll("\\{" + placeHolderProcess + "\\}", String.valueOf(log.getProcessId()));
        } else if (systemLog instanceof ProcessDefinitionDeleteLog) {
            ProcessDefinitionDeleteLog log = (ProcessDefinitionDeleteLog) systemLog;
            return MessagesOther.SYSTEM_LOG_DEFINITION_DELETED.message(env.getPageContext())
                    .replaceAll("\\{" + placeHolderProcessDefinition + "\\}", log.getName())
                    .replaceAll("\\{" + placeHolderVersion + "\\}", String.valueOf(log.getVersion()));
        } else if (systemLog instanceof ProcessLogsCleanLog) {
            ProcessLogsCleanLog log = (ProcessLogsCleanLog) systemLog;
            return MessagesOther.PROCESS_LOG_CLEAN_DESCRIPTION.message(env.getPageContext())
                    .replaceAll("\\{" + placeHolderBeforeDate + "\\}", CalendarUtil.formatDate(log.getBeforeDate()));
        }
        return "Unsupported log instance";
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
