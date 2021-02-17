package ru.runa.wf.web.tag;

import lombok.Setter;
import org.apache.ecs.html.*;
import org.tldgen.annotations.Attribute;
import org.tldgen.annotations.BodyContent;
import org.tldgen.annotations.Tag;
import ru.runa.common.web.PagingNavigationHelper;
import ru.runa.common.web.StrutsWebHelper;
import ru.runa.common.web.form.FileForm;
import ru.runa.common.web.tag.TitledFormTag;
import ru.runa.wf.web.ftl.component.ViewUtil;
import ru.runa.wfe.chat.dto.broadcast.MessageAddedBroadcast;
import ru.runa.wfe.service.delegate.Delegates;
import ru.runa.wfe.user.User;
import java.text.SimpleDateFormat;
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
        Input sendMessageButton = new Input("button", "sendMessageButton", "Отправить сообщение");
        sendMessageButton.setOnClick("sendMessage()");
        TD sendMessage = new TD(sendMessageButton);
        TD fileInput = new TD(ViewUtil.getFileInput(new StrutsWebHelper(pageContext),
                FileForm.FILE_INPUT_NAME, true, ""));
        sendMessage.setClass("list");
        fileInput.setClass("list");
        return new TR(sendMessage.addElement(" Приватное сообщение: " +
                new Input("checkbox").setID("isPrivate"))).addElement(fileInput);
    }

    private TR getMessageBody(MessageAddedBroadcast message) {
        TD messageText = new TD(message.getText());
        messageText.setClass("list");
        TD files = new TD(message.getFiles().toString());
        files.setClass("list");
        return new TR(messageText).addElement(files);
    }

    private TR getMessageHeader(MessageAddedBroadcast message) {
        Input button = (message.getAuthor().equals(user.getActor()))
                ? getEditMessageButton(message)
                : getReplyButton(message);
        return new TR()
                .addElement(new TH(message.getAuthor().getName()).setAlign("left").addElement(" " + button))
                .addElement(addDeleteMessageButton(getCreateDate(message), message.getId()));
    }

    private TH addDeleteMessageButton(TH th, long messageId) {
        if (Delegates.getExecutorService().isAdministrator(user)) {
            Input deleteButton = new Input("button", "deleteMessageButton", "X");
            deleteButton.setOnClick("deleteMessage(" + messageId + ");");
            return th.addElement(" " + deleteButton);
        }
        return th;
    }

    private TH getCreateDate(MessageAddedBroadcast message) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
        return new TH(dateFormat.format(message.getCreateDate())).setAlign("right");
    }

    private Input getEditMessageButton(MessageAddedBroadcast message) {
        Input button = new Input("button", "editMessageButton", "Изменить сообщение");
        button.setOnClick("editMessage(" + message.getId() + ",\"" + message.getText() + "\");");
        return button;
    }

    private Input getReplyButton(MessageAddedBroadcast message) {
        Input button = new Input("button", "replyButton", "Ответить");
        button.setOnClick("reply(\"" + message.getText() + "\");");
        return button;
    }
}
