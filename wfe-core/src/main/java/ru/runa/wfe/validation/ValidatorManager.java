package ru.runa.wfe.validation;

import com.google.common.base.Function;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.dom4j.Document;
import org.dom4j.Element;
import ru.runa.wfe.InternalApplicationException;
import ru.runa.wfe.commons.ApplicationContextFactory;
import ru.runa.wfe.commons.ClassLoaderUtil;
import ru.runa.wfe.commons.xml.XmlUtils;
import ru.runa.wfe.definition.par.ValidationXmlParser;
import ru.runa.wfe.execution.ExecutionContext;
import ru.runa.wfe.execution.dto.WfProcess;
import ru.runa.wfe.user.User;
import ru.runa.wfe.var.VariableDefinition;
import ru.runa.wfe.var.VariableProvider;

public class ValidatorManager {
    private static final Log log = LogFactory.getLog(ValidatorManager.class);
    private static final Map<String, String> validatorRegistrations = new HashMap<>();
    private static final Properties validatorProperties = ClassLoaderUtil.getLocalizedProperties("validators", ValidatorManager.class, null);
    private static final ValidatorManager instance = new ValidatorManager();

    static {
        ClassLoaderUtil.withExtensionResources("validators.xml", new Function<InputStream, Object>() {

            @Override
            public Object apply(InputStream input) {
                try (InputStream inputStream = input) {
                    Document document = XmlUtils.parseWithoutValidation(inputStream);
                    List<Element> nodes = document.getRootElement().elements("validator");
                    for (Element validatorElement : nodes) {
                        String name = validatorElement.attributeValue("name");
                        String className = validatorElement.attributeValue("class");
                        validatorRegistrations.put(name, className);
                    }
                } catch (IOException e) {
                    log.error(e.getMessage(), e);
                }
                return null;
            }
        });
    }

    public static ValidatorManager getInstance() {
        return instance;
    }

    private static String getInternalErrorMessage() {
        return (String) validatorProperties.get("internal.error");
    }

    private static String getDefaultValidationMessage(String name) {
        String key = "default.message." + name;
        if (validatorProperties.containsKey(key)) {
            return (String) validatorProperties.get(key);
        }
        return "";
    }
    
    private void addValidatorsToList (Collection<ValidatorConfig> configs, List<Validator> validators, User user, ExecutionContext executionContext,
            VariableProvider variableProvider, ValidatorContext validatorContext, Map<String, Object> variables) {
        for (ValidatorConfig config : configs) {
            if (Strings.isNullOrEmpty(config.getMessage())) {
                config.setMessage(getDefaultValidationMessage(config.getType()));
            }
            String className = validatorRegistrations.get(config.getType());
            if (className == null) {
                throw new InternalApplicationException("Validator '" + config.getType() + "' is not registered");
            }
            Validator validator = ApplicationContextFactory.createAutowiredBean(className);
            validator.init(user, executionContext, variableProvider, config, validatorContext, variables);
            validators.add(validator);
        }
    }

    public List<Validator> createValidators(User user, ExecutionContext executionContext, VariableProvider variableProvider, byte[] validationXml,
            ValidatorContext validatorContext, Map<String, Object> variables) {
        List<ValidatorConfig> configs = ValidationXmlParser.parseValidatorConfigs(validationXml);
        ArrayList<Validator> validators = new ArrayList<>(configs.size());
        addValidatorsToList (configs, validators, user, executionContext, variableProvider, validatorContext, variables);
        return validators;
    }
    
    public List<Validator> createValidatorsFromValidatorConfigs(User user, ExecutionContext executionContext, VariableProvider variableProvider,
            ValidatorContext validatorContext, Map<String, Object> variables, Map<String, ValidatorConfig> validatorConfigs, String variableName) {
        List<Validator> validators = Lists.newArrayList();
        for (ValidatorConfig config : validatorConfigs.values()) {
            if (config.getParams().get(FieldValidator.FIELD_NAME_PARAMETER_NAME) == null) {
                config.getParams().put(FieldValidator.FIELD_NAME_PARAMETER_NAME, variableName);
            };
        }
        addValidatorsToList(validatorConfigs.values(), validators, user, executionContext, variableProvider, validatorContext, variables);
        return validators;
    }
    
    public ValidatorContext validate(User user, ExecutionContext executionContext, VariableProvider variableProvider, byte[] validationXml,
            Map<String, VariableDefinition> interactionVariables, Map<String, Object> variables) {
        ValidatorContext validatorContext = new ValidatorContext();
        List<Validator> validators = createValidators(user, executionContext, variableProvider, validationXml, validatorContext, variables);
        processValidators(user, variableProvider, validators);
        return validatorContext;
    }
    
    public ValidatorContext validateVariable(User user, ExecutionContext executionContext, VariableProvider variableProvider, String variableName,
            Object value) {
        ValidatorContext validatorContext = new ValidatorContext();
        Map<String, ValidatorConfig> validatorConfigs;
        if (variableName.contains(".")) {
            int indexOfDelimiter = variableName.indexOf(".");
            validatorConfigs = variableProvider.getVariableNotNull(variableName.substring(0, indexOfDelimiter)).getDefinition().getUserType()
                    .getAttribute(variableName.substring(indexOfDelimiter + 1, variableName.length())).getValidators();
        } else {
            validatorConfigs = variableProvider.getVariableNotNull(variableName).getDefinition().getValidators();
        }
        if (validatorConfigs != null) {
            Map<String, Object> variable = Maps.newHashMap();
            variable.put(variableName, value);
            List<Validator> validators = createValidatorsFromValidatorConfigs(user, executionContext, variableProvider, validatorContext, variable,
                    validatorConfigs, variableName);
            processValidators(user, variableProvider, validators);
        }
        return validatorContext;
    }
    
    private void processValidators(User user, VariableProvider variableProvider, List<Validator> validators) {
        // can be null for single output transition
        String transitionName = (String) variableProvider.getValue(WfProcess.SELECTED_TRANSITION_KEY);
        for (Validator validator : validators) {
            if (transitionName != null && validator.getConfig().getTransitionNames().size() > 0
                    && !validator.getConfig().getTransitionNames().contains(transitionName)) {
                log.debug("Ignored validator: " + validator + " for transition " + transitionName);
                continue;
            }
            if (log.isDebugEnabled()) {
                log.debug("Running validator: " + validator);
            }
            try {
                validator.validate();
            } catch (Throwable th) {
                log.error("validator " + validator, th);
                validator.addError(getInternalErrorMessage());
            }
        }
    }

}
