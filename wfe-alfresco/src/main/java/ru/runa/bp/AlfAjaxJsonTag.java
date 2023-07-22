package ru.runa.bp;

import javax.servlet.http.HttpServletRequest;

import org.json.simple.JSONAware;

import ru.runa.Messages;
import ru.runa.alfresco.AlfConnection;
import ru.runa.alfresco.ConnectionException;
import ru.runa.alfresco.RemoteAlfConnector;
import ru.runa.wfe.commons.ftl.AjaxJsonFreemarkerTag;

import com.google.common.base.Strings;

/**
 * Base class for RunaWFE ajax freemarker tag to work with JSON format.
 * 
 * @author dofs
 */
public abstract class AlfAjaxJsonTag extends AjaxJsonFreemarkerTag {
    private static final long serialVersionUID = 1L;

    protected abstract String renderRequest(AlfConnection alfConnection) throws Exception;

    protected abstract JSONAware processAjaxRequest(AlfConnection alfConnection, HttpServletRequest request) throws Exception;

    @Override
    protected String renderRequest() throws Exception {
        try {
            return new RemoteAlfConnector<String>() {
                @Override
                protected String code() throws Exception {
                    return renderRequest(alfConnection);
                }
            }.runInSession();
        } catch (Exception e) {
            if (e instanceof ConnectionException) {
                throw new Exception(Messages.getMessage("error.alfresco.unavailable"), e);
            } else {
                throw new Exception(Messages.getMessage("error.alfresco") + ": " + e.getMessage(), e);
            }
        }
    }

    @Override
    public JSONAware processAjaxRequest(final HttpServletRequest request) throws Exception {
        return new RemoteAlfConnector<JSONAware>() {
            @Override
            protected JSONAware code() throws Exception {
                return processAjaxRequest(alfConnection, request);
            }
        }.runInSession();
    }

    protected boolean isValid(String varValue) {
        return !Strings.isNullOrEmpty(varValue);
    }

}
