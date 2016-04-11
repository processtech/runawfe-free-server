package ru.runa.bp.web;

import java.util.List;

import org.alfresco.service.cmr.repository.NodeRef;

import ru.runa.alfresco.AlfConnection;
import ru.runa.alfresco.AlfObject;
import ru.runa.alfresco.WSConnectionSettings;
import ru.runa.bp.AlfAjaxTag;

/**
 * Handler for downloading cm:content from Alfresco.
 * 
 * @author dofs
 */
public class AlfDownloadContentTag extends AlfAjaxTag {

    private static final long serialVersionUID = 1L;

    @Override
    protected String renderRequest(AlfConnection alfConnection) throws Exception {
        List<String> uuids = getParameterAs(List.class, 0);
        StringBuffer buf = new StringBuffer();
        for (int i = 0; i < uuids.size(); i++) {
            String uuid = uuids.get(0);
            if (i != 0) {
                buf.append("<br>");
            }
            AlfObject alfObject = alfConnection.loadObjectNotNull(uuid);
            String href = WSConnectionSettings.getInstance().getAlfBaseUrl() + "d/a/workspace/SpacesStore/"
                    + new NodeRef(alfObject.getUuidRef()).getId() + "/" + alfObject.getObjectName();
            buf.append("<a href=\"").append(href).append("\">").append(alfObject.getObjectName()).append("</a>");
        }
        return buf.toString();
    }

}
