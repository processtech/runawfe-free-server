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
package ru.runa.wf.logic.bot.mswordreport;

import com.google.common.collect.Lists;
import java.io.File;
import java.util.List;
import lombok.extern.apachecommons.CommonsLog;
import ru.runa.wfe.commons.ClassLoaderUtil;

@CommonsLog
public class MSWordReportTaskSettings {
    private final boolean strictMode;
    private final String templateFileLocation;
    private final String reportFileName;
    private final String reportVariableName;
    private final List<BookmarkVariableMapping> mappings = Lists.newArrayList();

    public MSWordReportTaskSettings(boolean strictMode, String templateFileLocation, String reportFileName, String reportVariableName) {
        this.strictMode = strictMode;
        this.templateFileLocation = templateFileLocation;
        this.reportFileName = reportFileName;
        this.reportVariableName = reportVariableName;
    }

    public boolean isStrictMode() {
        return strictMode;
    }

    public String getReportFileName() {
        return reportFileName;
    }

    public String getReportVariableName() {
        return reportVariableName;
    }

    public String getTemplateFileLocation() {
        return templateFileLocation;
    }

    public String getTemplateFilePath() {
        File file = new File(templateFileLocation);
        if (file.exists()) {
            return templateFileLocation;
        }
        try {
            file = new File(ClassLoaderUtil.getAsURLNotNull(templateFileLocation, getClass()).toURI());
            if (file.exists()) {
                return file.getAbsolutePath();
            }
            throw new MSWordReportException(MSWordReportException.TEMPLATE_NOT_FOUND, templateFileLocation);
        } catch (Exception e) {
            log.error("", e);
            throw new MSWordReportException(MSWordReportException.TEMPLATE_NOT_FOUND, templateFileLocation);
        }
    }

    public List<BookmarkVariableMapping> getMappings() {
        return mappings;
    }

}
