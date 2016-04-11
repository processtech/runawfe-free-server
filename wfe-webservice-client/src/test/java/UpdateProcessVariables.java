import java.util.ArrayList;
import java.util.List;

import ru.runa.wfe.webservice.AuthenticationAPI;
import ru.runa.wfe.webservice.AuthenticationWebService;
import ru.runa.wfe.webservice.ExecutionAPI;
import ru.runa.wfe.webservice.ExecutionWebService;
import ru.runa.wfe.webservice.User;
import ru.runa.wfe.webservice.Variable;

public class UpdateProcessVariables {

    public static void main(String[] args) {
        try {
            AuthenticationAPI authenticationAPI = new AuthenticationWebService().getAuthenticationAPIPort();
            User user = authenticationAPI.authenticateByLoginPassword("Administrator", "wf");
            ExecutionAPI executionAPI = new ExecutionWebService().getExecutionAPIPort();
            List<Variable> variables = new ArrayList<Variable>();

            Variable variable2 = new Variable();
            variable2.setName("selected row id");
            variable2.setValue("1");
            variables.add(variable2);

            executionAPI.updateVariablesWS(user, 6L, variables);
            System.out.println("Variables has been updated");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
