package ru.runa.wfe.extension.orgfunction;

import java.util.List;

import ru.runa.wfe.commons.TypeConversionUtil;

import com.google.common.collect.Lists;

/**
 * 
 * Returns actors by code, can accept multiple codes at once
 * 
 * Created on Jul 12, 2006
 * 
 */
public class GetActorsByCodesFunction extends GetActorsOrgFunctionBase {

    @Override
    protected List<Long> getActorCodes(Object... parameters) {
        List<Long> codes = Lists.newArrayListWithExpectedSize(parameters.length);
        for (int i = 0; i < parameters.length; i++) {
            codes.add(TypeConversionUtil.convertTo(Long.class, parameters[i]));
        }
        return codes;
    }
}
