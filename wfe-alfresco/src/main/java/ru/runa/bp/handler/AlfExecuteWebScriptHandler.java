package ru.runa.bp.handler;

import java.util.Map;

import ru.runa.alfresco.AlfConnection;
import ru.runa.alfresco.WebScriptExecutor;
import ru.runa.bp.AlfHandler;
import ru.runa.bp.AlfHandlerData;

import com.google.common.base.Charsets;

/**
 * Handler which executes alfresco web script.
 * 
 * @author dofs
 */
public abstract class AlfExecuteWebScriptHandler extends AlfHandler {

    @Override
    protected void executeAction(AlfConnection alfConnection, AlfHandlerData handlerData) throws Exception {
        WebScriptExecutor webScriptExecutor = new WebScriptExecutor(getWebScriptUri(handlerData), getWebScriptParameters(alfConnection, handlerData));
        webScriptExecutor.setUseHttpPost(useHttpPost());
        webScriptExecutor.setThrowExceptionOnErrorState(throwExceptionOnErrorState());
        byte[] response = webScriptExecutor.doRequest();
        log.debug(new String(response, Charsets.UTF_8.name()));
        handleResponse(handlerData, response);
    }

    protected String getWebScriptUri(AlfHandlerData alfHandlerData) {
        return alfHandlerData.getInputParamValueNotNull(String.class, "webScriptUri");
    }

    protected boolean throwExceptionOnErrorState() {
        return true;
    }

    protected boolean useHttpPost() {
        return false;
    }

    protected void handleResponse(AlfHandlerData alfHandlerData, byte[] response) {

    }

    protected abstract Map<String, Object> getWebScriptParameters(AlfConnection alfConnection, AlfHandlerData alfHandlerData);

}
