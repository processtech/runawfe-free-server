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
public class ChatWebServiceTest {
    public static final ChatAPI chatAPI;
    public static final User USER;
    public static final Long PROCESS_ID = 1L;
    // Specify the ID of the message that you want to edit and delete
    public static final Long MESSAGE_ID = 66L;

    static {
        AuthenticationAPI authenticationAPI = new AuthenticationWebService().getAuthenticationAPIPort();
        USER = authenticationAPI.authenticateByLoginPassword("Administrator", "wf");
        chatAPI = new ChatWebService().getChatAPIPort();
    }

    public static void main(String[] args) throws InterruptedException {
        sendMessageTest("New Message in test");
        Thread.sleep(1000);
        updateMessageTest(MESSAGE_ID, "Edit message ID: " + MESSAGE_ID);
        Thread.sleep(1000);
        deleteMessageTest(MESSAGE_ID);
    }

    public static void sendMessageTest(String message) {
        AddMessageRequest request = new AddMessageRequest();
        request.setMessage(message);
        request.setProcessId(PROCESS_ID);
        chatAPI.saveMessage(USER, request);
    }

    public static void updateMessageTest(Long id, String message) {
        EditMessageRequest request = new EditMessageRequest();
        request.setEditMessageId(id);
        request.setMessage(message);
        request.setProcessId(PROCESS_ID);
        chatAPI.updateMessage(USER, request);
    }

    public static void deleteMessageTest(Long id) {
        DeleteMessageRequest request = new DeleteMessageRequest();
        request.setMessageId(id);
        request.setProcessId(PROCESS_ID);
        chatAPI.deleteMessage(USER, request);
    }
}