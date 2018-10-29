package ru.runa.wf.logic.bot.textreport;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created on 2006
 * 
 */
public class SymbolsReplacer {

    /**
     * user defined replacements
     */
    private final String[] patterns;
    private final String[] replacements;

    /**
     * XML-specific replacements
     */
    private final String[] xmlPatterns = { "&", "'", "\"", "<", ">" };
    private final String[] xmlReplacements = { "&amp;", "&apos;", "&quot;", "&lt;", "&gt;" };

    /**
     * true to apply XML-specific replacements
     */
    private final boolean xmlFormat;

    /**
     * This is specific replacements for this task handler (TextReport)
     * If you want to declare string var}555 in variable definition you must write ${var\}555} in file
     */
    private final String[] predefinedPatterns = { "}" };
    private final String[] predefinedReplacements = { "\\}" };

    public SymbolsReplacer(String[] symbols, String[] replacements, boolean xmlFormat) {
        if (symbols.length != replacements.length) {
            throw new IllegalArgumentException("Arguments size 'symbols' and 'replacements' must be equals");
        }
        this.xmlFormat = xmlFormat;
        this.replacements = replacements;
        patterns = symbols;
    }

    public String replaceAll(String s) {
        String result = s;
        for (int i = 0; i < patterns.length; i++) {
            result = result.replaceAll(Pattern.quote(patterns[i]), Matcher.quoteReplacement(replacements[i]));
        }
        if (xmlFormat) {
            for (int i = 0; i < xmlPatterns.length; i++) {
                result = result.replaceAll(Pattern.quote(xmlPatterns[i]), Matcher.quoteReplacement(xmlReplacements[i]));
            }
        }
        return result;
    }

    public String replaceAllReverse(String s) {
        String result = s;
        for (int i = 0; i < patterns.length; i++) {
            result = result.replaceAll(Pattern.quote(replacements[i]), Matcher.quoteReplacement(patterns[i]));
        }
        if (xmlFormat) {
            for (int i = 0; i < xmlPatterns.length; i++) {
                result = result.replaceAll(Pattern.quote(xmlReplacements[i]), Matcher.quoteReplacement(xmlPatterns[i]));
            }
        }
        for (int i = 0; i < predefinedPatterns.length; i++) {
            result = result.replaceAll(Pattern.quote(predefinedReplacements[i]), Matcher.quoteReplacement(predefinedPatterns[i]));
        }
        return result;
    }
}
