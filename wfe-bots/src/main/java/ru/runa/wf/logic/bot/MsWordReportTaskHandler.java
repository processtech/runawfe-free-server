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
