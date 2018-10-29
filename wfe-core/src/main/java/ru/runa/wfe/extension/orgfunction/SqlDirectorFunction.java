package ru.runa.wfe.extension.orgfunction;

import java.util.List;


/**
 * Created on 19.05.2006 10:35:40
 */
public class SqlDirectorFunction extends ActorOrgFunctionBase {

    @Override
    protected List<Long> getActorCodes(Long actorCode) {
        return SqlFunctionDao.getDirectorCode(SqlFunctionResources.getAllDirectorsCodes(), SqlFunctionResources.getChiefCodeBySubordinateCodeSQL(), actorCode);
    }

}
