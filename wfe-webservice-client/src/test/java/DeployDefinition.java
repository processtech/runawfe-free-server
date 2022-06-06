import com.google.common.collect.Lists;
import com.google.common.io.Files;
import java.io.File;
import ru.runa.wfe.webservice.AuthenticationAPI;
import ru.runa.wfe.webservice.AuthenticationWebService;
import ru.runa.wfe.webservice.DefinitionAPI;
import ru.runa.wfe.webservice.DefinitionWebService;
import ru.runa.wfe.webservice.User;

public class DeployDefinition {

    public static void main(String[] args) {
        try {
            AuthenticationAPI authenticationAPI = new AuthenticationWebService().getAuthenticationAPIPort();
            User user = authenticationAPI.authenticateByLoginPassword("Administrator", "wf");
            DefinitionAPI definitionAPI = new DefinitionWebService().getDefinitionAPIPort();
            byte[] file = Files.toByteArray(new File("C:/Users/Dofs/Desktop/PARs/calendarTest.par"));
            definitionAPI.deployProcessDefinition(user, file, Lists.newArrayList("type"));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
