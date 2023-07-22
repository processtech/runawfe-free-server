/*
 * Copyright (c) 2002-2006 by OpenSymphony
 * All rights reserved.
 */
package ru.runa.wfe.validation.impl;

public class EmailValidator extends RegexFieldValidator {
    public static final String emailAddressPattern = "\\b(^[_A-Za-z0-9-]+(\\.[_A-Za-z0-9-]+)*@([A-Za-z0-9-])+(\\.[A-Za-z0-9-]+)*((\\.[A-Za-z0-9]{2,})|(\\.[A-Za-z0-9]{2,}\\.[A-Za-z0-9]{2,}))$)\\b";

    @Override
    protected String getExpression() {
        return emailAddressPattern;
    }

    @Override
    protected boolean isCaseSensitive() {
        return false;
    }

}
