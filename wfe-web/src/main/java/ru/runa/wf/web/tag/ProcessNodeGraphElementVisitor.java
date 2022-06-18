package ru.runa.wf.web.tag;

import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.servlet.jsp.PageContext;
import org.apache.ecs.html.Area;
import org.apache.ecs.html.TD;
import org.apache.ecs.html.TR;
import org.apache.ecs.html.Table;
import ru.runa.common.web.HTMLUtils;
import ru.runa.common.web.Messages;
import ru.runa.common.web.Resources;
import ru.runa.wf.web.action.ShowGraphModeHelper;
import ru.runa.wf.web.html.GraphElementPresentationHelper;
import ru.runa.wfe.audit.ActionLog;
import ru.runa.wfe.audit.BaseProcessLog;
import ru.runa.wfe.audit.Severity;
import ru.runa.wfe.commons.CalendarUtil;
import ru.runa.wfe.graph.view.MultiSubprocessNodeGraphElement;
import ru.runa.wfe.graph.view.NodeGraphElement;
import ru.runa.wfe.graph.view.NodeGraphElementVisitor;
import ru.runa.wfe.graph.view.SubprocessNodeGraphElement;
import ru.runa.wfe.graph.view.TaskNodeGraphElement;
import ru.runa.wfe.lang.NodeType;
import ru.runa.wfe.service.delegate.Delegates;
import ru.runa.wfe.user.User;

/**
 * Operation to create links to subprocesses and tool tips to minimized elements.
 */
public class ProcessNodeGraphElementVisitor extends NodeGraphElementVisitor {
    private static final Pattern ACTION_LOG_PATTERN = Pattern.compile(".*?class=(.*?), configuration.*?", Pattern.DOTALL);

    /**
     * Helper to create links to subprocesses.
     */
    private final GraphElementPresentationHelper presentationHelper;
    private final User user;
    private final PageContext pageContext;
    /**
     * Helper to create tool tips for task graph elements.
     */
    private final TD td;

    /**
     * Creates operation to create links to subprocesses and tool tips to minimized elements.
     *
     * @param pageContext Rendered page context.
     * @param td          Root form element.
     */
    public ProcessNodeGraphElementVisitor(User user, PageContext pageContext, TD td, String subprocessId) {
        this.user = user;
        this.pageContext = pageContext;
        this.td = td;
        presentationHelper = new GraphElementPresentationHelper(pageContext, subprocessId);
    }

    @Override
    public void visit(NodeGraphElement element) {
        Area area = null;
        if (element.getNodeType() == NodeType.SUBPROCESS) {
            area = presentationHelper.createSubprocessLink((SubprocessNodeGraphElement) element, ShowGraphModeHelper.getManageProcessAction(),
                    "javascript:showEmbeddedSubprocess");
        }
        if (element.getNodeType() == NodeType.MULTI_SUBPROCESS) {
            td.addElement(presentationHelper.createMultiSubprocessLinks((MultiSubprocessNodeGraphElement) element,
                    ShowGraphModeHelper.getManageProcessAction()));
        }
        if (element.getNodeType() == NodeType.TASK_STATE) {
            area = presentationHelper.createTaskTooltip((TaskNodeGraphElement) element);
        }

        if (element.getData() != null) {
            Table table = new Table();
            table.setClass(Resources.CLASS_LIST_TABLE);
            for (BaseProcessLog log : element.getData()) {
                String description;
                try {
                    String format = Messages.getMessage("history.log." + log.getPatternName(), pageContext);
                    Object[] arguments = log.getPatternArguments();
                    if (log instanceof ActionLog) {
                        // #812
                        Matcher matcher = ACTION_LOG_PATTERN.matcher((String) arguments[0]);
                        if (matcher.find()) {
                            String className = matcher.group(1);
                            arguments[0] = Delegates.getSystemService().getLocalized(className);
                        }
                    }
                    Object[] substitutedArguments = HTMLUtils.substituteArguments(user, pageContext, arguments);
                    description = log.toString(format, substitutedArguments);
                } catch (Exception e) {
                    description = log.toString();
                }
                TR tr = new TR();
                String eventDateString = CalendarUtil.format(log.getCreateDate(), CalendarUtil.DATE_WITH_HOUR_MINUTES_SECONDS_FORMAT);
                tr.addElement(new TD().addElement(eventDateString).setClass(Resources.CLASS_LIST_TABLE_TD));
                if (log.getSeverity() == Severity.ERROR) {
                    // to be escaped in js
                    description = "<error>" + description + "</error>";
                }
                tr.addElement(new TD().addElement(description).setClass(Resources.CLASS_LIST_TABLE_TD));
                table.addElement(tr);
            }

            presentationHelper.addTooltip(element, area, table.toString());
        }
    }

    public GraphElementPresentationHelper getPresentationHelper() {
        return presentationHelper;
    }

}
