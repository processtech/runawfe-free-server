import java.util.ArrayList;
import java.util.List;

import ru.runa.wfe.webservice.AuthenticationAPI;
import ru.runa.wfe.webservice.AuthenticationWebService;
import ru.runa.wfe.webservice.ExecutionAPI;
import ru.runa.wfe.webservice.ExecutionWebService;
import ru.runa.wfe.webservice.User;
import ru.runa.wfe.webservice.Variable;

public class StartProcess {

    public static void main(String[] args) {
        try {
            AuthenticationAPI authenticationAPI = new AuthenticationWebService().getAuthenticationAPIPort();
            User user = authenticationAPI.authenticateByLoginPassword("Administrator", "wf");
            ExecutionAPI executionAPI = new ExecutionWebService().getExecutionAPIPort();

            List<Variable> variables = new ArrayList<Variable>();

            Variable usersListVariable = new Variable();
            usersListVariable.setName("UserList");
            usersListVariable.setValue("[{\"name\": \"julius\"}, {\"name\": \"nero\"}, {\"name\": \"Administrator\"}]");
            variables.add(usersListVariable);

            Long processId = executionAPI.startProcessWS(user, "messagingFailureTest", variables);
            System.out.println("Started process " + processId);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
