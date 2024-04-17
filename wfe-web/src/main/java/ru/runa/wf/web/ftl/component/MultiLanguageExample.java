package ru.runa.wf.web.ftl.component;

import ru.runa.wfe.commons.ftl.FormComponent;

@Deprecated
public class MultiLanguageExample extends FormComponent {

    private static final long serialVersionUID = 1L;

    @Override
    protected Object renderRequest() {
        String key = getParameterAsString(0);
        return webHelper.getMessage(key);
    }

}
