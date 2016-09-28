package ru.runa.wf.logic.bot;

import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.dom4j.Document;
import org.dom4j.Element;
import org.dom4j.io.OutputFormat;

import ru.runa.wfe.InternalApplicationException;
import ru.runa.wfe.commons.xml.XmlUtils;
import ru.runa.wfe.definition.IFileDataProvider;
import ru.runa.wfe.extension.handler.ParamDef;
import ru.runa.wfe.extension.handler.ParamsDef;
import ru.runa.wfe.service.delegate.Delegates;
import ru.runa.wfe.task.dto.WfTask;
import ru.runa.wfe.user.User;
import ru.runa.wfe.var.IVariableProvider;

import com.google.common.base.Charsets;
import com.google.common.base.Objects;
import com.google.common.base.Preconditions;
import com.google.common.base.Strings;

public class BotTaskConfigurationUtils {
    private static final Log log = LogFactory.getLog(BotTaskConfigurationUtils.class);
    private static final String TASK_PARAM = "task";
    private static final String BOT_TASK_NAME_PARAM = "botTaskName";
    private static final String ID_PARAM = "id";
    private static final String PARAMETERS_PARAM = "parameters";
    private static final String BOTCONFIG_PARAM = "botconfig";
    private static final String CONFIG_PARAM = "config";

    public static boolean isExtendedBotTaskConfiguration(byte[] configuration) {
        try {
            if (configuration != null) {
                Document document = XmlUtils.parseWithoutValidation(configuration);
                Element paramConfigElement = document.getRootElement().element(PARAMETERS_PARAM);
                Element botConfigElement = document.getRootElement().element(BOTCONFIG_PARAM);
                return paramConfigElement != null && botConfigElement != null;
            }
        } catch (Exception e) {
            log.debug("Unable to determine is bot task extended or not from configuration: " + e);
        }
        return false;
    }

    public static String getBotTaskName(User user, WfTask task) {
        Element taskElement = getBotTaskElement(user, task);
        if (taskElement != null) {
            return taskElement.attributeValue(BOT_TASK_NAME_PARAM);
        }
        return task.getName();
    }

    private static Element getBotTaskElement(User user, WfTask task) {
        String fileName = IFileDataProvider.BOTS_XML_FILE;
        if (task.getNodeId().startsWith(IFileDataProvider.SUBPROCESS_DEFINITION_PREFIX)) {
            int index = task.getNodeId().indexOf(".");
            if (index > 0) {
                fileName = task.getNodeId().substring(0, index) + "." + fileName;
            }
        }
        byte[] xml = Delegates.getDefinitionService().getProcessDefinitionFile(user, task.getDefinitionId(), fileName);
        if (xml == null) {
            // this is the case of simple bot task
            return null;
        }
        Document document = XmlUtils.parseWithoutValidation(xml);
        List<Element> elements = document.getRootElement().elements(TASK_PARAM);
        for (Element element : elements) {
            if (Objects.equal(task.getNodeId(), element.attributeValue(ID_PARAM))) {
                return element;
            }
        }
        return null;
    }

    public static ParamsDef getExtendedBotTaskParameters(User user, WfTask task, byte[] configuration) {
        Document document = XmlUtils.parseWithoutValidation(configuration);
        Element parametersElement = document.getRootElement().element(PARAMETERS_PARAM);
        ParamsDef result = ParamsDef.parse(parametersElement.element(CONFIG_PARAM));
        Element taskElement = getBotTaskElement(user, task);
        Preconditions.checkNotNull(taskElement, "Unable to get bot task link xml");
        Element configElement = taskElement.element(CONFIG_PARAM);
        if (configElement == null) {
            if (result.getInputParams().size() > 0 || result.getOutputParams().size() > 0) {
                throw new InternalApplicationException("Unable to apply bot task link to parameters");
            }
            return result;
        }
        ParamsDef taskParamsDef = ParamsDef.parse(configElement);
        for (ParamDef paramDef : result.getInputParams().values()) {
            ParamDef taskParamDef = taskParamsDef.getInputParam(paramDef.getName());
            if (taskParamDef == null) {
                if (paramDef.isOptional()) {
                    continue;
                }
                throw new InternalApplicationException("no taskParamDef found for param " + paramDef);
            }
            substituteParameter(paramDef, taskParamDef);
        }
        for (ParamDef paramDef : result.getOutputParams().values()) {
            ParamDef taskParamDef = taskParamsDef.getOutputParam(paramDef.getName());
            if (taskParamDef == null) {
                if (paramDef.isOptional()) {
                    continue;
                }
                throw new InternalApplicationException("no taskParamDef found for param " + paramDef);
            }
            substituteParameter(paramDef, taskParamDef);
        }
        return result;
    }

    public static byte[] getExtendedBotTaskConfiguration(byte[] configuration) {
        Document document = XmlUtils.parseWithoutValidation(configuration);
        Element botConfigElement = document.getRootElement().element(BOTCONFIG_PARAM);
        String substituted;
        if (botConfigElement.elements().size() > 0) {
            Element taskConfigElement = (Element) botConfigElement.elements().get(0);
            substituted = XmlUtils.toString(taskConfigElement, OutputFormat.createPrettyPrint());
        } else {
            substituted = botConfigElement.getText();
        }
        return substituted.getBytes(Charsets.UTF_8);
    }

    private static void substituteParameter(ParamDef paramDef, ParamDef taskParamDef) {
        if (!Strings.isNullOrEmpty(taskParamDef.getVariableName())) {
            paramDef.setVariableName(taskParamDef.getVariableName());
        } else if (!Strings.isNullOrEmpty(taskParamDef.getValueAsString())) {
            paramDef.setValue(taskParamDef.getValue());
        } else {
            throw new InternalApplicationException("no replacement found for param " + paramDef);
        }
    }

    public static boolean isParameterizedBotTaskConfiguration(byte[] configuration) {
        try {
            if (configuration != null) {
                Document document = XmlUtils.parseWithoutValidation(configuration);
                ParamsDef paramsDef = ParamsDef.parse(document.getRootElement());
                return paramsDef.getInputParams().size() + paramsDef.getOutputParams().size() > 0;
            }
        } catch (Exception e) {
            log.debug("Unable to determine is bot task parameterized or not from configuration: " + e);
        }
        return false;
    }

    public static byte[] substituteParameterizedConfiguration(User user, WfTask task, byte[] configuration, IVariableProvider variableProvider) {
        Element taskElement = getBotTaskElement(user, task);
        if (taskElement == null) {
            return configuration;
        }
        Element configElement = taskElement.element(CONFIG_PARAM);
        ParamsDef taskParamsDef = ParamsDef.parse(configElement);

        Document document = XmlUtils.parseWithoutValidation(configuration);
        Element root = document.getRootElement();
        Element inputElement = root.element("input");
        if (inputElement != null) {
            List<Element> inputParamElements = inputElement.elements("param");
            for (Element element : inputParamElements) {
                String paramName = element.attributeValue("name");
                ParamDef paramDef = taskParamsDef.getInputParam(paramName);
                if (paramDef == null) {
                    // optional parameter
                    continue;
                }
                if (!Strings.isNullOrEmpty(paramDef.getVariableName())) {
                    element.addAttribute("variable", paramDef.getVariableName());
                } else if (!Strings.isNullOrEmpty(paramDef.getValueAsString())) {
                    element.addAttribute("value", paramDef.getValueAsString());
                }
            }
        }
        Element outputElement = root.element("output");
        if (outputElement != null) {
            List<Element> outputParamElements = outputElement.elements("param");
            for (Element element : outputParamElements) {
                String paramName = element.attributeValue("name");
                ParamDef paramDef = taskParamsDef.getOutputParam(paramName);
                if (paramDef == null) {
                    // optional parameter
                    continue;
                }
                if (!Strings.isNullOrEmpty(paramDef.getVariableName())) {
                    element.addAttribute("variable", paramDef.getVariableName());
                } else if (!Strings.isNullOrEmpty(paramDef.getValueAsString())) {
                    element.addAttribute("value", paramDef.getValueAsString());
                }
            }
        }
        return XmlUtils.save(document);
    }
}
