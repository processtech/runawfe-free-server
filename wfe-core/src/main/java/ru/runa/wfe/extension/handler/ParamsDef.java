package ru.runa.wfe.extension.handler;

import java.util.List;
import java.util.Map;

import org.dom4j.Document;
import org.dom4j.Element;

import ru.runa.wfe.InternalApplicationException;
import ru.runa.wfe.commons.xml.XmlUtils;
import ru.runa.wfe.var.IVariableProvider;

import com.google.common.base.Preconditions;
import com.google.common.base.Strings;
import com.google.common.collect.Maps;

@SuppressWarnings("unchecked")
public class ParamsDef {
    private final Map<String, ParamDef> inputParams = Maps.newHashMap();
    private final Map<String, ParamDef> outputParams = Maps.newHashMap();

    private ParamsDef() {
    }

    public static ParamsDef parse(String configuration) {
        if (Strings.isNullOrEmpty(configuration)) {
            return new ParamsDef();
        }
        Document doc = XmlUtils.parseWithoutValidation(configuration);
        return parse(doc.getRootElement());
    }

    public static ParamsDef parse(Element root) {
        Preconditions.checkNotNull(root);
        ParamsDef paramsDef = new ParamsDef();
        Element inputElement = root.element("input");
        if (inputElement != null) {
            List<Element> inputParamElements = inputElement.elements("param");
            for (Element element : inputParamElements) {
                ParamDef paramDef = new ParamDef(element);
                paramsDef.inputParams.put(paramDef.getName(), paramDef);
            }
        }
        Element outputElement = root.element("output");
        if (outputElement != null) {
            List<Element> outputParamElements = outputElement.elements("param");
            for (Element element : outputParamElements) {
                ParamDef paramDef = new ParamDef(element);
                paramsDef.outputParams.put(paramDef.getName(), paramDef);
            }
        }
        return paramsDef;
    }

    public Map<String, ParamDef> getInputParams() {
        return inputParams;
    }

    public Map<String, ParamDef> getOutputParams() {
        return outputParams;
    }

    public ParamDef getInputParam(String name) {
        return inputParams.get(name);
    }

    public ParamDef getInputParamNotNull(String name) {
        ParamDef result = getInputParam(name);
        if (result == null) {
            throw new InternalApplicationException("Input parameter '" + name + "' is not defined.");
        }
        return result;
    }

    public <T> T getInputParamValue(String name, IVariableProvider variableProvider) {
        ParamDef paramDef = getInputParam(name);
        if (paramDef == null) {
            return null;
        }
        T result = null;
        if (paramDef.getVariableName() != null) {
            result = (T) variableProvider.getValue(paramDef.getVariableName());
        }
        if (result == null && paramDef.getValue() != null) {
            result = (T) paramDef.getValue();
        }
        return result;
    }

    public <T> T getInputParamValueNotNull(String name, IVariableProvider variableProvider) {
        Object result = getInputParamValue(name, variableProvider);
        if (result == null) {
            // for more appropriate exception
            getInputParamNotNull(name);
            throw new InternalApplicationException("Input parameter '" + name + "' resolved as null.");
        }
        return (T) result;
    }

    public ParamDef getOutputParam(String name) {
        return outputParams.get(name);
    }

    public ParamDef getOutputParamNotNull(String name) {
        ParamDef result = getOutputParam(name);
        if (result == null) {
            throw new InternalApplicationException("Output parameter '" + name + "' is not defined.");
        }
        return result;
    }
}
