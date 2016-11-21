/*
 * Copyright (c) 2002-2006 by OpenSymphony
 * All rights reserved.
 */
package ru.runa.wfe.validation.impl;

import java.util.Date;

public class DateRangeFieldValidator extends AbstractRangeValidator<Date> {

    @Override
    protected Date getMinComparatorValue() {
        return getParameter(Date.class, "min", null);
    }

    @Override
    protected Date getMaxComparatorValue() {
        return getParameter(Date.class, "max", null);
    }

}
