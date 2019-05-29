package ru.runa.common.web.action;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.struts.action.Action;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;

import ru.runa.common.web.BatchPresentationsVisibility;
import ru.runa.common.web.form.IdNameForm;

/**
 * Created on 26.01.2005
 * 
 * @struts:action path="/hideableBlock" name="idNameForm" validate="false"
 */
public class HideableBlockAction extends Action {

    public static final String ACTION_PATH = "/hideableBlock";

    @Override
    public ActionForward execute(ActionMapping mapping, ActionForm actionForm, HttpServletRequest request, HttpServletResponse response) {
        IdNameForm form = (IdNameForm) actionForm;
        BatchPresentationsVisibility.get(request.getSession()).changeBlockVisibility(form.getName());
        return null;
    }
}
