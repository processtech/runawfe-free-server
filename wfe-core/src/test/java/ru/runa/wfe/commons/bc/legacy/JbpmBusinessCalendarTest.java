package ru.runa.wfe.commons.bc.legacy;

import java.util.Date;

import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import ru.runa.wfe.commons.CalendarUtil;

public class JbpmBusinessCalendarTest {
    private JbpmBusinessCalendar bc = new JbpmBusinessCalendar();

    @DataProvider
    public Object[][] getDurations() {
        return new Object[][] { { "-1 business hours" }, { "-1 business months" }, { "-1 business weeks" }, { "-1 business years" },
                { "-1 business minutes" }, { "1 business hours" }, { "1 business months" }, { "1 business weeks" }, { "1 business years" },
                { "1 business minutes" } };
    }

    @Test(dataProvider = "getDurations")
    public void testBusinessTime(String durationString) {
        Date date = new Date();
        Date date2 = bc.apply(date, durationString);
        System.out.println(durationString + ": " + CalendarUtil.formatDateTime(date) + " -> " + CalendarUtil.formatDateTime(date2));
    }

    @Test
    public void testBusinessTime2() {
        Date date = new Date();
        Date date2 = bc.apply(date, "1 business months");
        System.out.println(CalendarUtil.formatDateTime(date) + " -> " + CalendarUtil.formatDateTime(date2));
    }
}
