package ru.runa.wf.web.tag;

import com.google.common.collect.Lists;
import org.apache.ecs.html.TD;
import org.apache.ecs.html.TH;
import org.apache.ecs.html.TR;
import org.apache.ecs.html.Table;
import org.tldgen.annotations.BodyContent;
import org.tldgen.annotations.Tag;
import ru.runa.common.web.PagingNavigationHelper;
import ru.runa.common.web.tag.BatchReturningTitledFormTag;
import ru.runa.wfe.chat.dto.broadcast.MessageAddedBroadcast;
import ru.runa.wfe.service.delegate.Delegates;
import ru.runa.wfe.user.User;
import java.util.List;

@Tag(bodyContent = BodyContent.JSP, name = "chatForm")
public class ChatFormTag extends BatchReturningTitledFormTag {

    private static final long serialVersionUID = -1L;

    @Override
    protected void fillFormElement(TD tdFormElement) {
        Long processId = Long.parseLong(pageContext.getRequest().getParameter("processId"));
        User user = getUser();
        List<MessageAddedBroadcast> messages = Delegates.getChatService()
                .getChatMessages(user, processId, Long.MAX_VALUE, Integer.MAX_VALUE);
        Table table = createTable(messages);

        //ProcessInfoFormTag infoTag = new ProcessInfoFormTag();
        //infoTag.fillFormData(tdFormElement);

        PagingNavigationHelper navigation = new PagingNavigationHelper(pageContext, messages.size());
        navigation.addPagingNavigationTable(tdFormElement);
        tdFormElement.addElement(table);
        navigation.addPagingNavigationTable(tdFormElement);
    }

    private Table createTable(List<MessageAddedBroadcast> messages) {
        Table table = new Table();
        table.setClass("list");
        TR title = new TR();
        title.addElement(createHeader("Текст сообщения"));
        title.addElement(createHeader("Дата создания"));
        title.addElement(createHeader("Автор сообщения"));
        table.addElement(title);
        for (MessageAddedBroadcast message : Lists.reverse(messages)) {
            TR row = new TR();
            row.addElement(createCell(message.getText()));
            row.addElement(createCell(message.getCreateDate().toString()));
            row.addElement(createCell(message.getAuthor().getName()));
            table.addElement(row);
        }
        return table;
    }

    private TH createHeader(String element) {
        TH header = new TH();
        header.setClass("list");
        header.addElement(element);
        return header;
    }

    private TD createCell(String element) {
        TD cell = new TD();
        cell.setClass("list");
        cell.addElement(element);
        return cell;
    }
}
