import ru.runa.wfe.webservice.AuthenticationAPI;
import ru.runa.wfe.webservice.AuthenticationWebService;
import ru.runa.wfe.webservice.User;

public class Authenticate {

    public static void main(String[] args) {
        try {
            AuthenticationWebService authenticationWebService = new AuthenticationWebService();
            AuthenticationAPI authenticationAPI = authenticationWebService.getAuthenticationAPIPort();
            User user = authenticationAPI.authenticateByLoginPassword("Administrator", "wf");
            System.out.println(user.getActor().getFullName());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
