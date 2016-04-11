import ru.runa.wfe.webservice.AuthenticationAPI;
import ru.runa.wfe.webservice.AuthenticationWebService;
import ru.runa.wfe.webservice.AuthorizationAPI;
import ru.runa.wfe.webservice.AuthorizationWebService;
import ru.runa.wfe.webservice.ExecutorAPI;
import ru.runa.wfe.webservice.ExecutorWebService;
import ru.runa.wfe.webservice.Permission;
import ru.runa.wfe.webservice.SecuredObjectType;
import ru.runa.wfe.webservice.User;
import ru.runa.wfe.webservice.WfExecutor;

public class CheckPermissionOnExecutor {

    public static void main(String[] args) {
        try {
            AuthenticationAPI authenticationAPI = new AuthenticationWebService().getAuthenticationAPIPort();
            User user = authenticationAPI.authenticateByLoginPassword("Administrator", "wf");
            ExecutorAPI executorAPI = new ExecutorWebService().getExecutorAPIPort();
            WfExecutor executor = executorAPI.getExecutorByName(user, "Administrators");
            AuthorizationAPI authorizationAPI = new AuthorizationWebService().getAuthorizationAPIPort();
            Permission permission = new Permission();
            permission.setName("permission.read");
            permission.setMask(1);
            System.out.println("check=" + authorizationAPI.isAllowedWS(user, permission, SecuredObjectType.GROUP, executor.getId()));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
