package ru.runa.wf.web.html;

import ru.runa.common.web.Messages;
import ru.runa.wfe.execution.dto.WfFrozenToken;

public class FrozenTokenNodeTypeTdBuilder extends TokenNodeTypeTdBuilder {

    @Override
    public String getValue(Object object, Env env) {
        WfFrozenToken token = (WfFrozenToken) object;
        if (token.getNodeType() == null) {
            return "";
        }
        if (env.getPageContext() != null) {
            return Messages.getMessage(token.getNodeType().getLabelKey(), env.getPageContext());
        }
        return token.getNodeType().name();
    }
}
