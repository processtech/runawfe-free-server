/*
 * This file is part of the RUNA WFE project.
 * Copyright (C) 2004-2006, Joint stock company "RUNA Technology"
 * All rights reserved.
 * 
 * 
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 * 
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 */
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
import ru.runa.wfe.var.IVariableProvider;
import ru.runa.wfe.var.file.FileVariable;

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
    public Map<String, Object> handle(User user, IVariableProvider variableProvider, WfTask task) throws IOException {
        byte[] fileContent = TextReportGenerator.getReportContent(settings, variableProvider);
        Map<String, Object> vars = new HashMap<String, Object>();
        FileVariable fileVariable = new FileVariable(settings.getReportFileName(), fileContent, settings.getReportContentType());
        vars.put(settings.getReportVariableName(), fileVariable);
        return vars;
    }

}
