package ru.runa.common.web;

import javax.servlet.jsp.PageContext;

/**
 * Created 14.05.2005
 * 
 * @author Gordienko_m
 * @author Vitaliy S aka Yilativs
 */
public class Messages {

    private Messages() {
    }

    public static String getMessage(String key, PageContext pageContext) {
        String value = Commons.getMessage(key, pageContext);
        if (value == null) {
            value = '!' + key + '!';
        }
        return value;
    }
}
