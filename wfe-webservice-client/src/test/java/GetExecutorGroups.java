import java.util.List;

import ru.runa.wfe.webservice.AuthenticationAPI;
import ru.runa.wfe.webservice.AuthenticationWebService;
import ru.runa.wfe.webservice.ExecutorAPI;
import ru.runa.wfe.webservice.ExecutorWebService;
import ru.runa.wfe.webservice.Group;
import ru.runa.wfe.webservice.User;
import ru.runa.wfe.webservice.WfExecutor;

public class GetExecutorGroups {

    public static void main(String[] args) {
        try {
            AuthenticationAPI authenticationAPI = new AuthenticationWebService().getAuthenticationAPIPort();
            User user = authenticationAPI.authenticateByLoginPassword("Administrator", "wf");
            ExecutorAPI executorAPI = new ExecutorWebService().getExecutorAPIPort();
            WfExecutor executor = executorAPI.getExecutorByName(user, "Administrators");
            List<Group> groups = executorAPI.getExecutorGroups(user, executor, null, false);
            System.out.println("groups=" + groups);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
