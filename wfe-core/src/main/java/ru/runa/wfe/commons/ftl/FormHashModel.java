package ru.runa.wfe.commons.ftl;

import java.util.ArrayList;
import java.util.List;

import javax.servlet.http.HttpSession;

import org.apache.commons.logging.LogFactory;

import ru.runa.wfe.commons.web.WebHelper;
import ru.runa.wfe.user.User;
import ru.runa.wfe.var.IVariableProvider;

import com.google.common.base.Throwables;

import freemarker.template.ObjectWrapper;
import freemarker.template.SimpleHash;
import freemarker.template.TemplateModel;
import freemarker.template.TemplateModelException;

public class FormHashModel extends SimpleHash {
    private static final long serialVersionUID = 1L;
    private final User user;
    private final IVariableProvider variableProvider;
    private final WebHelper webHelper;

    public FormHashModel(User user, IVariableProvider variableProvider, WebHelper webHelper) {
        super(ObjectWrapper.BEANS_WRAPPER);
        this.user = user;
        this.variableProvider = variableProvider;
        this.webHelper = webHelper;
    }

    public void clearSession() {
        HttpSession session = webHelper.getRequest().getSession();
        java.util.Enumeration<String> attributeNames = session.getAttributeNames();
        while (attributeNames.hasMoreElements()) {
            String attributeName = attributeNames.nextElement();
            if (attributeName.startsWith(AjaxFormComponent.COMPONENT_SESSION_PREFIX)) {
                session.removeAttribute(attributeName);
            }
        }
    }

    public WebHelper getWebHelper() {
        return webHelper;
    }

    public IVariableProvider getVariableProvider() {
        return variableProvider;
    }

    @Override
    public TemplateModel get(String key) throws TemplateModelException {
        TemplateModel model = super.get(key);
        if (model != null) {
            return model;
        }
        try {
            FormComponent component = FreemarkerConfiguration.getComponent(key);
            if (component != null) {
                component.init(user, webHelper, variableProvider, key.startsWith(FormComponent.TARGET_PROCESS_PREFIX));
                if (webHelper != null && webHelper.getRequest() != null && component instanceof AjaxFormComponent) {
                    HttpSession session = webHelper.getRequest().getSession();
                    String sessionKey = AjaxFormComponent.COMPONENT_SESSION_PREFIX + key;
                    List<AjaxFormComponent> tags = (List<AjaxFormComponent>) session.getAttribute(sessionKey);
                    if (tags == null) {
                        tags = new ArrayList<AjaxFormComponent>();
                        session.setAttribute(sessionKey, tags);
                    }
                    tags.add((AjaxFormComponent) component);
                }
                return component;
            }
        } catch (Exception e) {
            Throwables.propagateIfInstanceOf(e, RuntimeException.class);
            throw new TemplateModelException(e);
        }
        Object variableValue = variableProvider.getValue(key);
        if (variableValue != null) {
            put(key, wrap(variableValue));
            return super.get(key);
        }
        LogFactory.getLog(getClass()).warn("Null for " + key);
        return null;
    }
}
