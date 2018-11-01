package ru.runa.wfe.extension.handler;

import com.google.common.collect.Lists;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import ru.runa.wfe.user.Executor;
import ru.runa.wfe.user.Group;
import ru.runa.wfe.user.dao.ExecutorDao;

public class ExecutorsListFromGroupHandler extends CommonParamBasedHandler {
    @Autowired
    protected ExecutorDao executorDao;
    
    @Override
    protected void executeAction(HandlerData handlerData) throws Exception {
        String groupName = handlerData.getInputParamValueNotNull(String.class, "group");
        List<Executor> executors;
        try {
            executors = executorDao.getAllNonGroupExecutorsFromGroup(executorDao.getGroup(groupName));
        } catch(Exception e) {
            executors = Lists.newArrayList();
        }
        handlerData.setOutputParam("executors", executors);
    }

}
