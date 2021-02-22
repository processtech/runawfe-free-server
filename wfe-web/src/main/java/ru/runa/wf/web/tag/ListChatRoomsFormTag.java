package ru.runa.wf.web.tag;

import com.google.common.collect.Maps;
import java.util.List;
import java.util.Map;
import org.apache.ecs.html.A;
import org.apache.ecs.html.TD;
import org.apache.ecs.html.TH;
import org.apache.ecs.html.TR;
import org.apache.ecs.html.Table;
import org.tldgen.annotations.BodyContent;
import ru.runa.common.WebResources;
import ru.runa.common.web.Commons;
import ru.runa.common.web.Messages;
import ru.runa.common.web.PagingNavigationHelper;
import ru.runa.common.web.Resources;
import ru.runa.common.web.html.ItemUrlStrategy;
import ru.runa.common.web.tag.BatchReturningTitledFormTag;
import ru.runa.wf.web.MessagesProcesses;
import ru.runa.wfe.commons.web.PortletUrlType;
import ru.runa.wfe.execution.dto.WfProcess;
import ru.runa.wfe.presentation.BatchPresentation;
import ru.runa.wfe.service.delegate.Delegates;

/**
 * Created on 18.02.2021
 *
 * @author Sergey Inyakin
 */
@org.tldgen.annotations.Tag(bodyContent = BodyContent.JSP, name = "listChatRoomsForm")
public class ListChatRoomsFormTag extends BatchReturningTitledFormTag {

    @Override
    protected void fillFormElement(TD tdFormElement) {
        final String PROCESS_ID = "batch_presentation.process.id";
        final String PROCESS_DEFINITION_NAME = "batch_presentation.process.definition_name";
        final String NEW_MESSAGES_COUNT = "chat_rooms.new_messages";

        final BatchPresentation batchPresentation = getBatchPresentation();
        final List<WfProcess> processes = Delegates.getExecutionService().getProcesses(getUser(), batchPresentation);
        final Map<Long, Long> activeChats = Delegates.getChatService().getNewMessagesCounts(getUser());

        if (processes.isEmpty()) {
            return;
        }

        PagingNavigationHelper navigation = new PagingNavigationHelper(pageContext, batchPresentation, processes.size(), getReturnAction());
        navigation.addPagingNavigationTable(tdFormElement);

        Table table = new Table();
        tdFormElement.addElement(table);
        table.setClass(Resources.CLASS_LIST_TABLE);
        table.setID("chatRooms");
        TR headerTr = new TR();
        table.addElement(headerTr);
        headerTr.addElement(new TH(Messages.getMessage(PROCESS_ID, pageContext)).setClass(Resources.CLASS_LIST_TABLE_TH));
        headerTr.addElement(new TH(Messages.getMessage(PROCESS_DEFINITION_NAME, pageContext)).setClass(Resources.CLASS_LIST_TABLE_TH));
        headerTr.addElement(new TH(Messages.getMessage(NEW_MESSAGES_COUNT, pageContext)).setClass(Resources.CLASS_LIST_TABLE_TH));

        for (WfProcess process : processes) {
            TR row = new TR();
            table.addElement(row);

            ProcessUrlStrategy processUrlStrategy = new ProcessUrlStrategy();
            String processUrl = processUrlStrategy.getUrl(WebResources.ACTION_MAPPING_MANAGE_PROCESS, process);
            Long processId = process.getId();
            TD processIdTd = new TD(new A(processUrl, String.valueOf(processId)));
            processIdTd.setClass(Resources.CLASS_LIST_TABLE_TD);
            row.addElement(processIdTd);

            TD processNameTd = new TD(new A(processUrl, process.getName()));
            processNameTd.setClass(Resources.CLASS_LIST_TABLE_TD);
            row.addElement(processNameTd);

            ChatUrlStrategy chatUrlStrategy = new ChatUrlStrategy();
            String chatUrl = chatUrlStrategy.getUrl(WebResources.ACTION_MAPPING_CHAT_PAGE, process);
            String newMessageCountStr = activeChats.get(processId) == null ? "0" : String.valueOf(activeChats.get(processId));
            TD newMessageCountTd = new TD(new A(chatUrl, newMessageCountStr));
            newMessageCountTd.setClass(Resources.CLASS_LIST_TABLE_TD);
            row.addElement(newMessageCountTd);
        }

        navigation.addPagingNavigationTable(tdFormElement);
    }

    @Override
    protected String getTitle() {
        return MessagesProcesses.TITLE_CHAT_ROOMS.message(pageContext);
    }

    @Override
    protected boolean isSubmitButtonEnabled() {
        return false;
    }

    @Override
    protected boolean isSubmitButtonVisible() {
        return false;
    }

    class ChatUrlStrategy implements ItemUrlStrategy {
        public static final String PROCESS_ID = "processId";

        @Override
        public String getUrl(String baseUrl, Object item) {
            WfProcess process = (WfProcess) item;
            Long processId = process.getId();
            Map<String, Object> map = Maps.newHashMap();
            map.put(PROCESS_ID, processId);
            return Commons.getActionUrl(baseUrl, map, pageContext, PortletUrlType.Action);
        }
    }

    class ProcessUrlStrategy implements ItemUrlStrategy {
        public static final String PROCESS_ID = "id";

        @Override
        public String getUrl(String baseUrl, Object item) {
            WfProcess process = (WfProcess) item;
            Long processId = process.getId();
            Map<String, Object> map = Maps.newHashMap();
            map.put(PROCESS_ID, processId);
            return Commons.getActionUrl(baseUrl, map, pageContext, PortletUrlType.Action);
        }
    }
}
