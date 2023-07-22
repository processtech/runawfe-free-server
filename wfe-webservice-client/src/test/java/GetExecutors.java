import java.util.List;
import ru.runa.wfe.webservice.AuthenticationAPI;
import ru.runa.wfe.webservice.AuthenticationWebService;
import ru.runa.wfe.webservice.ClassPresentationType;
import ru.runa.wfe.webservice.ExecutorAPI;
import ru.runa.wfe.webservice.ExecutorWebService;
import ru.runa.wfe.webservice.Filter;
import ru.runa.wfe.webservice.User;
import ru.runa.wfe.webservice.WfBatchPresentation;

public class GetExecutors {

    public static void main(String[] args) {
        try {
            AuthenticationAPI authenticationAPI = new AuthenticationWebService().getAuthenticationAPIPort();
            User user = authenticationAPI.authenticateByLoginPassword("Administrator", "wf");
            ExecutorAPI executorAPI = new ExecutorWebService().getExecutorAPIPort();
            WfBatchPresentation batchPresentation = new WfBatchPresentation();
            batchPresentation.setClassPresentationType(ClassPresentationType.ACTOR);
            batchPresentation.setPageNumber(1);
            batchPresentation.setPageSize(10);
            Filter filter = new Filter();
            filter.setName("name");
            filter.setValue("Admin*");
            batchPresentation.getFilters().add(filter);
            List actors = executorAPI.getExecutors(user, batchPresentation);
            System.out.println("actors=" + actors.size());
            batchPresentation.setClassPresentationType(ClassPresentationType.EXECUTOR);
            List executors = executorAPI.getExecutors(user, batchPresentation);
            System.out.println("executors=" + executors.size());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
