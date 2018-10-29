package ru.runa.wfe.commons.web;

import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import com.google.common.base.Charsets;
import com.google.common.base.Preconditions;
import com.google.common.base.Throwables;
import com.google.common.collect.Maps;
import com.google.common.io.ByteStreams;

public class WebUtils {

    public static String getScript(String javascript) {
        return "<script type=\"text/javascript\">" + javascript + "</script>";
    }

    public static String getFormComponentScript(String javascript, Map<String, String> substitutions) {
        if (substitutions == null) {
            substitutions = Maps.newHashMap();
        }
        for (Map.Entry<String, String> entry : substitutions.entrySet()) {
            javascript = javascript.replaceAll(Pattern.quote(entry.getKey()), Matcher.quoteReplacement(entry.getValue()));
        }
        return getScript(javascript);
    }

    public static String getFormComponentScript(InputStream javascriptStream, Map<String, String> substitutions) {
        Preconditions.checkNotNull(javascriptStream);
        try {
            String javascript = new String(ByteStreams.toByteArray(javascriptStream), Charsets.UTF_8);
            return getFormComponentScript(javascript, substitutions);
        } catch (IOException e) {
            throw Throwables.propagate(e);
        }
    }

    public static String getExternalScript(WebHelper webHelper, String src) {
        if (webHelper == null || webHelper.getRequest().getAttribute(src) != null) {
            return "";
        }
        webHelper.getRequest().setAttribute(src, Boolean.TRUE);
        String url = webHelper.getUrl(src);
        return "<script type=\"text/javascript\" src=\"" + url + "\"></script>";
    }

}
