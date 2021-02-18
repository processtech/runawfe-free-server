package ru.runa.wf.web.tag;

import lombok.Setter;
import org.apache.ecs.html.*;
import org.tldgen.annotations.Attribute;
import org.tldgen.annotations.BodyContent;
import org.tldgen.annotations.Tag;
import ru.runa.common.web.PagingNavigationHelper;
import ru.runa.common.web.tag.TitledFormTag;
import ru.runa.wfe.chat.dto.ChatMessageFileDetailDto;
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
        Input fileInput = new Input("file");
        fileInput.setID("fileInput");
        fileInput.addAttribute("multiple", "true");
        TD sendMessage = new TD(sendMessageButton);
        TD file = new TD(fileInput);
        sendMessage.setClass("list");
        file.setClass("list");
        return new TR(sendMessage.addElement(" Приватное сообщение: " +
                new Input("checkbox").setID("isPrivate"))).addElement(file);
    }

    private TR getMessageBody(MessageAddedBroadcast message) {
        TD messageText = new TD(message.getText());
        messageText.setClass("list");
        return new TR(messageText).addElement(getFileHolder(message));
    }

    private TD getFileHolder(MessageAddedBroadcast message) {
        Table table = new Table();
        table.setClass("fileHolder");
        for (ChatMessageFileDetailDto fileDto : message.getFiles()) {
            table.addElement(new TR(new TD("<a href='/wfe/chatFileOutput?fileId=" + fileDto.getId() +
                    "' download='" + fileDto.getName() + "'>" + fileDto.getName() + "</a>")));
        }
        TD td = new TD(table);
        td.setClass("list");
        return td;
    }

    private TR getMessageHeader(MessageAddedBroadcast message) {
        return new TR()
                .addElement(getAuthorAndActionWithMessage(message))
                .addElement(getCreateDateAndDeleteMessageButton(message));
    }

    private TH getCreateDateAndDeleteMessageButton(MessageAddedBroadcast message) {
        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
        TH th = new TH(dateFormat.format(message.getCreateDate())).setAlign("right");

        if (Delegates.getExecutorService().isAdministrator(user)) {
            Input deleteButton = new Input("button", "deleteMessageButton", "X");
            deleteButton.setOnClick("deleteMessage(" + message.getId() + ");");
            return th.addElement(" " + deleteButton);
        }
        return th;
    }

    private TH getAuthorAndActionWithMessage(MessageAddedBroadcast message) {
        Input button = (message.getAuthor().equals(user.getActor()))
                ? getEditMessageButton(message)
                : getReplyButton(message);
        return new TH(message.getAuthor().getName()).setAlign("left").addElement(" " + button);
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
