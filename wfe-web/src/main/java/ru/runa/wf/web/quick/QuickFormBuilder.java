package ru.runa.wf.web.quick;

import com.google.common.base.Charsets;
import com.google.common.base.Preconditions;
import com.google.common.collect.Maps;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.dom4j.Document;
import org.dom4j.Element;
import ru.runa.wf.web.ftl.FtlFormBuilder;
import ru.runa.wfe.commons.ftl.FreemarkerConfiguration;
import ru.runa.wfe.commons.xml.XmlUtils;
import ru.runa.wfe.var.MapDelegableVariableProvider;
import ru.runa.wfe.var.MapVariableProvider;
import ru.runa.wfe.var.VariableProvider;
import ru.runa.wfe.var.dto.QuickFormProperty;
import ru.runa.wfe.var.dto.QuickFormVariable;
import ru.runa.wfe.var.dto.WfVariable;

public class QuickFormBuilder extends FtlFormBuilder {
    private static final String ELEMENT_TAGS = "tags";
    private static final String ATTRIBUTE_NAME = "name";
    private static final String ATTRIBUTE_VALUE = "value";
    private static final String ELEMENT_TAG = "tag";
    private static final String ELEMENT_PARAM = "param";
    private static final String ELEMENT_PROPERTIES = "properties";
    private static final String ELEMENT_PROPERTY = "property";
    private static final String ELEMENT_PARAM_ITEM = "item";
    private static final String ATTRIBUTE_MULTIPLE = "multiple";
    private static final String ATTRIBUTE_MULTIPLE_VALUE_TRUE = "true";

    @Override
    protected String buildForm(VariableProvider variableProvider) {
        String ftlFormData = toFtlFormData(variableProvider);

        Map<String, Object> variables = loadDraftData(user, task.getId());
        VariableProvider decorator = new MapDelegableVariableProvider(variables, variableProvider);

        return processFreemarkerTemplate(ftlFormData, decorator, true);
    }

    public String toFtlFormData(VariableProvider variableProvider) {
        String quickForm = new String(interaction.getFormData(), Charsets.UTF_8);
        List<QuickFormVariable> templateVariables = new ArrayList<QuickFormVariable>();
        Document document = XmlUtils.parseWithoutValidation(quickForm);
        Element tagsElement = document.getRootElement().element(ELEMENT_TAGS);
        List<Element> varElementsList = tagsElement.elements(ELEMENT_TAG);
        for (Element varElement : varElementsList) {
            String tag = varElement.elementText(ATTRIBUTE_NAME);
            QuickFormVariable quickFormVariable = new QuickFormVariable();
            quickFormVariable.setTagName(tag);
            List<Element> paramElements = varElement.elements(ELEMENT_PARAM);
            int paramElementsSize = paramElements.size();
            if (paramElements != null && paramElementsSize > 0) {
                List<Object> params = new ArrayList<Object>(paramElementsSize);
                int index = 0;
                int mainVariableIndex = FreemarkerConfiguration.getTagMainVariableIndex(tag);
                for (Element paramElement : paramElements) {
                    if (index == mainVariableIndex) {
                        WfVariable variable = variableProvider.getVariableNotNull(paramElement.getText());
                        quickFormVariable.setName(variable.getDefinition().getName());
                        quickFormVariable.setScriptingName(variable.getDefinition().getScriptingName());
                        quickFormVariable.setDescription(variable.getDefinition().getDescription());
                    }
                    params.add(getParamFromElement(paramElement));
                    index++;
                }
                quickFormVariable.setParams(params);
            }
            templateVariables.add(quickFormVariable);
        }
        List<QuickFormProperty> templateProperties = new ArrayList<QuickFormProperty>();
        Element propertiesElement = document.getRootElement().element(ELEMENT_PROPERTIES);
        if (propertiesElement != null) {
            List<Element> varPrElementsList = propertiesElement.elements(ELEMENT_PROPERTY);
            for (Element varElement : varPrElementsList) {
                QuickFormProperty quickFormGpdProperty = new QuickFormProperty();
                quickFormGpdProperty.setName(varElement.elementText(ATTRIBUTE_NAME));
                quickFormGpdProperty.setValue(varElement.elementText(ATTRIBUTE_VALUE));
                templateProperties.add(quickFormGpdProperty);
            }
        }
        return processFormTemplate(interaction.getTemplateData(), templateVariables, templateProperties);
    }

    private String processFormTemplate(byte[] templateData, List<QuickFormVariable> templateVariables, List<QuickFormProperty> templateProperties) {
        Map<String, Object> map = Maps.newHashMap();
        map.put("variables", templateVariables);
        for (QuickFormProperty quickFormGpdProperty : templateProperties) {
            map.put(quickFormGpdProperty.getName(), quickFormGpdProperty.getValue() == null ? "" : quickFormGpdProperty.getValue());
        }
        VariableProvider variableProvider = new MapVariableProvider(map);
        Preconditions.checkNotNull(templateData, "Template is required");
        return processFreemarkerTemplate(new String(templateData, Charsets.UTF_8), variableProvider, false);
    }

    private static Object getParamFromElement(Element element) {
        String isMultiple = element.attributeValue(ATTRIBUTE_MULTIPLE);
        if (isMultiple != null && isMultiple.equals(ATTRIBUTE_MULTIPLE_VALUE_TRUE)) {
            List<Element> paramItemElements = element.elements(ELEMENT_PARAM_ITEM);
            List<String> result = new ArrayList<>(paramItemElements.size());
            for (Element i : paramItemElements) {
                result.add(i.getText());
            }
            return result;
        } else {
            return element.getText();
        }
    }
}
