package ru.runa.wf.web.tag;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import javax.servlet.jsp.PageContext;
import org.apache.ecs.html.TD;
import org.tldgen.annotations.BodyContent;
import ru.runa.af.web.BatchPresentationUtils;
import ru.runa.common.WebResources;
import ru.runa.common.web.Commons;
import ru.runa.common.web.PagingNavigationHelper;
import ru.runa.common.web.html.HeaderBuilder;
import ru.runa.common.web.html.ItemUrlStrategy;
import ru.runa.common.web.html.ReflectionRowBuilder;
import ru.runa.common.web.html.SortingHeaderBuilder;
import ru.runa.common.web.html.TableBuilder;
import ru.runa.common.web.html.TdBuilder;
import ru.runa.common.web.tag.BatchReturningTitledFormTag;
import ru.runa.wf.web.MessagesProcesses;
import ru.runa.wfe.chat.dto.WfChatRoom;
import ru.runa.wfe.commons.web.PortletUrlType;
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
        BatchPresentation batchPresentation = getBatchPresentation();
        List<WfChatRoom> chatRooms = Delegates.getChatService().getChatRooms(getUser());

        PagingNavigationHelper navigation = new PagingNavigationHelper(pageContext, chatRooms.size());
        navigation.addPagingNavigationTable(tdFormElement);

        TdBuilder[] builders = BatchPresentationUtils.getBuilders(null, batchPresentation, null);
        HeaderBuilder headerBuilder = new SortingHeaderBuilder(batchPresentation, new String[0], new String[0], getReturnAction(), pageContext);

        ReflectionRowBuilder rowBuilder = new ReflectionRowBuilder(chatRooms, batchPresentation, pageContext,
                WebResources.ACTION_MAPPING_CHAT_PAGE, getReturnAction(), new ChatUrlStrategy(pageContext), builders);

        tdFormElement.addElement(new TableBuilder().build(headerBuilder, rowBuilder, false));

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

    static class ChatUrlStrategy implements ItemUrlStrategy {
        private final PageContext pageContext;
        private final String param;

        public ChatUrlStrategy(PageContext pageContext) {
            this.pageContext = pageContext;
            this.param = "processId";
        }

        @Override
        public String getUrl(String baseUrl, Object item) {
            WfChatRoom chatRoom = (WfChatRoom) item;
            Long processId = chatRoom.getId();
            Map<String, Object> map = new HashMap<>();
            map.put(param, processId);
            return Commons.getActionUrl(baseUrl, map, pageContext, PortletUrlType.Action);
        }
    }
}
