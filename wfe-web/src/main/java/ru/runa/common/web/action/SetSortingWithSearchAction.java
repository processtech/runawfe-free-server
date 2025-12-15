package ru.runa.common.web.action;

import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import ru.runa.common.web.Commons;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.HashMap;
import java.util.Map;

/**
 * @struts:action path="/setSortingWithSearch" name="setSortingForm" validate="false"
 */
public class SetSortingWithSearchAction extends SetSortingAction {
    public static final String ACTION_PATH = "/setSortingWithSearch";

    @Override
    public ActionForward execute(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response) {
        ActionForward actionForward = super.execute(mapping, form, request, response);

        Map<String, String> params = new HashMap<>();

        String search = request.getParameter("search");
        if (search != null && !search.trim().isEmpty()) {
            params.put("search", search);
        }

        return Commons.forward(actionForward, params);
    }
}
