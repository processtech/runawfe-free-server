package ru.runa.wfe.extension.orgfunction;

import java.util.List;


/**
 * 
 * Created on Jul 12, 2006
 * 
 */
public class SqlChiefRecursiveFunction extends ActorOrgFunctionBase {

    @Override
    protected List<Long> getActorCodes(Long code) {
        return SqlFunctionDao.getActorCodesRecurisve(SqlFunctionResources.getChiefCodeBySubordinateCodeSQL(), new Long[] { code });
    }
}
