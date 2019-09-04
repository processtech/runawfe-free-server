package ru.runa.wfe.extension.handler;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import ru.runa.wfe.InternalApplicationException;
import ru.runa.wfe.user.Executor;
import ru.runa.wfe.user.Group;
import ru.runa.wfe.user.dao.ExecutorDao;

public class ExecutorsListFromGroupHandler extends CommonParamBasedHandler {
    @Autowired
    protected ExecutorDao executorDao;

    @Override
    protected void executeAction(HandlerData handlerData) throws Exception {
        Group group;
        Object groupOrGroupName = handlerData.getInputParamValueNotNull("group");
        if (groupOrGroupName instanceof Group) {
            group = (Group) groupOrGroupName;
        } else if (groupOrGroupName instanceof String) {
            group = executorDao.getGroup((String) groupOrGroupName);
        } else {
            throw new InternalApplicationException("Parameter 'group' has wrong type: must be String (group name) or Group");
        }
        List<Executor> executors = executorDao.getAllNonGroupExecutorsFromGroup(group);
        handlerData.setOutputParam("executors", executors);
    }
}
