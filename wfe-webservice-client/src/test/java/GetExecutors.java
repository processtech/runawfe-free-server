import java.util.List;

import ru.runa.wfe.webservice.AuthenticationAPI;
import ru.runa.wfe.webservice.AuthenticationWebService;
import ru.runa.wfe.webservice.BatchPresentation;
import ru.runa.wfe.webservice.ClassPresentationType;
import ru.runa.wfe.webservice.ExecutorAPI;
import ru.runa.wfe.webservice.ExecutorWebService;
import ru.runa.wfe.webservice.User;

public class GetExecutors {

    public static void main(String[] args) {
        try {
            AuthenticationAPI authenticationAPI = new AuthenticationWebService().getAuthenticationAPIPort();
            User user = authenticationAPI.authenticateByLoginPassword("Administrator", "wf");
            ExecutorAPI executorAPI = new ExecutorWebService().getExecutorAPIPort();
            BatchPresentation actorsBatchPresentation = new BatchPresentation();
            actorsBatchPresentation.setPageNumber(1);
            actorsBatchPresentation.setRangeSize(10);
            actorsBatchPresentation.setType(ClassPresentationType.ACTOR);
            List actors = executorAPI.getExecutors(user, actorsBatchPresentation);
            System.out.println("actors=" + actors.size());
            List executors = executorAPI.getExecutors(user, null);
            System.out.println("executors=" + executors.size());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
