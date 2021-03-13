package ru.runa.wf.web.tag;

import java.util.List;
import org.apache.ecs.html.TD;
import org.tldgen.annotations.BodyContent;
import ru.runa.af.web.BatchPresentationUtils;
import ru.runa.common.WebResources;
import ru.runa.common.web.PagingNavigationHelper;
import ru.runa.common.web.html.HeaderBuilder;
import ru.runa.common.web.html.ReflectionRowBuilder;
import ru.runa.common.web.html.SortingHeaderBuilder;
import ru.runa.common.web.html.TableBuilder;
import ru.runa.common.web.html.TdBuilder;
import ru.runa.common.web.tag.BatchReturningTitledFormTag;
import ru.runa.wf.web.MessagesProcesses;
import ru.runa.wfe.chat.dto.WfChatRoom;
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
        List<WfChatRoom> chatRooms = Delegates.getChatService().getChatRooms(getUser(), batchPresentation);

        PagingNavigationHelper navigation = new PagingNavigationHelper(pageContext, chatRooms.size());
        navigation.addPagingNavigationTable(tdFormElement);

        TdBuilder[] builders = BatchPresentationUtils.getBuilders(null, batchPresentation, null);
        HeaderBuilder headerBuilder = new SortingHeaderBuilder(batchPresentation, 0, 0, getReturnAction(), pageContext);
        ReflectionRowBuilder rowBuilder = new ReflectionRowBuilder(chatRooms, batchPresentation, pageContext,
                WebResources.ACTION_MAPPING_MANAGE_PROCESS, getReturnAction(), "id", builders);
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

}
