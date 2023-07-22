package ru.runa.wf.web.quick;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.dom4j.Document;
import org.dom4j.Element;

import ru.runa.wf.web.ftl.FtlFormBuilder;
import ru.runa.wfe.commons.xml.XmlUtils;
import ru.runa.wfe.var.VariableProvider;
import ru.runa.wfe.var.MapVariableProvider;
import ru.runa.wfe.var.dto.QuickFormProperty;
import ru.runa.wfe.var.dto.QuickFormVariable;
import ru.runa.wfe.var.dto.WfVariable;

import com.google.common.base.Charsets;
import com.google.common.base.Preconditions;
import com.google.common.collect.Maps;

public class QuickFormBuilder extends FtlFormBuilder {
    private static final String ELEMENT_TAGS = "tags";
    private static final String ATTRIBUTE_NAME = "name";
    private static final String ATTRIBUTE_VALUE = "value";
    private static final String ELEMENT_TAG = "tag";
    private static final String ELEMENT_PARAM = "param";
    private static final String ELEMENT_PROPERTIES = "properties";
    private static final String ELEMENT_PROPERTY = "property";

    @Override
    protected String buildForm(VariableProvider variableProvider) {
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
            if (paramElements != null && paramElements.size() > 0) {
                List<String> params = new ArrayList<String>();
                int index = 0;
                for (Element paramElement : paramElements) {
                    if (index == 0) {
                        // TODO excessive variable value invocation
                        WfVariable variable = variableProvider.getVariableNotNull(paramElement.getText());
                        quickFormVariable.setName(variable.getDefinition().getName());
                        quickFormVariable.setScriptingName(variable.getDefinition().getScriptingName());
                        quickFormVariable.setDescription(variable.getDefinition().getDescription());
                    } else {
                        params.add(paramElement.getText());
                    }
                    index++;
                }
                quickFormVariable.setParams(params.toArray(new String[params.size()]));
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

        String template = processFormTemplate(interaction.getTemplateData(), templateVariables, templateProperties);
        return processFreemarkerTemplate(template, variableProvider);
    }

    private String processFormTemplate(byte[] templateData, List<QuickFormVariable> templateVariables, List<QuickFormProperty> templateProperties) {
        Map<String, Object> map = Maps.newHashMap();
        map.put("variables", templateVariables);
        for (QuickFormProperty quickFormGpdProperty : templateProperties) {
            map.put(quickFormGpdProperty.getName(), quickFormGpdProperty.getValue() == null ? "" : quickFormGpdProperty.getValue());
        }
        VariableProvider variableProvider = new MapVariableProvider(map);
        Preconditions.checkNotNull(templateData, "Template is required");
        return processFreemarkerTemplate(new String(templateData, Charsets.UTF_8), variableProvider);
    }
}
