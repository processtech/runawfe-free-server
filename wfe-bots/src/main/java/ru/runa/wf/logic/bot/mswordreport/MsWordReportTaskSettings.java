package ru.runa.wf.logic.bot.mswordreport;

import com.google.common.collect.Lists;
import java.io.File;
import java.util.List;
import lombok.extern.apachecommons.CommonsLog;
import ru.runa.wfe.commons.ClassLoaderUtil;

@CommonsLog
public class MsWordReportTaskSettings {
    private final boolean strictMode;
    private final String templateFileLocation;
    private final String reportFileName;
    private final String reportVariableName;
    private final List<BookmarkVariableMapping> mappings = Lists.newArrayList();

    public MsWordReportTaskSettings(boolean strictMode, String templateFileLocation, String reportFileName, String reportVariableName) {
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
            throw new MsWordReportException(MsWordReportException.TEMPLATE_NOT_FOUND, templateFileLocation);
        } catch (Exception e) {
            log.error("", e);
            throw new MsWordReportException(MsWordReportException.TEMPLATE_NOT_FOUND, templateFileLocation);
        }
    }

    public List<BookmarkVariableMapping> getMappings() {
        return mappings;
    }
}
