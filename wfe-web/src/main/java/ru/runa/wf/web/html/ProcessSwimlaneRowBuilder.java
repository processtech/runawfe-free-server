package ru.runa.wf.web.html;

import com.google.common.base.Strings;
import java.util.List;
import java.util.Optional;
import javax.servlet.jsp.PageContext;
import org.apache.ecs.html.TD;
import org.apache.ecs.html.TR;
import ru.runa.common.web.HTMLUtils;
import ru.runa.common.web.MessagesOther;
import ru.runa.common.web.html.RowBuilder;
import ru.runa.wfe.commons.SystemProperties;
import ru.runa.wfe.execution.dto.WfSwimlane;
import ru.runa.wfe.lang.SwimlaneDefinition;

public class ProcessSwimlaneRowBuilder implements RowBuilder {
    private final PageContext pageContext;
    private final List<SwimlaneDefinition> swimlaneDefinitions;
    private final List<WfSwimlane> swimlanes;
    private int currentIndex = 0;

    public ProcessSwimlaneRowBuilder(List<SwimlaneDefinition> swimlaneDefinitions, List<WfSwimlane> swimlanes, PageContext pageContext) {
        this.swimlaneDefinitions = swimlaneDefinitions;
        this.swimlanes = swimlanes;
        this.pageContext = pageContext;
    }

    @Override
    public boolean hasNext() {
        return currentIndex < swimlanes.size();
    }

    @Override
    public TR buildNext() {
        TR tr = new TR();
        SwimlaneDefinition swimlaneDefinition = swimlaneDefinitions.get(currentIndex++);

        TD nameTD = new TD(swimlaneDefinition.getName());
        tr.addElement(nameTD);
        nameTD.setClass(ru.runa.common.web.Resources.CLASS_LIST_TABLE_TD);

        if (SystemProperties.isGlobalObjectsEnabled()) {
            TD globalTD = new TD(swimlaneDefinition.isGlobal() ? /* √ */"&#x221A;" : "");
            tr.addElement(globalTD);
            globalTD.setClass(ru.runa.common.web.Resources.CLASS_LIST_TABLE_TD);
        }

        Optional<WfSwimlane> swimlane = swimlanes.stream().filter(s -> s.getName().equals(swimlaneDefinition.getName())).findFirst();
        TD assignedToExecutorTD = new TD();
        tr.addElement(assignedToExecutorTD);
        assignedToExecutorTD.setClass(ru.runa.common.web.Resources.CLASS_LIST_TABLE_TD);
        assignedToExecutorTD.addElement(HTMLUtils.createExecutorElement(pageContext, swimlane.isPresent() ? swimlane.get().getExecutor() : null));

        TD organizationFunctionTD = new TD();
        tr.addElement(organizationFunctionTD);
        organizationFunctionTD.setClass(ru.runa.common.web.Resources.CLASS_LIST_TABLE_TD);
        String swimlaneInitializer = swimlaneDefinition.getOrgFunctionLabel();
        if (Strings.isNullOrEmpty(swimlaneInitializer)) {
            swimlaneInitializer = MessagesOther.LABEL_UNSET_EMPTY_VALUE.message(pageContext);
        }
        organizationFunctionTD.addElement(swimlaneInitializer);
        return tr;
    }

    public int getEnabledRowsCount() {
        return swimlanes.size();
    }

    @Override
    public List<TR> buildNextArray() {
        return null;
    }
}
