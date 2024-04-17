package ru.runa.wf.web.ftl.component;

import com.google.common.base.Charsets;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.io.Files;
import freemarker.template.TemplateModelException;
import java.util.List;
import java.util.Map;
import ru.runa.wfe.commons.ftl.FormComponent;
import ru.runa.wfe.commons.web.WebHelper;
import ru.runa.wfe.var.file.FileVariable;

@Deprecated
public class ViewFile extends FormComponent {
    private static final long serialVersionUID = 1L;
    private static final List<String> textFileExtensions = Lists.newArrayList("txt", "log");

    @Override
    protected Object renderRequest() throws Exception {
        String variableName = getParameterAsString(0);
        String view = getParameterAsString(1);
        FileVariable fileVariable = variableProvider.getValueNotNull(FileVariable.class, variableName);
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
