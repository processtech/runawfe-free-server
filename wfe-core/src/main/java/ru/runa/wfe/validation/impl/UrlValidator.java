package ru.runa.wfe.validation.impl;

import java.net.MalformedURLException;
import java.net.URL;

import ru.runa.wfe.validation.FieldValidator;

import com.google.common.base.Strings;

public class UrlValidator extends FieldValidator {

    @Override
    public void validate() {
        String url = (String) getFieldValue();
        // if there is no value - don't do comparison
        // if a value is required, a required validator should be added to the
        // field
        if (Strings.isNullOrEmpty(url)) {
            return;
        }
        if (url.startsWith("https://")) {
            // URL doesn't understand the https protocol, hack it
            url = "http://" + url.substring(8);
        }
        try {
            new URL(url);
        } catch (MalformedURLException e) {
            addError();
        }
    }

}
