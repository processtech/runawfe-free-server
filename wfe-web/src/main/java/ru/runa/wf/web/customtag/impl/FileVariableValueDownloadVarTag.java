package ru.runa.wf.web.customtag.impl;

import java.util.Map;

import javax.servlet.jsp.PageContext;

import org.apache.ecs.StringElement;
import org.apache.ecs.html.A;

import ru.runa.common.web.Commons;
import ru.runa.wf.web.customtag.VarTag;
import ru.runa.wfe.commons.TypeConversionUtil;
import ru.runa.wfe.commons.web.PortletUrlType;
import ru.runa.wfe.user.User;
import ru.runa.wfe.var.VariableProvider;
import ru.runa.wfe.var.file.FileVariable;

import com.google.common.collect.Maps;

/**
 * Created on 14.06.2005
 * 
 */
public class FileVariableValueDownloadVarTag implements VarTag {

    @Override
    public String getHtml(User user, String varName, Object var, PageContext pageContext, VariableProvider variableProvider) {
        if (pageContext == null || var == null) {
            return "";
        }
        FileVariable fileVariable = TypeConversionUtil.convertTo(FileVariable.class, var);
        A ahref = new A();
        ahref.addElement(new StringElement(fileVariable.getName()));

        Map<String, Object> parametersMap = Maps.newHashMap();
        parametersMap.put("id", variableProvider.getProcessId());
        parametersMap.put("variableName", varName);
        ahref.setHref(Commons.getActionUrl("/variableDownloader", parametersMap, pageContext, PortletUrlType.Render));
        return ahref.toString();
    }
}
