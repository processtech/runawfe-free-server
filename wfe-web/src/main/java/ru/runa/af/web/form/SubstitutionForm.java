package ru.runa.af.web.form;

import javax.servlet.http.HttpServletRequest;

import org.apache.struts.action.ActionErrors;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.action.ActionMessage;
import org.apache.struts.action.ActionMessages;

import ru.runa.af.web.orgfunction.FunctionDef;
import ru.runa.af.web.orgfunction.SubstitutionDefinitions;
import ru.runa.common.web.Commons;
import ru.runa.common.web.form.IdForm;
import ru.runa.wfe.extension.orgfunction.ParamRenderer;
import ru.runa.wfe.user.User;

/**
 * @struts:form name = "substitutionForm"
 */
public class SubstitutionForm extends IdForm {
    private static final long serialVersionUID = 1L;
    private static final String ERROR_KEY = "substitution.params.invalid";

    public static final String CRITERIA_ID_INPUT_NAME = "criteriaId";
    public static final String ENABLED_INPUT_NAME = "enabled";
    public static final String TERMINATOR_INPUT_NAME = "terminator";
    public static final String ACTOR_ID_INPUT_NAME = "actorId";
    public static final String FUNCTION_INPUT_NAME = "function";
    public static final String PARAMS_INPUT_NAME = "params";

    private String function = "";
    private String[] params;
    private Long criteriaId;
    private boolean enabled;
    private boolean terminator;
    private Long actorId;

    @Override
    public ActionErrors validate(ActionMapping mapping, HttpServletRequest request) {
        ActionErrors errors = new ActionErrors();
        if (!terminator) {
            try {
                if (params == null) {
                    params = new String[0];
                }
                User user = Commons.getUser(request.getSession());
                FunctionDef functionDef = SubstitutionDefinitions.getByClassNameNotNull(function);
                if (functionDef.getParams().size() != params.length) {
                    errors.add(ActionMessages.GLOBAL_MESSAGE, new ActionMessage(ERROR_KEY));
                } else {
                    for (int i = 0; i < functionDef.getParams().size(); i++) {
                        ParamRenderer renderer = functionDef.getParams().get(i).getRenderer();
                        if (!renderer.isValueValid(user, params[i])) {
                            errors.add(ActionMessages.GLOBAL_MESSAGE, new ActionMessage(ERROR_KEY));
                        }
                    }
                }
            } catch (Exception e) {
                errors.add(ActionMessages.GLOBAL_MESSAGE, new ActionMessage(e.getClass().getName()));
            }
        }
        return errors;
    }

    public Long getActorId() {
        return actorId;
    }

    public void setActorId(Long actorId) {
        this.actorId = actorId;
    }

    public Long getCriteriaId() {
        return criteriaId;
    }

    public void setCriteriaId(Long criteriaId) {
        this.criteriaId = criteriaId;
    }

    public boolean isTerminator() {
        return terminator;
    }

    public void setTerminator(boolean terminator) {
        this.terminator = terminator;
    }

    public boolean isEnabled() {
        return enabled;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public String getFunction() {
        return function;
    }

    public void setFunction(String function) {
        this.function = function;
    }

    public String[] getParams() {
        return params;
    }

    public void setParams(String[] params) {
        this.params = params;
    }

    public String buildOrgFunction() {
        StringBuffer b = new StringBuffer(function);
        b.append("(");
        for (int i = 0; i < params.length; i++) {
            if (i != 0) {
                b.append(",");
            }
            b.append(params[i]);
        }
        b.append(")");
        return b.toString();
    }
}
