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
