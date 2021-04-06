package ru.runa.wfe.extension.orgfunction;

import java.util.List;


/**
 * 
 * Created on Jul 13, 2006
 * 
 */
public class SqlSubordinateFunction extends ActorOrgFunctionBase {

    @Override
    protected List<Long> getActorCodes(Long code) {
        return SqlFunctionDao.getActorCodes(SqlFunctionResources.getSubordinateCodesByChiefCodeSQL(), new Long[] { code });
    }
}
