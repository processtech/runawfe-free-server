package ru.runa.wfe.extension.handler;

import ru.runa.wfe.extension.ActionHandlerBase;

public abstract class ParamBasedHandlerActionHandler extends ActionHandlerBase {
    protected ParamsDef paramsDef;

    @Override
    public void setConfiguration(String configuration) {
        super.setConfiguration(configuration);
        paramsDef = ParamsDef.parse(configuration);
    }

}
