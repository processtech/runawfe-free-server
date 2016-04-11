package ru.runa.wfe.extension.handler.user;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;

import ru.runa.wfe.extension.handler.CommonParamBasedHandler;
import ru.runa.wfe.extension.handler.HandlerData;
import ru.runa.wfe.relation.Relation;
import ru.runa.wfe.relation.RelationPair;
import ru.runa.wfe.relation.dao.RelationDAO;
import ru.runa.wfe.relation.dao.RelationPairDAO;
import ru.runa.wfe.user.Executor;
import ru.runa.wfe.user.dao.ExecutorDAO;

import com.google.common.collect.Lists;

public class GetExecutorsByRelationHandler extends CommonParamBasedHandler {
    @Autowired
    private ExecutorDAO executorDAO;
    @Autowired
    private RelationDAO relationDAO;
    @Autowired
    private RelationPairDAO relationPairDAO;

    @Override
    protected void executeAction(HandlerData handlerData) throws Exception {
        String relationName = handlerData.getInputParamValueNotNull(String.class, "name");
        Executor parameter = handlerData.getInputParamValueNotNull(Executor.class, "parameter");
        boolean inversed = handlerData.getInputParamValueNotNull(boolean.class, "inversed");
        boolean recursively = handlerData.getInputParamValueNotNull(boolean.class, "recursively");
        List<Executor> parameters = Lists.newArrayList(parameter);
        if (recursively) {
            parameters.addAll(executorDAO.getExecutorParentsAll(parameter, false));
        }
        Relation relation = relationDAO.getNotNull(relationName);
        List<RelationPair> pairs;
        if (inversed) {
            pairs = relationPairDAO.getExecutorsRelationPairsLeft(relation, parameters);
        } else {
            pairs = relationPairDAO.getExecutorsRelationPairsRight(relation, parameters);
        }
        List<Executor> result = Lists.newArrayList();
        for (RelationPair pair : pairs) {
            if (inversed) {
                result.add(pair.getRight());
            } else {
                result.add(pair.getLeft());
            }
        }
        handlerData.setOutputParam("result", result);
    }

}
