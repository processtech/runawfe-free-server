package ru.runa.bp.demo;

import ru.runa.alfresco.AlfConnection;
import ru.runa.bp.AlfAjaxTag;

public class ShowDocStatusTag extends AlfAjaxTag {

    private static final long serialVersionUID = 1L;

    @Override
    protected String renderRequest(AlfConnection alfConnection) throws Exception {
        String uuid = variableProvider.getValueNotNull(String.class, "uuid");
        MyDoc myDoc = alfConnection.loadObjectNotNull(uuid);
        return "<b>" + myDoc.getStatus() + "</b>";
    }

}
