package ru.runa.wfe.commons.bc;

import java.util.Calendar;

import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

public class BusinessDurationParserTest extends Assert {

    @DataProvider
    public Object[][] getDurations() {
        return new Object[][] { { "1 business hours", new BusinessDuration(Calendar.MINUTE, 60, true) },
                { "1 hours", new BusinessDuration(Calendar.HOUR, 1, false) },
                { "15 business minutes", new BusinessDuration(Calendar.MINUTE, 15, true) },
                { "101 minutes", new BusinessDuration(Calendar.MINUTE, 101, false) },
                { "3 business days", new BusinessDuration(Calendar.DAY_OF_YEAR, 3, true) },
                { "14 days", new BusinessDuration(Calendar.DAY_OF_YEAR, 14, false) },
                { "11 seconds", new BusinessDuration(Calendar.SECOND, 11, false) },
                { "2 business weeks", new BusinessDuration(Calendar.DAY_OF_YEAR, 10, true) },
                { "1 business years", new BusinessDuration(Calendar.DAY_OF_YEAR, 220, true) },
                { "1 business months", new BusinessDuration(Calendar.DAY_OF_YEAR, 21, true) },
                { "1 months", new BusinessDuration(Calendar.MONTH, 1, false) },
                { "10 weeks", new BusinessDuration(Calendar.WEEK_OF_YEAR, 10, false) } };
    }

    @Test(dataProvider = "getDurations")
    public void parseDurations(String durationString, BusinessDuration expected) {
        BusinessDuration businessDuration = BusinessDurationParser.parse(durationString);
        assertEquals(businessDuration, expected);
    }

    @Test(expectedExceptions = IllegalArgumentException.class)
    public void parseBadDuration() {
        BusinessDurationParser.parse("1 business week");
    }
}
