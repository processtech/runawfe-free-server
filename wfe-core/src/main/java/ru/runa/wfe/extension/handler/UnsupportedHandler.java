package ru.runa.wfe.extension.handler;

import java.util.Map;

import ru.runa.wfe.var.IVariableProvider;

/**
 * Unsupported handler. Can be used as a stub for throwing exception.
 *
 * @author Dofs
 */
public class UnsupportedHandler extends CommonHandler {

    @Override
    protected Map<String, Object> executeAction(IVariableProvider variableProvider) throws Exception {
        throw new UnsupportedOperationException();
    }

}
