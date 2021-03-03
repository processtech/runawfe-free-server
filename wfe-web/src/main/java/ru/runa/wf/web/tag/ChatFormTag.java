package ru.runa.wf.web.tag;

import lombok.Setter;
import org.apache.ecs.Element;
import org.apache.ecs.StringElement;
import org.apache.ecs.html.A;
import org.apache.ecs.html.Div;
import org.apache.ecs.html.IMG;
import org.apache.ecs.html.Input;
import org.apache.ecs.html.IMG;
import org.apache.ecs.html.TD;
import org.apache.ecs.html.TR;
import org.apache.ecs.html.Table;
import org.apache.ecs.html.TextArea;
import org.tldgen.annotations.Attribute;
import org.tldgen.annotations.BodyContent;
import org.tldgen.annotations.Tag;
import ru.runa.common.web.Commons;
import ru.runa.common.web.PagingNavigationHelper;
import ru.runa.common.web.Resources;
import ru.runa.common.web.tag.TitledFormTag;
import ru.runa.wfe.chat.dto.ChatMessageFileDetailDto;
import ru.runa.wfe.chat.dto.broadcast.MessageAddedBroadcast;
import ru.runa.wfe.commons.CalendarUtil;
import ru.runa.wfe.commons.web.PortletUrlType;
import ru.runa.wfe.security.Permission;
import ru.runa.wfe.security.SecuredObjectType;
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

    private TextArea getTextArea() {
        TextArea textArea = new TextArea();
        textArea.setID("message");
        textArea.addAttribute("placeholder", "Введите текст сообщения");
        textArea.setClass("inputText");
        return textArea;
    }

    private TR getButtons() {
        Input sendMessageButton = new Input("button", "sendMessageButton", "Отправить сообщение");
        sendMessageButton.setOnClick("sendMessage()");

        Input fileInput = new Input("file");
        fileInput.setID("fileInput");
        fileInput.addAttribute("multiple", "true");

        TD fileTd = new TD(fileInput);
        fileTd.setClass("list");

        TD sendMessageTd = new TD(sendMessageButton)
                .addElement(new Div("Приватное сообщение").setClass("text message-card-header private-message-label"))
                .addElement(new Input("checkbox").setID("isPrivate"))
                .addElement(fileTd);
        sendMessageTd.setClass("list");
        return new TR(sendMessageTd);
    }

    private Table createMessages(List<MessageAddedBroadcast> messages) {
        Table table = new Table();
        table.setClass("messages");
        for (MessageAddedBroadcast message : messages) {
            Table messageCard = new Table();
            messageCard.setClass("message-card");
            messageCard.addElement(getMessageHeader(message).setClass("message-header"));
            messageCard.addElement(getFileHolder(message));
            messageCard.addElement(new TR(new TD(message.getText()).setClass("text")));
            table.addElement(new TR(new TD(messageCard)));
        }
        return table;
    }

    private TR getFileHolder(MessageAddedBroadcast message) {
        Table table = new Table();
        table.setClass("fileHolder");
        for (ChatMessageFileDetailDto fileDto : message.getFiles()) {
            table.addElement(new TR(new TD("<a href='/wfe/chatFileOutput?fileId=" + fileDto.getId() +
                    "' download='" + fileDto.getName() + "'>" + fileDto.getName() + "</a>").setClass("link")));
        }
        return new TR(new TD(table));
    }

    private TR getMessageHeader(MessageAddedBroadcast message) {
        final Div authorAndDateTime = new Div();
        authorAndDateTime.setClass("text message-card-header");
        authorAndDateTime.addElement(getAuthor(message)).addElement(" " + CalendarUtil.formatDateTime(message.getCreateDate()) + " ");
        return new TR(new TD()
                .addElement(authorAndDateTime)
                .addElement(getDeleteMessageButton(message))
                .addElement(getEditMessageButton(message))
                .addElement(getReplyButton(message)));
    }

    private Element getAuthor(MessageAddedBroadcast message) {
        if (isAdmin || Delegates.getAuthorizationService().isAllowed(user, Permission.READ,
                SecuredObjectType.EXECUTOR, message.getAuthor().getId())) {
            return new A("/wfe/manage_executor.do?id=" + message.getAuthor().getId(), message.getAuthor().getName()).setClass("link");
        }
        return new StringElement(message.getAuthor().getName());
    }

    private A getDeleteMessageButton(MessageAddedBroadcast message) {
        if (isAdmin) {
            IMG button = new IMG(Commons.getUrl(Resources.IMAGE_DELETE, pageContext, PortletUrlType.Action));
            button.setOnClick("deleteMessage(" + message.getId() + ");");
            return new A().addElement(button.setAlign("right"));
        }
        return null;
    }

    private A getEditMessageButton(MessageAddedBroadcast message) {
        if (message.getAuthor().equals(user.getActor())) {
            IMG button = new IMG().setAlt("Изменить");
            button.setOnClick("editMessage(" + message.getId() + ",\"" + message.getText() + "\");");
            return new A().addElement(button.setAlign("right"));
        }
        return null;
    }

    private A getReplyButton(MessageAddedBroadcast message) {
        IMG button = new IMG().setAlt("Ответить");
        button.setOnClick("reply(\"" + message.getText() + "\");");
        return new A().addElement(button.setAlign("right"));
    }
}
