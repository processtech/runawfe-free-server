package ru.runa.wf.web.tag;

import lombok.Setter;
import org.apache.ecs.html.A;
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

    private Table createMessages(List<MessageAddedBroadcast> messages) {
        Table table = new Table();
        table.setClass("messages");
        for (MessageAddedBroadcast message : messages) {
            Table messageCard = new Table();
            messageCard.setClass("message-card");
            messageCard.addElement(getMessageHeader(message).setClass("message-header"));
            messageCard.addElement(getFileHolder(message));
            messageCard.addElement(new TR(new TD(message.getText())));
            table.addElement(new TR(new TD(messageCard)));
        }
        return table;
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
                .addElement(getAuthor(message))
                .addElement(" " + CalendarUtil.formatDateTime(message.getCreateDate()) + " ")
                .addElement(getDeleteMessageButton(message))
                .addElement(getActionWithMessage(message)));
    }

    private A getAuthor(MessageAddedBroadcast message) {
        if (Delegates.getAuthorizationService().isAllowed(user, Permission.READ,
                SecuredObjectType.EXECUTOR, message.getAuthor().getIdentifiableId())) {
            return new A("/wfe/manage_executor.do?id=" + message.getAuthor().getId(), message.getAuthor().getName());
        }
        return new A().addElement(message.getAuthor().getName());
    }

    private A getDeleteMessageButton(MessageAddedBroadcast message) {
        A a = new A();
        if (isAdmin) {
            IMG button = new IMG(Commons.getUrl(Resources.IMAGE_DELETE, pageContext, PortletUrlType.Action));
            button.setOnClick("deleteMessage(" + message.getId() + ");");
            a.addElement(button.setAlign("right"));
        }
        return a;
    }

    private A getActionWithMessage(MessageAddedBroadcast message) {
        IMG button = new IMG();
        if (message.getAuthor().equals(user.getActor())) {
            button.setAlt("Изменить").setOnClick("editMessage(" + message.getId() + ",\"" + message.getText() + "\");");
        } else {
            button.setAlt("Ответить").setOnClick("reply(\"" + message.getText() + "\");");
        }
        return new A().addElement(button.setAlign("right"));
    }
}
