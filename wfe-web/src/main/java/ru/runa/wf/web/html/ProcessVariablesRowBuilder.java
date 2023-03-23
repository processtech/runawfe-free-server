package ru.runa.wf.web.html;

import java.util.List;
import java.util.Map;
import javax.servlet.jsp.PageContext;
import org.apache.ecs.html.Div;
import org.apache.ecs.html.P;
import org.apache.ecs.html.TD;
import org.apache.ecs.html.TR;
import ru.runa.common.WebResources;
import ru.runa.common.web.Commons;
import ru.runa.common.web.MessagesOther;
import ru.runa.common.web.Resources;
import ru.runa.common.web.StrutsWebHelper;
import ru.runa.common.web.html.RowBuilder;
import ru.runa.wf.web.MessagesProcesses;
import ru.runa.wf.web.ftl.component.ViewUtil;
import ru.runa.wfe.commons.SystemProperties;
import ru.runa.wfe.var.UserType;
import ru.runa.wfe.var.dto.WfVariable;
import ru.runa.wfe.var.format.ListFormat;
import ru.runa.wfe.var.format.MapFormat;
import ru.runa.wfe.var.format.VariableFormat;

public class ProcessVariablesRowBuilder implements RowBuilder {
    private int index = 0;
    private final List<WfVariable> variables;
    private final PageContext pageContext;
    private final Long processId;

    public ProcessVariablesRowBuilder(Long processId, List<WfVariable> variables, PageContext pageContext) {
        this.variables = variables;
        this.processId = processId;
        this.pageContext = pageContext;
    }

    @Override
    public boolean hasNext() {
        return index < variables.size();
    }

    @Override
    public TR buildNext() {
        WfVariable variable = variables.get(index);
        Object value = variable.getValue();
        TR tr = new TR();
        TD nameTd = new TD(variable.getDefinition().getName());
        if (variable.getDefinition().isSynthetic()) {
            nameTd.setStyle("color: #aaaaaa;");
        }
        tr.addElement(nameTd.setClass(Resources.CLASS_LIST_TABLE_TD));

        if (SystemProperties.isGlobalObjectsEnabled()) {
            tr.addElement(new TD(variable.getDefinition().isGlobal() ? /*√*/"&#x221A;" : "").setClass(Resources.CLASS_LIST_TABLE_TD));
        }
        tr.addElement(new TD(variable.getDefinition() != null ? variable.getDefinition().getFormatLabel() : "-").setClass(Resources.CLASS_LIST_TABLE_TD));
        if (WebResources.isDisplayVariablesJavaType()) {
            tr.addElement(new TD(value != null ? value.getClass().getName() : "").setClass(Resources.CLASS_LIST_TABLE_TD));
        }
        if (value != null && WebResources.isVariableHidingEnabled() && isHidingRequired(variable)) {
            tr.addElement(buildHiddenVariable(variable));
        } else {
            String formattedValue = value == null
                    ? MessagesOther.LABEL_UNSET_EMPTY_VALUE.message(pageContext)
                    : ViewUtil.getOutput(Commons.getUser(pageContext.getSession()), new StrutsWebHelper(pageContext), processId, variable);
            tr.addElement(new TD(formattedValue).setClass(Resources.CLASS_LIST_TABLE_TD));
        }
        index++;
        return tr;
    }

    @Override
    public List<TR> buildNextArray() {
        return null;
    }

    private boolean isHidingRequired(WfVariable variable) {
        VariableFormat format = variable.getDefinition().getFormatNotNull();
        if (format instanceof ListFormat) {
            int collectionSize = ((List) variable.getValue()).size();
            int numberOfAttributes = collectionSize * countAttributes(((ListFormat) format).getComponentUserType(0));
            return numberOfAttributes > WebResources.getProcessVariableNumberOfAttributesToHide();
        }
        if (format instanceof MapFormat) {
            int collectionSize = ((Map) variable.getValue()).size();
            int numberOfKeyAttributes = collectionSize * countAttributes(((MapFormat) format).getComponentUserType(0));
            int numberOfValueAttributes = collectionSize * countAttributes(((MapFormat) format).getComponentUserType(1));
            return numberOfKeyAttributes + numberOfValueAttributes > WebResources.getProcessVariableNumberOfAttributesToHide();
        }
        return countAttributes(variable.getDefinition().getUserType()) > WebResources.getProcessVariableNumberOfAttributesToHide();
    }

    private int countAttributes(UserType userType) {
        int count = userType != null ? userType.getAttributes().size() : 0;
        if (count > WebResources.getProcessVariableNumberOfAttributesToHide()) {
            return count;
        }
        return count == 0 ? 0 : count + userType.getAttributes().stream()
                .map(v -> v != null ? countAttributes(v.getUserType()) : 0)
                .mapToInt(Integer::intValue).sum();
    }

    private TD buildHiddenVariable(WfVariable variable) {
        TD hiddenVariable = new TD();
        P variableContent = new P();
        variableContent.setClass("hiddenVariableContent");
        variableContent.addAttribute("opened", false);
        variableContent.addAttribute("loaded", false);
        variableContent.addAttribute("variableName", variable.getDefinition().getName());
        variableContent.addAttribute("index", index);
        variableContent.addAttribute("date", "" + pageContext.getRequest().getParameter("date"));
        variableContent.addElement(MessagesProcesses.LABEL_EXPAND.message(pageContext));
        hiddenVariable.addElement(variableContent);
        Div contentDiv = new Div();
        contentDiv.setID("content" + index);
        hiddenVariable.addElement(contentDiv);
        hiddenVariable.setClass(Resources.CLASS_LIST_TABLE_TD);
        return hiddenVariable;
    }
}
