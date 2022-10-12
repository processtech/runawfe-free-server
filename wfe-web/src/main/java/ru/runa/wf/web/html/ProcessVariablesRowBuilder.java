package ru.runa.wf.web.html;

import java.util.List;
import javax.servlet.jsp.PageContext;
import org.apache.ecs.html.TD;
import org.apache.ecs.html.TR;
import ru.runa.common.WebResources;
import ru.runa.common.web.Commons;
import ru.runa.common.web.MessagesOther;
import ru.runa.common.web.Resources;
import ru.runa.common.web.StrutsWebHelper;
import ru.runa.common.web.html.RowBuilder;
import ru.runa.wf.web.ftl.component.ViewUtil;
import ru.runa.wfe.commons.SystemProperties;
import ru.runa.wfe.user.User;
import ru.runa.wfe.var.dto.WfVariable;

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
            TD globalTD = new TD(variable.getDefinition().isGlobal() ? /*√*/"&#x221A;" : "");
            tr.addElement(globalTD);
            globalTD.setClass(ru.runa.common.web.Resources.CLASS_LIST_TABLE_TD);
        }
        String fl = variable.getDefinition() != null ? variable.getDefinition().getFormatLabel() : "-";
        tr.addElement(new TD(fl).setClass(Resources.CLASS_LIST_TABLE_TD));


        if (WebResources.isDisplayVariablesJavaType()) {
            String className = value != null ? value.getClass().getName() : "";
            tr.addElement(new TD(className).setClass(Resources.CLASS_LIST_TABLE_TD));
        }
        String formattedValue;
        if (value == null) {
            formattedValue = MessagesOther.LABEL_UNSET_EMPTY_VALUE.message(pageContext);
        } else {
            User user = Commons.getUser(pageContext.getSession());
            formattedValue = ViewUtil.getOutput(user, new StrutsWebHelper(pageContext), processId, variable);
        }
        tr.addElement(new TD(formattedValue).setClass(Resources.CLASS_LIST_TABLE_TD));
        index++;
        return tr;
    }

    @Override
    public List<TR> buildNextArray() {
        return null;
    }

}
