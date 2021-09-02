package ru.runa.wfe.extension.handler.user;

import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;

import ru.runa.wfe.extension.handler.CommonParamBasedHandler;
import ru.runa.wfe.extension.handler.HandlerData;
import ru.runa.wfe.relation.Relation;
import ru.runa.wfe.relation.RelationPair;
import ru.runa.wfe.relation.dao.RelationDao;
import ru.runa.wfe.relation.dao.RelationPairDao;
import ru.runa.wfe.user.Executor;
import ru.runa.wfe.user.dao.ExecutorDao;

import com.google.common.collect.Lists;

public class GetExecutorByRelationHandler extends CommonParamBasedHandler {
    @Autowired
    private ExecutorDao executorDao;
    @Autowired
    private RelationDao relationDao;
    @Autowired
    private RelationPairDao relationPairDao;

    @Override
    protected void executeAction(HandlerData handlerData) throws Exception {
        String relationName = handlerData.getInputParamValueNotNull(String.class, "name");
        Executor parameter = handlerData.getInputParamValueNotNull(Executor.class, "parameter");
        boolean inversed = handlerData.getInputParamValueNotNull(boolean.class, "inversed");
        boolean recursively = handlerData.getInputParamValueNotNull(boolean.class, "recursively");
        List<Executor> parameters = Lists.newArrayList(parameter);
        if (recursively) {
            parameters.addAll(executorDao.getExecutorParentsAll(parameter));
        }
        Relation relation = relationDao.getNotNull(relationName);
        List<RelationPair> pairs;
        if (inversed) {
            pairs = relationPairDao.getExecutorsRelationPairsLeft(relation, parameters);
        } else {
            pairs = relationPairDao.getExecutorsRelationPairsRight(relation, parameters);
        }
        if (pairs.size() == 0) {
            String option = handlerData.getInputParamValueNotNull(String.class, "missedCaseOption");
            if ("THROW_ERROR".equals(option)) {
                throw new Exception("Relation " + (inversed ? "!" : "") + "'" + relationName + "' does not defined for " + parameter.getLabel());
            }
            handlerData.setOutputParam("result", null);
            return;
        }
        if (pairs.size() > 1) {
            log.warn(pairs);
            String option = handlerData.getInputParamValueNotNull(String.class, "multipleCaseOption");
            if ("THROW_ERROR".equals(option)) {
                throw new Exception("Relation " + (inversed ? "!" : "") + "'" + relationName + "' has multiple choice for " + parameter.getLabel());
            }
        }
        Executor result;
        if (inversed) {
            result = pairs.get(0).getRight();
        } else {
            result = pairs.get(0).getLeft();
        }
        handlerData.setOutputParam("result", result);
    }

}
