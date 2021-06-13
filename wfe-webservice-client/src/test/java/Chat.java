import ru.runa.wfe.webservice.AddMessageRequest;
import ru.runa.wfe.webservice.AuthenticationAPI;
import ru.runa.wfe.webservice.AuthenticationWebService;
import ru.runa.wfe.webservice.ChatAPI;
import ru.runa.wfe.webservice.ChatWebService;
import ru.runa.wfe.webservice.DeleteMessageRequest;
import ru.runa.wfe.webservice.EditMessageRequest;
import ru.runa.wfe.webservice.User;

/**
 * @author Sergey Inyakin
 */
public class Chat {
    public static final ChatAPI chatAPI;
    public static final User USER;
    public static final Long PROCESS_ID = 1L;

    static {
        AuthenticationAPI authenticationAPI = new AuthenticationWebService().getAuthenticationAPIPort();
        USER = authenticationAPI.authenticateByLoginPassword("Administrator", "wf");
        chatAPI = new ChatWebService().getChatAPIPort();
    }

    public static void main(String[] args) throws InterruptedException {
        final Long messageId = sendMessageTest("New Message in test");
        Thread.sleep(1000);
        updateMessageTest(messageId, "Edit message ID: " + messageId);
        Thread.sleep(1000);
        deleteMessageTest(messageId);
    }

    public static Long sendMessageTest(String message) {
        AddMessageRequest request = new AddMessageRequest();
        request.setMessage(message);
        request.setProcessId(PROCESS_ID);
        return chatAPI.saveMessage(USER, request);
    }

    public static void updateMessageTest(Long id, String message) {
        EditMessageRequest request = new EditMessageRequest();
        request.setEditMessageId(id);
        request.setMessage(message);
        request.setProcessId(PROCESS_ID);
        chatAPI.editMessage(USER, request);
    }

    public static void deleteMessageTest(Long id) {
        DeleteMessageRequest request = new DeleteMessageRequest();
        request.setMessageId(id);
        request.setProcessId(PROCESS_ID);
        chatAPI.deleteMessage(USER, request);
    }
}