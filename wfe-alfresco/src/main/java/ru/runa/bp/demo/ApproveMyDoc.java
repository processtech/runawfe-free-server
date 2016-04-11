package ru.runa.bp.demo;

import ru.runa.alfresco.AlfConnection;
import ru.runa.bp.AlfHandler;
import ru.runa.bp.AlfHandlerData;

public class ApproveMyDoc extends AlfHandler {

    @Override
    protected void executeAction(AlfConnection alfConnection, AlfHandlerData alfHandlerData) throws Exception {
        MyDoc myDoc = alfConnection.loadObjectNotNull(alfHandlerData.getInputParamValueNotNull(String.class, "uuid"));
        myDoc.setStatus(MyDoc.STATUS_APPROVED);
        alfConnection.updateObject(myDoc, false, "business process update");
    }
}
