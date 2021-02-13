import ru.runa.wfe.webservice.AddMessageRequest;
import ru.runa.wfe.webservice.AuthenticationAPI;
import ru.runa.wfe.webservice.AuthenticationWebService;
import ru.runa.wfe.webservice.ChatAPI;
import ru.runa.wfe.webservice.ChatWebService;
import ru.runa.wfe.webservice.DeleteMessageRequest;
import ru.runa.wfe.webservice.EditMessageRequest;
import ru.runa.wfe.webservice.MessageAddedBroadcast;
import ru.runa.wfe.webservice.MessageDeletedBroadcast;
import ru.runa.wfe.webservice.MessageEditedBroadcast;
import ru.runa.wfe.webservice.User;

/**
 * @author Sergey Inyakin
 */
public class ChatWebServiceTest {
    public static final ChatAPI chatAPI;
    public static final User USER;
    public static final Long PROCESS_ID = 1L;

    static {
        AuthenticationAPI authenticationAPI = new AuthenticationWebService().getAuthenticationAPIPort();
        USER = authenticationAPI.authenticateByLoginPassword("Administrator", "wf");
        chatAPI = new ChatWebService().getChatAPIPort();
    }

    public static void main(String[] args) throws InterruptedException {
        MessageAddedBroadcast addedBroadcast = sendMessageTest("New Message in test 123");
        sendMessageTest("2 new message");
        Thread.sleep(5000);
        MessageEditedBroadcast editedBroadcast = updateMessageTest(addedBroadcast.getId(), "Edit message");
        Thread.sleep(5000);
        MessageDeletedBroadcast deletedBroadcast = deleteMessageTest(editedBroadcast.getEditMessageId());
    }

    public static MessageAddedBroadcast sendMessageTest(String message) {
        AddMessageRequest request = new AddMessageRequest();
        request.setMessage(message);
        request.setProcessId(PROCESS_ID);
        return chatAPI.saveMessage(USER, PROCESS_ID, request);
    }

    public static MessageEditedBroadcast updateMessageTest(Long id, String message) {
        EditMessageRequest request = new EditMessageRequest();
        request.setEditMessageId(id);
        request.setMessage(message);
        request.setProcessId(PROCESS_ID);
        return chatAPI.updateMessage(USER, request);
    }

    public static MessageDeletedBroadcast deleteMessageTest(Long id) {
        DeleteMessageRequest request = new DeleteMessageRequest();
        request.setMessageId(id);
        request.setProcessId(PROCESS_ID);
        return chatAPI.deleteMessage(USER, request);
    }
}
