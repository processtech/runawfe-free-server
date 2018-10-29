package ru.runa.wf.logic.bot.textreport;


/**
 * Created on 2006
 * 
 */
public class TextReportSettings {
    private String templateFileName;
    private String reportVariableName;
    private String reportFileName;
    private String reportContentType;
    private String[] contextSymbols;
    private String[] contextReplacements;

    /**
     * true for applying replacements to REGEXP
     */
    private boolean applyToRegexp;
    private String reportEncoding;
    private String templateEncoding;
    /**
     * true for XML set languages
     */
    private boolean xmlFormatSupport;

    public String[] getContextSymbols() {
        return contextSymbols;
    }

    public String[] getContextReplacements() {
        return contextReplacements;
    }

    public String getReportContentType() {
        return reportContentType;
    }

    public void setReportContentType(String reportContentType) {
        this.reportContentType = reportContentType;
    }

    public boolean isApplyToRegexp() {
        return applyToRegexp;
    }

    public void setApplyToRegexp(boolean applyToRegexp) {
        this.applyToRegexp = applyToRegexp;
    }

    public String getReportEncoding() {
        return reportEncoding;
    }

    public void setReportEncoding(String reportEncoding) {
        this.reportEncoding = reportEncoding;
    }

    public String getReportFileName() {
        return reportFileName;
    }

    public void setReportFileName(String reportName) {
        reportFileName = reportName;
    }

    public String getReportVariableName() {
        return reportVariableName;
    }

    public void setReportVariableName(String reportVariableName) {
        this.reportVariableName = reportVariableName;
    }

    public String getTemplateEncoding() {
        return templateEncoding;
    }

    public void setTemplateEncoding(String templateEncoding) {
        this.templateEncoding = templateEncoding;
    }

    public String getTemplateFileName() {
        return templateFileName;
    }

    public void setTemplateFileName(String templateFileName) {
        this.templateFileName = templateFileName;
    }

    public void setReplacements(String[] contextSymbols, String[] contextReplacements) {
        this.contextReplacements = contextReplacements;
        this.contextSymbols = contextSymbols;
    }

    public boolean isXmlFormatSupport() {
        return xmlFormatSupport;
    }

    public void setXmlFormatSupport(boolean templateXmlFormatSupport) {
        xmlFormatSupport = templateXmlFormatSupport;
    }
}
