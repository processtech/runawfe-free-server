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
    private User user;

    @Setter
    @Attribute(required = true)
    private Long processId;

    @Override
    protected boolean isSubmitButtonVisible() {
        return false;
    }

    @Override
    protected void fillFormElement(TD tdFormElement) {
        user = getUser();
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
        table.addElement(getTextArea());
        table.addElement(getButtons());
        for (MessageAddedBroadcast message : messages) {
            table.addElement(getMessageHeader(message));
            table.addElement(getMessageBody(message));
        }
        return table;
    }

    private TextArea getTextArea() {
        TextArea textArea = new TextArea();
        textArea.setID("message");
        textArea.addAttribute("placeholder", "Введите текст сообщения");
        return textArea;
    }

    private TR getButtons() {
        Input input = new Input("button", "sendMessageButton", "Отправить сообщение");
        input.setOnClick("sendMessage()");
        TD sendMessage = new TD(input);
        sendMessage.setClass("list");
        TD isPrivate = new TD("Приватное сообщение: " + new Input("checkbox").setID("isPrivate"));
        isPrivate.setClass("list");
        return new TR(sendMessage).addElement(isPrivate.addElement(getAddFilesButton()));
    }

    private Input getAddFilesButton() {
        Input input = new Input("file");
        input.addAttribute("multiple", "true");
        input.setOnClick("alert(\"File added!\")");
        return input;
    }

    private TR getMessageHeader(MessageAddedBroadcast message) {
        TR row = new TR();
        row.addElement(new TH(message.getAuthor().getName()).setAlign("left"));
        row.addElement(addDeleteButton(new TH(message.getCreateDate().toString()).setAlign("right"), message.getId()));
        return row;
    }

    private TR getMessageBody(MessageAddedBroadcast message) {
        TD messageText = new TD(message.getText());
        messageText.setClass("list");
        TD files = new TD(message.getFiles().toString());
        files.setClass("list");
        return new TR(messageText).addElement(addEditButton(files, message).setAlign("right"));
    }

    private TH addDeleteButton(TH th, long messageId) {
        if (Delegates.getExecutorService().isAdministrator(user)) {
            Input deleteButton = new Input("button", "deleteMessageButton", "X");
            deleteButton.setOnClick("deleteMessage(" + messageId + ");");
            return th.addElement(deleteButton);
        }
        return th;
    }

    private TD addEditButton(TD td, MessageAddedBroadcast message) {
        if (message.getAuthor().equals(user.getActor())) {
            Input editButton = new Input("button", "editMessageButton", "Изменить сообщение");
            editButton.setOnClick("editMessage(" + message.getId() + ",\"" + message.getText() + "\");");
            return td.addElement(editButton);
        }
        return td;
    }
}
