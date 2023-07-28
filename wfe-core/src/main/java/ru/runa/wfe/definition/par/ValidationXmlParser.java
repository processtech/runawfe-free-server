package ru.runa.wfe.definition.par;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.dom4j.Document;
import org.dom4j.Element;

import ru.runa.wfe.commons.SystemProperties;
import ru.runa.wfe.commons.xml.XmlUtils;
import ru.runa.wfe.definition.InvalidDefinitionException;
import ru.runa.wfe.lang.ParsedProcessDefinition;
import ru.runa.wfe.validation.FieldValidator;
import ru.runa.wfe.validation.ValidatorConfig;

import com.google.common.base.Throwables;

public class ValidationXmlParser {
    private static final String FIELD_ELEMENT_NAME = "field";
    private static final String FIELD_VALIDATOR_ELEMENT_NAME = "field-validator";
    private static final String GLOBAL_VALIDATOR_ELEMENT_NAME = "validator";
    private static final String NAME_ATTRIBUTE_NAME = "name";
    private static final String TYPE_ATTRIBUTE_NAME = "type";
    private static final String TRANSITION_CONTEXT_ELEMENT_NAME = "transition-context";
    private static final String TRANSITION_NAME_ELEMENT_NAME = "on";
    private static final String MESSAGE_ELEMENT_NAME = "message";
    private static final String PARAM_ELEMENT_NAME = "param";

    public static List<String> readVariableNames(ParsedProcessDefinition parsedProcessDefinition, String fileName, byte[] xmlFileBytes) {
        try {
            Document document = XmlUtils.parseWithoutValidation(xmlFileBytes);
            List<Element> fieldElements = document.getRootElement().elements(FIELD_ELEMENT_NAME);
            List<String> varNames = new ArrayList<String>(fieldElements.size());
            for (Element fieldElement : fieldElements) {
                varNames.add(fieldElement.attributeValue(NAME_ATTRIBUTE_NAME).intern());
            }
            return varNames;
        } catch (Exception e) {
            Throwables.propagateIfInstanceOf(e, InvalidDefinitionException.class);
            throw new InvalidDefinitionException(parsedProcessDefinition.getName(), "Error in " + fileName, e);
        }
    }

    public static List<String> readRequiredVariableNames(ParsedProcessDefinition parsedProcessDefinition, byte[] xmlFileBytes) {
        try {
            Document document = XmlUtils.parseWithoutValidation(xmlFileBytes);
            List<Element> fieldElements = document.getRootElement().elements(FIELD_ELEMENT_NAME);
            List<String> variableNames = new ArrayList<String>(fieldElements.size());
            List<String> requiredValidatorNames = SystemProperties.getRequiredValidatorNames();
            for (Element fieldElement : fieldElements) {
                String variableName = fieldElement.attributeValue(NAME_ATTRIBUTE_NAME);
                List<Element> validatorElements = fieldElement.elements(FIELD_VALIDATOR_ELEMENT_NAME);
                for (Element validatorElement : validatorElements) {
                    String typeName = validatorElement.attributeValue(TYPE_ATTRIBUTE_NAME);
                    if (requiredValidatorNames.contains(typeName)) {
                        if (validatorElement.element(TRANSITION_CONTEXT_ELEMENT_NAME) == null) {
                            variableNames.add(variableName.intern());
                        }
                    }
                }
            }
            return variableNames;
        } catch (Exception e) {
            Throwables.propagateIfInstanceOf(e, InvalidDefinitionException.class);
            throw new InvalidDefinitionException(parsedProcessDefinition.getName(), e);
        }
    }

    public static List<ValidatorConfig> parseValidatorConfigs(byte[] validationXml) {
        List<ValidatorConfig> configs = new ArrayList<ValidatorConfig>();
        Document doc = XmlUtils.parseWithoutValidation(validationXml);
        List<Element> fieldElements = doc.getRootElement().elements(FIELD_ELEMENT_NAME);
        List<Element> validatorElements = doc.getRootElement().elements(GLOBAL_VALIDATOR_ELEMENT_NAME);
        addValidatorConfigs(validatorElements, new HashMap<String, String>(), configs);
        for (Element fieldElement : fieldElements) {
            String fieldName = fieldElement.attributeValue(NAME_ATTRIBUTE_NAME);
            Map<String, String> extraParams = new HashMap<String, String>();
            extraParams.put(FieldValidator.FIELD_NAME_PARAMETER_NAME, fieldName);
            validatorElements = fieldElement.elements(FIELD_VALIDATOR_ELEMENT_NAME);
            addValidatorConfigs(validatorElements, extraParams, configs);
        }
        return configs;
    }

    private static void addValidatorConfigs(List<Element> validatorElements, Map<String, String> extraParams, List<ValidatorConfig> configs) {
        for (Element validatorElement : validatorElements) {
            String validatorType = validatorElement.attributeValue(TYPE_ATTRIBUTE_NAME);
            ValidatorConfig config = new ValidatorConfig(validatorType);
            Element transitionsElement = validatorElement.element(TRANSITION_CONTEXT_ELEMENT_NAME);
            if (transitionsElement != null) {
                List<Element> transitionElements = transitionsElement.elements(TRANSITION_NAME_ELEMENT_NAME);
                for (Element transitionElement : transitionElements) {
                    config.getTransitionNames().add(transitionElement.getTextTrim());
                }
            }
            config.getParams().putAll(extraParams);
            List<Element> paramElements = validatorElement.elements(PARAM_ELEMENT_NAME);
            for (Element paramElement : paramElements) {
                String paramName = paramElement.attributeValue(NAME_ATTRIBUTE_NAME);
                String text = paramElement.getText();
                config.getParams().put(paramName, text);
            }
            config.setMessage(validatorElement.elementText(MESSAGE_ELEMENT_NAME));
            configs.add(config);
        }
    }

}
