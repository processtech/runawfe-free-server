package ru.runa.wfe.extension.orgfunction;

import java.util.List;


/**
 * 
 * Created on Jul 13, 2006
 * 
 */
public class SqlSubordinateRecursiveFunction extends ActorOrgFunctionBase {

    @Override
    protected List<Long> getActorCodes(Long code) {
        return SqlFunctionDao.getActorCodesRecurisve(SqlFunctionResources.getSubordinateCodesByChiefCodeSQL(), new Long[] { code });
    }

}
