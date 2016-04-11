/*
 * This file is part of the RUNA WFE project.
 * 
 * This program is free software; you can redistribute it and/or 
 * modify it under the terms of the GNU Lesser General Public License 
 * as published by the Free Software Foundation; version 2.1 
 * of the License. 
 * 
 * This program is distributed in the hope that it will be useful, 
 * but WITHOUT ANY WARRANTY; without even the implied warranty of 
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the 
 * GNU Lesser General Public License for more details. 
 * 
 * You should have received a copy of the GNU Lesser General Public License 
 * along with this program; if not, write to the Free Software 
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA.
 */
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
import ru.runa.wfe.var.IVariableProvider;
import ru.runa.wfe.var.file.IFileVariable;

import com.google.common.collect.Maps;

/**
 * Created on 14.06.2005
 * 
 */
public class FileVariableValueDownloadVarTag implements VarTag {

    @Override
    public String getHtml(User user, String varName, Object var, PageContext pageContext, IVariableProvider variableProvider) {
        if (pageContext == null || var == null) {
            return "";
        }
        IFileVariable fileVariable = TypeConversionUtil.convertTo(IFileVariable.class, var);
        A ahref = new A();
        ahref.addElement(new StringElement(fileVariable.getName()));

        Map<String, Object> parametersMap = Maps.newHashMap();
        parametersMap.put("id", variableProvider.getProcessId());
        parametersMap.put("variableName", varName);
        ahref.setHref(Commons.getActionUrl("/variableDownloader", parametersMap, pageContext, PortletUrlType.Render));
        return ahref.toString();
    }
}
