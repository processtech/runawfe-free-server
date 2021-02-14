package ru.runa.wf.web.tag;

import lombok.Setter;
import org.apache.ecs.html.*;
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
        for (MessageAddedBroadcast message : messages) {
            table.addElement(createHead(message));
            table.addElement(createBody(message.getText()));
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
        Input input = new Input("SUBMIT", "submitButton", "Отправить сообщение");
        input.setClass("button");
        input.setOnClick("alert(\"Clicked!\")");
        return input;
    }

    private TR createHead(MessageAddedBroadcast message) {
        TR row = new TR();
        row.addElement(new TH(message.getAuthor().getName()).setAlign("left"));
        row.addElement(new TH(message.getCreateDate().toString()).setAlign("right"));
        return row;
    }

    private TD createBody(String text) {
        TD td = new TD(text);
        td.setClass("list");
        return td;
    }
}
