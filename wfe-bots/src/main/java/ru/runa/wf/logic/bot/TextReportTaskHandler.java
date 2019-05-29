package ru.runa.wf.logic.bot;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import ru.runa.wf.logic.bot.textreport.TextReportGenerator;
import ru.runa.wf.logic.bot.textreport.TextReportSettings;
import ru.runa.wf.logic.bot.textreport.TextReportSettingsXmlParser;
import ru.runa.wfe.extension.handler.TaskHandlerBase;
import ru.runa.wfe.task.dto.WfTask;
import ru.runa.wfe.user.User;
import ru.runa.wfe.var.VariableProvider;
import ru.runa.wfe.var.file.FileVariableImpl;

/**
 * Plain text report generator.
 * 
 * @author dofs
 * @since 2.0
 */
public class TextReportTaskHandler extends TaskHandlerBase {
    private TextReportSettings settings;

    @Override
    public void setConfiguration(String configuration) {
        settings = TextReportSettingsXmlParser.read(configuration);
    }

    @Override
    public Map<String, Object> handle(User user, VariableProvider variableProvider, WfTask task) throws IOException {
        byte[] fileContent = TextReportGenerator.getReportContent(settings, variableProvider);
        Map<String, Object> vars = new HashMap<String, Object>();
        FileVariableImpl fileVariable = new FileVariableImpl(settings.getReportFileName(), fileContent, settings.getReportContentType());
        vars.put(settings.getReportVariableName(), fileVariable);
        return vars;
    }

}
