package ru.runa.wf.logic.bot;

import java.io.File;
import java.io.FileInputStream;
import java.util.Map;

import ru.runa.wf.logic.bot.mswordreport.MsWordReportBuilder;
import ru.runa.wf.logic.bot.mswordreport.MsWordReportBuilderFactory;
import ru.runa.wf.logic.bot.mswordreport.MsWordReportTaskSettings;
import ru.runa.wf.logic.bot.mswordreport.MsWordReportSettingsXmlParser;
import ru.runa.wfe.extension.handler.CommonHandler;
import ru.runa.wfe.var.VariableProvider;
import ru.runa.wfe.var.file.FileVariableImpl;

import com.google.common.collect.Maps;
import com.google.common.io.ByteStreams;

/**
 * 
 * Reads template word document. Replaces all bookmarks by rules provided in
 * configuration.
 * 
 * Created on 23.11.2006
 * 
 */
public class MsWordReportTaskHandler extends CommonHandler {
    private static final String CONTENT_TYPE = "application/vnd.ms-word";

    private MsWordReportTaskSettings settings;

    @Override
    public void setConfiguration(String configuration) {
        settings = MsWordReportSettingsXmlParser.read(configuration);
    }

    @Override
    protected Map<String, Object> executeAction(VariableProvider variableProvider) throws Exception {
        File reportTemporaryFile = null;
        FileInputStream reportFileInputStream = null;
        try {
            reportTemporaryFile = File.createTempFile("prefix", ".doc");
            MsWordReportBuilder wordReportBuilder = MsWordReportBuilderFactory.createBuilder(settings, variableProvider);
            log.debug("Using template " + settings.getTemplateFileLocation());
            wordReportBuilder.build(reportTemporaryFile.getAbsolutePath());
            reportFileInputStream = new FileInputStream(reportTemporaryFile);
            byte[] fileContent = ByteStreams.toByteArray(reportFileInputStream);
            FileVariableImpl fileVariable = new FileVariableImpl(settings.getReportFileName(), fileContent, CONTENT_TYPE);
            Map<String, Object> result = Maps.newHashMapWithExpectedSize(1);
            result.put(settings.getReportVariableName(), fileVariable);
            return result;
        } finally {
            if (reportFileInputStream != null) {
                reportFileInputStream.close();
            }
            if (reportTemporaryFile != null) {
                if (!reportTemporaryFile.delete()) {
                    log.warn("Unable to delete " + reportTemporaryFile.getAbsolutePath());
                }
            }
        }
    }
}
