package ru.runa.wfe.extension.orgfunction;

import java.util.List;

import ru.runa.wfe.commons.ArraysCommons;
import ru.runa.wfe.commons.TypeConversionUtil;

/**
 * Uses first argument as SQL and the rest arguments as actor codes
 * 
 * Created on Jul 12, 2006
 * 
 */
public class SqlFunction extends GetActorsOrgFunctionBase {

    @Override
    protected List<Long> getActorCodes(Object... parameters) {
        String sql = TypeConversionUtil.convertTo(String.class, parameters[0]);
        return SqlFunctionDao.getActorCodes(sql, ArraysCommons.remove(parameters, 0));
    }
}
