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
