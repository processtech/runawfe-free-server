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
package ru.runa.wf.web.ftl.component;

import java.util.List;
import java.util.Map;

import ru.runa.wfe.commons.ftl.FormComponent;
import ru.runa.wfe.commons.web.WebHelper;
import ru.runa.wfe.var.file.IFileVariable;

import com.google.common.base.Charsets;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.io.Files;

import freemarker.template.TemplateModelException;

public class ViewFile extends FormComponent {
    private static final long serialVersionUID = 1L;
    private static final List<String> textFileExtensions = Lists.newArrayList("txt", "log");

    @Override
    protected Object renderRequest() throws Exception {
        String variableName = getParameterAsString(0);
        String view = getParameterAsString(1);
        IFileVariable fileVariable = variableProvider.getValueNotNull(IFileVariable.class, variableName);
        if ("content".equals(view)) {
            String content = new String(fileVariable.getData(), Charsets.UTF_8);
            if (textFileExtensions.contains(Files.getFileExtension(fileVariable.getName()))) {
                content = content.replaceAll("\n", "<br>");
                content = content.replaceAll("\t", "&nbsp;&nbsp;&nbsp;&nbsp;");
            }
            return content;
        } else if ("contentlength".equals(view)) {
            return fileVariable.getData().length;
        } else if ("contenttype".equals(view)) {
            return fileVariable.getContentType();
        } else if ("raw".equals(view)) {
            return fileVariable;
        } else if ("drawimage".equals(view)) {
            String fileName = fileVariable.getName();
            webHelper.getRequest().getSession().setAttribute(fileName, fileVariable);
            Map<String, String> params = Maps.newHashMap();
            params.put(WebHelper.PARAM_FILE_NAME, fileName);
            String actionUrl = webHelper.getActionUrl(WebHelper.ACTION_DOWNLOAD_SESSION_FILE, params);
            return "<img src='" + actionUrl + "' />";
        } else {
            throw new TemplateModelException("Unexpected value of VIEW parameter: " + view);
        }
    }

}
