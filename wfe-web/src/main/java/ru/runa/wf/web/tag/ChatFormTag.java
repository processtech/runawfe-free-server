package ru.runa.wf.web.tag;

import com.google.common.collect.Lists;
import lombok.Setter;
import org.apache.ecs.html.TD;
import org.apache.ecs.html.TH;
import org.apache.ecs.html.TR;
import org.apache.ecs.html.Table;
import org.apache.ecs.html.TextArea;
import org.apache.ecs.html.Input;
import org.tldgen.annotations.Attribute;
import org.tldgen.annotations.BodyContent;
import org.tldgen.annotations.Tag;
import ru.runa.common.web.PagingNavigationHelper;
import ru.runa.common.web.tag.TitledFormTag;
import ru.runa.wfe.chat.dto.broadcast.MessageAddedBroadcast;
import ru.runa.wfe.service.delegate.Delegates;
import ru.runa.wfe.user.User;
import java.util.List;

@Tag(bodyContent = BodyContent.JSP, name = "chatForm")
public class ChatFormTag extends TitledFormTag {

    private static final long serialVersionUID = -1L;

    @Setter
    @Attribute(required = true)
    private Long processId;

    @Override
    protected boolean isSubmitButtonVisible() {
        return false;
    }

    @Override
    protected void fillFormElement(TD tdFormElement) {
        User user = getUser();
        List<MessageAddedBroadcast> messages = Delegates.getChatService()
                .getChatMessages(user, processId, Long.MAX_VALUE, Integer.MAX_VALUE);

        PagingNavigationHelper navigation = new PagingNavigationHelper(pageContext, messages.size());
        navigation.addPagingNavigationTable(tdFormElement);
        tdFormElement.addElement(createTable(messages));
        navigation.addPagingNavigationTable(tdFormElement);
    }

    private Table createTable(List<MessageAddedBroadcast> messages) {
        Table table = new Table();
        table.setClass("list");
        table.addElement(createTextArea());
        table.addElement(createSubmitButton());
        table.addElement(createTitle());
        for (MessageAddedBroadcast message : Lists.reverse(messages)) {
            table.addElement(createRow(message));
        }
        return table;
    }

    private TextArea createTextArea() {
        TextArea textArea = new TextArea();
        textArea.setID("message");
        textArea.setName("message");
        textArea.addAttribute("placeholder", "Введите текст сообщения");
        return textArea;
    }

    private Input createSubmitButton() {
        Input input = new Input();
        input.setOnClick("alert(\"Clicked!\")");
        input.setValue("Отправить сообщение");
        input.setClass("button");
        input.setName("submitButton");
        input.setType("SUBMIT");
        return input;
    }

    private TR createTitle() {
        TR title = new TR();
        title.addElement(createHeader("Текст сообщения"));
        title.addElement(createHeader("Дата создания"));
        title.addElement(createHeader("Автор сообщения"));
        return title;
    }

    private TR createRow(MessageAddedBroadcast message) {
        TR row = new TR();
        row.addElement(createCell(message.getText()));
        row.addElement(createCell(message.getCreateDate().toString()));
        row.addElement(createCell(message.getAuthor().getName()));
        return row;
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
