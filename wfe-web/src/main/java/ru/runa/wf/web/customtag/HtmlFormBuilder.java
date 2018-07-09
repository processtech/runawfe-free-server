package ru.runa.wf.web.customtag;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import ru.runa.wf.web.TaskFormBuilder;
import ru.runa.wfe.commons.ApplicationContextFactory;
import ru.runa.wfe.commons.BackCompatibilityClassNames;
import ru.runa.wfe.var.VariableProvider;

import com.google.common.base.Charsets;

public class HtmlFormBuilder extends TaskFormBuilder {
    private static final Log log = LogFactory.getLog(HtmlFormBuilder.class);

    private static final Pattern CUSTOM_TAG_PATTERN = Pattern.compile(
            "<customtag\\s+var\\s*=\\s*\"([^\"]+)\"\\s+delegation\\s*=\\s*\"([^\"]+)\"\\s*/>", Pattern.MULTILINE);

    @Override
    protected String buildForm(VariableProvider variableProvider) {
        StringBuilder builder = new StringBuilder(new String(interaction.getFormData(), Charsets.UTF_8));
        Matcher matcher = CUSTOM_TAG_PATTERN.matcher(builder);
        for (int position = 0; matcher.find(position);) {
            int start = matcher.start();
            int end = matcher.end();
            String varName = matcher.group(1);
            String className = matcher.group(2);
            String replacement;
            try {
                className = BackCompatibilityClassNames.getClassName(className);
                VarTag customTag = (VarTag) ApplicationContextFactory.createAutowiredBean(className);
                replacement = customTag.getHtml(user, varName, variableProvider.getValue(varName), pageContext, variableProvider);
            } catch (Exception e) {
                log.warn("Exception processing vartags", e);
                replacement = "<p class='error'>" + e.getMessage() + "</p>";
            }
            builder.replace(start, end, replacement);
            position = start + replacement.length();
        }
        return builder.toString();
    }

}
