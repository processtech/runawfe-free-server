package ru.runa.wf.web.servlet;

import com.google.common.base.Charsets;
import com.google.common.base.Objects;
import com.google.common.base.Throwables;
import com.google.common.collect.Maps;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.nio.charset.StandardCharsets;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;
import javax.servlet.annotation.MultipartConfig;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import org.json.simple.JSONObject;
import ru.runa.common.web.RequestWebHelper;
import ru.runa.wf.web.FormSubmissionUtils;
import ru.runa.wfe.commons.ftl.FormComponentExtractionModel;
import ru.runa.wfe.commons.ftl.FormComponentSubmissionHandler;
import ru.runa.wfe.commons.ftl.FormComponentSubmissionPostProcessor;
import ru.runa.wfe.commons.ftl.FreemarkerProcessor;
import ru.runa.wfe.execution.dto.WfProcess;
import ru.runa.wfe.form.Interaction;
import ru.runa.wfe.service.client.DelegateProcessVariableProvider;
import ru.runa.wfe.service.delegate.Delegates;
import ru.runa.wfe.task.dto.WfTask;
import ru.runa.wfe.user.User;
import ru.runa.wfe.var.VariableDefinition;
import ru.runa.wfe.var.VariableProvider;

@WebServlet(name = "OldFormSubmitServlet", urlPatterns = {"/submitOldTaskForm"})
@MultipartConfig
public class OldFormSubmitServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;
    private static final String USER_DEFINED_VARIABLES = "UserInputVariables";
    private static final String LOGGED_USER_ATTRIBUTE_NAME = User.class.getName();
    private User user;
    private Interaction interaction;

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        processRequest(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        processRequest(request, response);
    }

    @SuppressWarnings("unchecked")
    private void processRequest(HttpServletRequest request, HttpServletResponse response) throws IOException {
        request.setCharacterEncoding("UTF-8");
        user = (User) request.getAttribute("user");
        if (user == null) {
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "attribute user is null");
            return;
        }
        // id - параметр для taskId из формы задач в старом интерфейсе
        if (request.getParameter("id") == null) {
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, "parameter id not found");
            return;
        }
        // добавил в сессию user, т.к. user берётся из сессии в FormSubmissionUtils.extractVariable см.ниже
        request.getSession().setAttribute(LOGGED_USER_ATTRIBUTE_NAME, user);
        Long taskId = Long.valueOf(request.getParameter("id"));
        WfTask task = Delegates.getTaskService().getTask(user, taskId);
        interaction = Delegates.getDefinitionService().getTaskNodeInteraction(user, task.getDefinitionVersionId(), task.getNodeId());
        Map<String, String> errors = Maps.newHashMap();
        Map<String, Object> variables = extractVariables(request, interaction, 
                new DelegateProcessVariableProvider(user, task.getProcessId()), taskId.toString(), errors);
        if (errors.size() > 0) {
            final StringBuffer buff = new StringBuffer(); 
            errors.forEach((error, description) -> {
                buff.append(error + ": " + description);
            });
            response.sendError(HttpServletResponse.SC_INTERNAL_SERVER_ERROR, buff.toString());
            return;
        }
        String transitionName = null;
        if (isMultipleSubmit()) {
            transitionName = decode(request.getParameter("submitButton"));
        }
        variables.put(WfProcess.SELECTED_TRANSITION_KEY, transitionName);
        Delegates.getTaskService().completeTask(user, taskId, variables);
        FormSubmissionUtils.clearUserInputFiles(request);
        JSONObject message = new JSONObject();
        message.put("msg", "Задание выполнено");
        response.setContentType("application/json; charset=utf-8");
        response.getWriter().print(message);
    }

    private boolean isMultipleSubmit() {
        return interaction.getOutputTransitions().size() > 1;
    }

    private Map<String, Object> extractVariables(HttpServletRequest request, Interaction interaction,
            VariableProvider variableProvider, String taskId, Map<String, String> errors) {
        Enumeration<String> enumeration = request.getParameterNames();
        Map<String, Object> userInput = new HashMap<>();
        while (enumeration.hasMoreElements()) {
            String parameterName = enumeration.nextElement();
            userInput.put(parameterName, request.getParameter(parameterName));
        }
        userInput.putAll(FormSubmissionUtils.getUserInputFiles(request, taskId));
        Map<String, Object> variables = extractVariables(request, interaction, variableProvider, userInput, errors);
        request.setAttribute(USER_DEFINED_VARIABLES, variables);
        return variables;
    }

    public Map<String, Object> extractVariables(HttpServletRequest request, Interaction interaction, VariableProvider variableProvider,
            Map<String, ?> userInput, Map<String, String> errors) {
        try {
            FormComponentExtractionModel model = new FormComponentExtractionModel(variableProvider, user, new RequestWebHelper(request));
            if (interaction.getFormData() != null) {
                String template = new String(interaction.getFormData(), Charsets.UTF_8);
                FreemarkerProcessor.process(template, model);
            }
            HashMap<String, Object> variables = Maps.newHashMap();
            for (VariableDefinition variableDefinition : interaction.getVariables().values()) {
                try {
                    FormComponentSubmissionHandler handler = model.getSubmissionHandlers().get(variableDefinition.getName());
                    if (handler != null) {
                        variables.putAll(handler.extractVariables(interaction, variableDefinition, userInput, errors));
                    } else {
                        Object variableValue = FormSubmissionUtils.extractVariable(request, userInput, variableDefinition, errors);
                        if (!Objects.equal(FormSubmissionUtils.IGNORED_VALUE, variableValue)) {
                            FormComponentSubmissionPostProcessor postProcessor = model.getSubmissionPostProcessors()
                                    .get(variableDefinition.getName());
                            if (postProcessor != null) {
                                variableValue = postProcessor.postProcessValue(variableValue);
                            }
                            variables.put(variableDefinition.getName(), variableValue);
                        }
                    }
                } catch (Exception e) {
                    errors.put(variableDefinition.getName(), e.getMessage());
                }
            }
            return variables;
        } catch (Exception e) {
            throw Throwables.propagate(e);
        }
    }

    private String decode(String value) throws UnsupportedEncodingException {
        return URLDecoder.decode(value, StandardCharsets.UTF_8.toString());
    }
}
