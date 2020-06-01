import ru.runa.wfe.webservice.AuthenticationAPI;
import ru.runa.wfe.webservice.AuthenticationWebService;
import ru.runa.wfe.webservice.ExecutorAPI;
import ru.runa.wfe.webservice.ExecutorWebService;
import ru.runa.wfe.webservice.User;

public class CreateExecutor {

    public static void main(String[] args) {
        try {
            AuthenticationAPI authenticationAPI = new AuthenticationWebService().getAuthenticationAPIPort();
            User user = authenticationAPI.authenticateByLoginPassword("Administrator", "wf");
            ExecutorAPI executorAPI = new ExecutorWebService().getExecutorAPIPort();
            executorAPI.createActor(user, "testLogin", "FIO", null);
            executorAPI.createGroup(user, "testGroup", null);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
