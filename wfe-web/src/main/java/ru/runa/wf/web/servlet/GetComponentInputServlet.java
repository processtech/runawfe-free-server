package ru.runa.wf.web.servlet;

import com.google.common.base.Charsets;
import java.io.IOException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.json.simple.JSONObject;
import ru.runa.common.web.AjaxWebHelper;
import ru.runa.common.web.Commons;
import ru.runa.wf.web.ftl.component.ViewUtil;
import ru.runa.wfe.InternalApplicationException;
import ru.runa.wfe.commons.web.WebHelper;
import ru.runa.wfe.service.delegate.Delegates;
import ru.runa.wfe.user.User;
import ru.runa.wfe.var.VariableDefinition;
import ru.runa.wfe.var.dto.WfVariable;

public class GetComponentInputServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        processRequest(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        processRequest(request, response);
    }

    private void processRequest(HttpServletRequest request, HttpServletResponse response) throws IOException {
        String variableName = request.getParameter("variableName");
        Long processId = Long.valueOf(request.getParameter("id"));
        if (processId == null) {
            throw new InternalApplicationException("id not found");
        }

        JSONObject variableObject = new JSONObject();
        User user = Commons.getUser(request.getSession());
        WfVariable variable = Delegates.getExecutionService().getVariable(user, processId, variableName);
        String scriptingName = "";
        String variableIsNullString = "";
        String variableValue = "";
        WebHelper webHelper = new AjaxWebHelper(request);

        if (variable != null) {
            Object value = variable.getValue();
            Boolean variableIsNull = true;
            if (value != null) {
                variableIsNull = false;
                variableValue = ViewUtil.getComponentOutput(user, webHelper, processId, variable);
            }
            scriptingName = variable.getDefinition().getScriptingName();
            variableIsNullString = variableIsNull.toString();
        } else {
            Long definitionId = Delegates.getExecutionService().getProcess(user, processId).getDefinitionId();
            VariableDefinition variableDefinition = Delegates.getDefinitionService().getVariableDefinition(user, definitionId, variableName);
            if (variableDefinition != null) {
                scriptingName = variableDefinition.getScriptingName();
                variableIsNullString = "true";
                variableValue = "null";
                variable = new WfVariable(variableDefinition, null);
            }
        }
        variableObject.put("scriptingName", scriptingName);
        variableObject.put("variableIsNull", variableIsNullString);
        variableObject.put("variableValue", variableValue);
        try {
            String componentInput = ViewUtil.getComponentInput(user, webHelper, variable);
            variableObject.put("input", componentInput);
        } catch (Exception e) {
            variableObject.put("input", e.getLocalizedMessage());
        } finally {
            response.setContentType("text/html");
            response.setCharacterEncoding(Charsets.UTF_8.name());
            response.getOutputStream().write(variableObject.toString().getBytes(Charsets.UTF_8));
            response.getOutputStream().flush();
        }
    }
}
