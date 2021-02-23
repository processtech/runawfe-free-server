package ru.runa.wf.web.tag;

import lombok.Setter;
import org.apache.ecs.html.A;
import org.apache.ecs.html.Input;
import org.apache.ecs.html.TD;
import org.apache.ecs.html.TR;
import org.apache.ecs.html.Table;
import org.apache.ecs.html.TextArea;
import org.tldgen.annotations.Attribute;
import org.tldgen.annotations.BodyContent;
import org.tldgen.annotations.Tag;
import ru.runa.common.web.PagingNavigationHelper;
import ru.runa.common.web.tag.TitledFormTag;
import ru.runa.wfe.chat.dto.ChatMessageFileDetailDto;
import ru.runa.wfe.chat.dto.broadcast.MessageAddedBroadcast;
import ru.runa.wfe.commons.CalendarUtil;
import ru.runa.wfe.service.delegate.Delegates;
import ru.runa.wfe.user.User;
import java.util.List;

@Tag(bodyContent = BodyContent.JSP, name = "chatForm")
public class ChatFormTag extends TitledFormTag {

    private static final long serialVersionUID = 3503222160684440119L;
    private User user;
    private boolean isAdmin;

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
        isAdmin = Delegates.getExecutorService().isAdministrator(user);
        List<MessageAddedBroadcast> messages = Delegates.getChatService().getMessages(user, processId);

        PagingNavigationHelper navigation = new PagingNavigationHelper(pageContext, messages.size());
        navigation.addPagingNavigationTable(tdFormElement);
        tdFormElement.addElement(new Table().addElement(getTextArea()).addElement(getButtons()));
        tdFormElement.addElement(createMessages(messages));
        navigation.addPagingNavigationTable(tdFormElement);
    }

    private Table createMessages(List<MessageAddedBroadcast> messages) {
        Table messagesTable = new Table();
        messagesTable.setClass("messages");
        for (MessageAddedBroadcast message : messages) {
            Table messageCard = new Table();
            messageCard.setClass("message-card");
            messageCard.addElement(getMessageHeader(message).setClass("message-header"));
            messageCard.addElement(getFileHolder(message));
            messageCard.addElement(new TR(new TD(message.getText())));
            TD td = new TD();
            td.addElement(messageCard);
            messagesTable.addElement(new TR(td));
        }
        return messagesTable;
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

    private TR getFileHolder(MessageAddedBroadcast message) {
        Table table = new Table();
        table.setClass("fileHolder");
        for (ChatMessageFileDetailDto fileDto : message.getFiles()) {
            table.addElement(new TR(new TD("<a href='/wfe/chatFileOutput?fileId=" + fileDto.getId() +
                    "' download='" + fileDto.getName() + "'>" + fileDto.getName() + "</a>")));
        }
        return new TR(new TD(table));
    }

    private TR getMessageHeader(MessageAddedBroadcast message) {
        return new TR(new TD()
                .addElement(getAuthorAndActionWithMessage(message))
                .addElement(" " + CalendarUtil.formatDateTime(message.getCreateDate()) + " ")
        ).addElement(new TD()
                .addElement((message.getAuthor().equals(user.getActor()))
                        ? getEditMessageButton(message)
                        : getReplyButton(message))
                .addElement(isAdmin
                        ? " " + getDeleteMessageButton(message)
                        : "").setAlign("right"));
    }

    private A getAuthorAndActionWithMessage(MessageAddedBroadcast message) {
        return new A("/wfe/manage_executor.do?id=" + message.getAuthor().getId(), message.getAuthor().getName());
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

    private Input getDeleteMessageButton(MessageAddedBroadcast message) {
        Input deleteButton = new Input("button", "deleteMessageButton", "X");
        deleteButton.setOnClick("deleteMessage(" + message.getId() + ");");
        return deleteButton;
    }
}
