package ru.runa.wfe.extension.orgfunction;

import java.util.List;


import com.google.common.collect.Lists;

/**
 * 
 * Created on Jul 12, 2006
 * 
 */
public class SqlChiefFunction extends ActorOrgFunctionBase {

    @Override
    protected List<Long> getActorCodes(Long code) {
        List<Long> allChiefsCodes = SqlFunctionDao.getActorCodes(SqlFunctionResources.getChiefCodeBySubordinateCodeSQL(), new Long[] { code });
        if (allChiefsCodes != null && allChiefsCodes.size() > 1) {
            return Lists.newArrayList(allChiefsCodes.get(0));
        } else {
            return allChiefsCodes;
        }
    }
}
