package ru.runa.common.web.action;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.actions.ForwardAction;

import ru.runa.common.web.TabHttpSessionHelper;

public class TabHeaderForwardAction extends ForwardAction {

    private final static String TAB_FORWARD_NAME_PARAMETER_NAME = "tabForwardName";

    @Override
    public ActionForward execute(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response) throws Exception {
        String tabForwardName = request.getParameter(TAB_FORWARD_NAME_PARAMETER_NAME);
        if (tabForwardName != null) {
            TabHttpSessionHelper.setTabForwardName(tabForwardName, request.getSession());
        }
        return super.execute(mapping, form, request, response);
    }
}
