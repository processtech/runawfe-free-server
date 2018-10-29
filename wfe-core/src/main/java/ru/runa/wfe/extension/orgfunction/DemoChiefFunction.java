package ru.runa.wfe.extension.orgfunction;

import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;

import ru.runa.wfe.extension.OrgFunctionException;
import ru.runa.wfe.user.Actor;
import ru.runa.wfe.user.Group;

import com.google.common.collect.Lists;

/**
 * 
 * Created on 19.05.2005
 */
public class DemoChiefFunction extends ActorOrgFunctionBase {

    @Override
    protected List<Long> getActorCodes(Long actorCode) {
        Actor actor = executorDao.getActorByCode(actorCode);
        Set<String> patterns = DemoChiefResources.getPatterns();
        String chiefName = null;
        for (String pattern : patterns) {
            if (Pattern.matches(pattern, actor.getName())) {
                chiefName = DemoChiefResources.getChiefName(pattern);
                break;
            }
            if (executorDao.isExecutorExist(pattern)) {
                Group group = executorDao.getGroup(pattern);
                if (executorDao.isExecutorInGroup(actor, group)) {
                    chiefName = DemoChiefResources.getChiefName(pattern);
                    break;
                }
            }
        }
        if (chiefName == null) {
            throw new OrgFunctionException("Wrong parameter: '" + actorCode + "' (Chief cannot be determined)");
        }
        return Lists.newArrayList(executorDao.getActor(chiefName).getCode());
    }

}
