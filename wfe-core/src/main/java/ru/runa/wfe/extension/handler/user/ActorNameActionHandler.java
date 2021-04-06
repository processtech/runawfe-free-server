package ru.runa.wfe.extension.handler.user;

import org.springframework.beans.factory.annotation.Autowired;

import ru.runa.wfe.InternalApplicationException;
import ru.runa.wfe.extension.handler.CommonParamBasedHandler;
import ru.runa.wfe.extension.handler.HandlerData;
import ru.runa.wfe.user.Actor;
import ru.runa.wfe.user.dao.ExecutorDao;

/**
 * 
 * @author Dofs
 * @since 3.3
 * @deprecated Use {@link GetExecutorInfoHandler}.
 */
@Deprecated
public class ActorNameActionHandler extends CommonParamBasedHandler {
    @Autowired
    private ExecutorDao executorDao;

    @Override
    protected void executeAction(HandlerData handlerData) throws Exception {
        Long actorCode = handlerData.getInputParamValue(Long.class, "actorCode");
        String actorLogin = handlerData.getInputParamValue(String.class, "actorLogin");
        String format = handlerData.getInputParamValueNotNull("format");
        Actor actor;
        if (actorCode != null) {
            actor = executorDao.getActorByCode(actorCode);
        } else if (actorLogin != null) {
            actor = executorDao.getActor(actorLogin);
        } else {
            throw new InternalApplicationException("Neither actor code and login are not defined in configuration.");
        }
        String result;
        if ("name".equals(format)) {
            result = actor.getName();
        } else if ("code".equals(format)) {
            result = String.valueOf(actor.getCode());
        } else if ("email".equals(format)) {
            result = actor.getEmail();
        } else if ("description".equals(format)) {
            result = actor.getDescription();
        } else if ("phone".equals(format)) {
            result = actor.getPhone();
        } else {
            result = actor.getFullName();
        }
        handlerData.setOutputParam("result", result);
    }

}
