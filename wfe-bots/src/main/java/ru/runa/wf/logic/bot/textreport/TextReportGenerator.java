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

import java.io.IOException;
import java.io.InputStream;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import ru.runa.wfe.commons.ClassLoaderUtil;
import ru.runa.wfe.var.VariableProvider;
import ru.runa.wfe.var.dto.WfVariable;

import com.google.common.io.ByteStreams;

/**
 * Created on 2006
 * 
 */
public class TextReportGenerator {
    private static final String VARIABLE_REGEXP = "\\$\\{(.*?[^\\\\])\\}";

    public static synchronized byte[] getReportContent(TextReportSettings settings, VariableProvider variableProvider) throws IOException {
        String templateFileName = settings.getTemplateFileName();
        String templateEncoding = settings.getTemplateEncoding();
        String encoding = settings.getReportEncoding();

        InputStream inputStream = ClassLoaderUtil.getAsStreamNotNull(templateFileName, TextReportGenerator.class);
        String content = new String(ByteStreams.toByteArray(inputStream), templateEncoding);
        String[] symbols = settings.getContextSymbols();
        String[] replacements = settings.getContextReplacements();
        SymbolsReplacer symbolsReplacer = new SymbolsReplacer(symbols, replacements, settings.isXmlFormatSupport());
        String currentRegexp;
        if (settings.isApplyToRegexp()) {
            currentRegexp = symbolsReplacer.replaceAll(VARIABLE_REGEXP);
        } else {
            currentRegexp = VARIABLE_REGEXP;
        }

        Pattern pattern = Pattern.compile(currentRegexp);
        Matcher matcher = pattern.matcher(content);

        StringBuffer buffer = new StringBuffer();
        while (matcher.find()) {
            String originalVarName = matcher.group(1);
            String variableName = symbolsReplacer.replaceAllReverse(originalVarName);
            WfVariable variable = variableProvider.getVariableNotNull(variableName);
            String formattedValue = variable.getStringValue();
            if (formattedValue != null) {
                String replacedFormattedValue = symbolsReplacer.replaceAll(formattedValue);
                matcher.appendReplacement(buffer, Matcher.quoteReplacement(replacedFormattedValue));
            }
        }
        matcher.appendTail(buffer);
        return buffer.toString().getBytes(encoding);
    }
}
