package ru.runa.wfe.commons.ftl;

import com.google.common.base.Throwables;
import freemarker.template.ObjectWrapper;
import freemarker.template.SimpleHash;
import freemarker.template.TemplateModel;
import freemarker.template.TemplateModelException;
import java.util.ArrayList;
import java.util.List;
import javax.servlet.http.HttpSession;
import lombok.extern.apachecommons.CommonsLog;
import ru.runa.wfe.commons.web.WebHelper;
import ru.runa.wfe.user.User;
import ru.runa.wfe.var.VariableProvider;

@CommonsLog
public class FormHashModel extends SimpleHash {
    private static final long serialVersionUID = 1L;
    private final User user;
    private final VariableProvider variableProvider;
    private final WebHelper webHelper;

    public FormHashModel(User user, VariableProvider variableProvider, WebHelper webHelper) {
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

    public VariableProvider getVariableProvider() {
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
                        tags = new ArrayList<>();
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
        log.warn("Null for " + key);
        return null;
    }
}
