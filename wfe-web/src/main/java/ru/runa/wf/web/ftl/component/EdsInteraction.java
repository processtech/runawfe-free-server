package ru.runa.wf.web.ftl.component;

import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import ru.runa.wf.web.form.ProcessForm;
import ru.runa.wf.web.servlet.UploadedFile;
import ru.runa.wfe.commons.ftl.FormComponent;
import ru.runa.wfe.commons.ftl.FormComponentSubmissionHandler;
import ru.runa.wfe.form.Interaction;
import ru.runa.wfe.service.delegate.Delegates;
import ru.runa.wfe.task.dto.WfTask;
import ru.runa.wfe.var.UserTypeMap;
import ru.runa.wfe.var.VariableDefinition;
import ru.runa.wfe.var.dto.WfVariable;
import ru.runa.wfe.var.file.FileVariableImpl;

public class EdsInteraction extends FormComponent implements FormComponentSubmissionHandler {
    public static final String SIGNED_FILE = "Подписываемый файл";
    public static final String SIGNATURES_LIST = "Список подписей";
    public static final String SIGNATURE_USER_TYPE_NAME = "Подпись";
    public static final String SIGNATURE_FILE = "Файл подписи";
    public static final String SIGNATURE_SIGNATORY = "Подписант";
    public static final String SIGNATURE_IS_ACQUIRED_BY_SUBSTITUTION = "Выполнено по замещению";
    public static final String SIGNATURE_ROLE = "Роль";
    public static final List<String> SIGNATURE_ATTRIBUTES = Lists.newArrayList(
            SIGNATURE_FILE, SIGNATURE_SIGNATORY, SIGNATURE_IS_ACQUIRED_BY_SUBSTITUTION, SIGNATURE_ROLE);

    @Override
    protected Object renderRequest() throws Exception {
        WfVariable eds = variableProvider.getVariableNotNull(getParameterAsString(0));
        if (isReadonly()) {
            return ViewUtil.wrapDisplayVariable(eds, ViewUtil.getComponentOutput(user, webHelper, variableProvider.getProcessId(), eds));
        }
        WfVariable file = ((UserTypeMap) eds.getValue()).getAttributeValue(SIGNED_FILE);
        WfVariable signature = variableProvider.getVariableNotNull(getParameterAsString(1));
        return ViewUtil.wrapDisplayVariable(file, ViewUtil.getComponentOutput(user, webHelper, variableProvider.getProcessId(), file))
                + ViewUtil.wrapInputVariable(signature, ViewUtil.getComponentInput(user, webHelper, signature));
    }

    @Override
    public Map<String, ?> extractVariables(Interaction interaction, VariableDefinition definition, Map<String, ?> userInput, Map<String, String> errors) {
        if (isReadonly()) {
            return new HashMap<>();
        }
        UserTypeMap edsValue = (UserTypeMap) variableProvider.getVariableNotNull(getParameterAsString(0)).getValue();
        List<UserTypeMap> signatures = getSignatures(edsValue, userInput, errors);
        if (!errors.isEmpty()) {
            return new HashMap<>();
        }
        edsValue.put(SIGNATURES_LIST, signatures);
        return new HashMap<String, Object>() {{
            put(getParameterAsString(0), edsValue);
            put(getParameterAsString(1), null);
        }};
    }

    private List<UserTypeMap> getSignatures(UserTypeMap edsValue, Map<String, ?> userInput, Map<String, String> errors) {
        Object file = userInput.get(getParameterAsString(1));
        if (!(file instanceof UploadedFile)) {
            errors.put(SIGNATURE_FILE + " обязателен для заполнения", "");
            return Lists.newArrayList();
        }
        Long taskId = Long.valueOf(((String[]) userInput.get(ProcessForm.ID_INPUT_NAME))[0]);
        List<UserTypeMap> signatures = (List<UserTypeMap>) edsValue.getAttributeValue(SIGNATURES_LIST).getValue();
        if (signatures == null) {
            return Lists.newArrayList(getSignature((UploadedFile) file, taskId, errors));
        }
        signatures.add(getSignature((UploadedFile) file, taskId, errors));
        return signatures;
    }

    private UserTypeMap getSignature(UploadedFile file, Long taskId, Map<String, String> errors) {
        WfTask task = Delegates.getTaskService().getTask(user, taskId);
        UserTypeMap signature = new UserTypeMap(variableProvider.getUserType(SIGNATURE_USER_TYPE_NAME));
        if (SIGNATURE_ATTRIBUTES.size() != signature.getUserType().getAttributes().size() ||
                !signature.getUserType().getAttributes().stream().map(VariableDefinition::getName).allMatch(SIGNATURE_ATTRIBUTES::contains)) {
            errors.put("Тип '" + SIGNATURE_USER_TYPE_NAME + "' должен содержать атрибуты: " + SIGNATURE_ATTRIBUTES, "");
            return signature;
        }
        signature.put(SIGNATURE_FILE, new FileVariableImpl(file.getName(), file.getContent(), file.getMimeType()));
        signature.put(SIGNATURE_SIGNATORY, user.getActor());
        signature.put(SIGNATURE_IS_ACQUIRED_BY_SUBSTITUTION, task.isAcquiredBySubstitution());
        signature.put(SIGNATURE_ROLE, task.getSwimlaneName());
        return signature;
    }

    private boolean isReadonly() {
        return Strings.isNullOrEmpty(getParameterAsString(1));
    }
}
