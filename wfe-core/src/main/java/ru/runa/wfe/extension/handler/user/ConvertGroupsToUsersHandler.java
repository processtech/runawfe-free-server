package ru.runa.wfe.extension.handler.user;

import java.util.List;
import java.util.Set;

import org.springframework.beans.factory.annotation.Autowired;

import ru.runa.wfe.extension.handler.CommonParamBasedHandler;
import ru.runa.wfe.extension.handler.HandlerData;
import ru.runa.wfe.user.Actor;
import ru.runa.wfe.user.Executor;
import ru.runa.wfe.user.Group;
import ru.runa.wfe.user.dao.ExecutorDAO;

import com.google.common.collect.Lists;

public class ConvertGroupsToUsersHandler extends CommonParamBasedHandler {
    @Autowired
    private ExecutorDAO executorDAO;

    @Override
    protected void executeAction(HandlerData handlerData) throws Exception {
        List<Executor> executors = handlerData.getInputParamValueNotNull(List.class, "input");
        List<Actor> result = Lists.newArrayList();
        for (Executor executor : executors) {
            toActors(result, executor);
        }
        handlerData.setOutputParam("result", result);
    }

    private void toActors(List<Actor> result, Executor executor) {
        if (executor instanceof Actor) {
            result.add((Actor) executor);
        } else if (executor instanceof Group) {
            Set<Executor> children = executorDAO.getGroupChildren((Group) executor);
            for (Executor child : children) {
                toActors(result, child);
            }
        }
    }
}
