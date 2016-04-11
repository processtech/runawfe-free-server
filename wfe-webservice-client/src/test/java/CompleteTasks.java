import java.util.List;

import ru.runa.wfe.webservice.AuthenticationAPI;
import ru.runa.wfe.webservice.AuthenticationWebService;
import ru.runa.wfe.webservice.TaskAPI;
import ru.runa.wfe.webservice.TaskWebService;
import ru.runa.wfe.webservice.User;
import ru.runa.wfe.webservice.WfTask;

public class CompleteTasks {

    public static void main(String[] args) {
        try {
            AuthenticationAPI authenticationAPI = new AuthenticationWebService().getAuthenticationAPIPort();
            User user = authenticationAPI.authenticateByLoginPassword("Administrator", "wf");
            TaskAPI taskAPI = new TaskWebService().getTaskAPIPort();

            List<WfTask> tasks = taskAPI.getMyTasks(user, null);
            System.out.println("TASKS = " + tasks.size());
            for (WfTask task : tasks) {
                System.out.println(" Completing " + task.getName());
                taskAPI.completeTaskWS(user, task.getId(), null, null);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
