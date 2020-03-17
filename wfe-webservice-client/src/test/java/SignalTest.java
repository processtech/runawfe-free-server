import java.util.ArrayList;
import java.util.List;
import ru.runa.wfe.webservice.AuthenticationAPI;
import ru.runa.wfe.webservice.AuthenticationWebService;
import ru.runa.wfe.webservice.ExecutionAPI;
import ru.runa.wfe.webservice.ExecutionWebService;
import ru.runa.wfe.webservice.StringKeyValue;
import ru.runa.wfe.webservice.User;

public class SignalTest {

    public static void main(String[] args) {
        try {
            AuthenticationAPI authenticationAPI = new AuthenticationWebService().getAuthenticationAPIPort();
            User user = authenticationAPI.authenticateByLoginPassword("Administrator", "wf");
            ExecutionAPI executionAPI = new ExecutionWebService().getExecutionAPIPort();
            {
                List<StringKeyValue> routingData = new ArrayList<>();
                routingData.add(create("processDefinitionName", "sample1655-unknown"));
                System.out.println(executionAPI.signalReceiverIsActiveWS(user, routingData));
            }
            {
                List<StringKeyValue> routingData = new ArrayList<>();
                routingData.add(create("processDefinitionName", "sample1655"));
                System.out.println(executionAPI.signalReceiverIsActiveWS(user, routingData));
            }
            {
                List<StringKeyValue> routingData = new ArrayList<>();
                routingData.add(create("processDefinitionName", "sample1655"));
                List<StringKeyValue> payloadData = new ArrayList<>();
                payloadData.add(create("stringValue", "sample"));
                payloadData.add(create("datetimeValue", "17.02.2020 15:17:44"));
                executionAPI.sendSignalWS(user, routingData, payloadData, 1);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private static StringKeyValue create(String key, String value) {
        StringKeyValue kv = new StringKeyValue();
        kv.setKey(key);
        kv.setValue(value);
        return kv;
    }
}
