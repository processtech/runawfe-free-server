package ru.runa.wfe.commons.bc;

import java.util.Date;

import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;

import ru.runa.wfe.commons.CalendarUtil;

public class DefaultBusinessCalendarTest extends Assert {
    private DefaultBusinessCalendar businessCalendar = new DefaultBusinessCalendar();

    @DataProvider
    public Object[][] getRegularDurations() {
        return new Object[][] { { "03.09.2013 09:30", "-1 minutes", "03.09.2013 09:29" }, { "03.09.2013 09:30", "-1 hours", "03.09.2013 08:30" },
                { "03.09.2013 09:30", "-1 months", "03.08.2013 09:30" }, { "03.09.2013 09:30", "-1 weeks", "27.08.2013 09:30" },
                { "03.09.2013 09:30", "-1 years", "03.09.2012 09:30" }, { "03.09.2013 09:30", "1 minutes", "03.09.2013 09:31" },
                { "03.09.2013 09:30", "1 hours", "03.09.2013 10:30" }, { "03.09.2013 09:30", "1 months", "03.10.2013 09:30" },
                { "03.09.2013 09:30", "1 weeks", "10.09.2013 09:30" }, { "03.09.2013 09:30", "1 years", "03.09.2014 09:30" } };
    }

    @DataProvider
    public Object[][] getBusinessDurations() {
        return new Object[][] {
                // 07.08.2012 is Tuesday, no holidays in this year
                { "07.08.2012 09:30", "-1 business years", "04.10.2011 09:30" },
                { "07.08.2012 09:30", "1 business years", "11.06.2013 09:30" },
                // 09.07.2013 is Tuesday, no holidays in this month
                { "09.07.2013 09:30", "-1 business weeks", "02.07.2013 09:30" },
                { "09.07.2013 09:30", "1 business weeks", "16.07.2013 09:30" },
                { "09.07.2013 09:30", "-1 business months", "10.06.2013 09:30" },
                { "09.07.2013 09:30", "1 business months", "07.08.2013 09:30" },
                // 03.09.2013 is Tuesday [holidays are 04.09.2013, 06.09.2013,
                // 17.09.2013]
                { "03.09.2013 09:30", "1 business weeks", "12.09.2013 09:30" },
                { "03.09.2013 09:30", "1 business months", "07.10.2013 09:30" },
                // test time shift from non-working time to first business day
                // part
                { "03.09.2013 07:30", "-1 business days", "30.08.2013 18:00" },
                { "03.09.2013 07:30", "1 business days", "05.09.2013 09:00" },
                { "03.09.2013 13:30", "-2 business days", "30.08.2013 13:00" },
                { "03.09.2013 13:30", "2 business days", "09.09.2013 14:00" },
                { "03.09.2013 18:30", "-1 business days", "02.09.2013 18:00" },
                { "03.09.2013 18:30", "1 business days", "09.09.2013 09:00" },
                // test time only
                { "03.09.2013 09:30", "-1 business minutes", "03.09.2013 09:29" }, { "03.09.2013 09:30", "1 business minutes", "03.09.2013 09:31" },
                { "03.09.2013 09:30", "-1 business hours", "02.09.2013 17:30" }, { "03.09.2013 09:30", "1 business hours", "03.09.2013 10:30" },
                { "03.09.2013 07:30", "-1 business minutes", "02.09.2013 17:59" }, { "03.09.2013 07:30", "1 business minutes", "03.09.2013 09:01" },
                { "03.09.2013 07:30", "-1 business hours", "02.09.2013 17:00" }, { "03.09.2013 07:30", "1 business hours", "03.09.2013 10:00" },
                { "02.09.2013 09:30", "-10 business hours", "29.08.2013 16:30" }, { "02.09.2013 09:30", "10 business hours", "03.09.2013 11:30" },
                { "03.09.2013 09:30", "-7 business hours", "02.09.2013 10:30" }, { "03.09.2013 09:30", "10 business hours", "05.09.2013 11:30" },
        //
        };
    }

    @Test(dataProvider = "getBusinessDurations")
    public void testBusinessTime(String baseDateString, String durationString, String expectedDateString) {
        Date baseDate = CalendarUtil.convertToDate(baseDateString, CalendarUtil.DATE_WITH_HOUR_MINUTES_FORMAT_STR);
        Date appliedDate = businessCalendar.apply(baseDate, durationString);
        assertEquals(CalendarUtil.formatDateTime(appliedDate), expectedDateString);
    }

    @Test(dataProvider = "getRegularDurations")
    public void testRegularTime(String baseDateString, String durationString, String expectedDateString) {
        Date baseDate = CalendarUtil.convertToDate(baseDateString, CalendarUtil.DATE_WITH_HOUR_MINUTES_FORMAT_STR);
        Date appliedDate = businessCalendar.apply(baseDate, durationString);
        assertEquals(CalendarUtil.formatDateTime(appliedDate), expectedDateString);
    }

    @Test
    public void testDebug() {
        String baseDateString = "03.09.2013 07:30";
        String durationString = "1 business days";
        Date baseDate = CalendarUtil.convertToDate(baseDateString, CalendarUtil.DATE_WITH_HOUR_MINUTES_FORMAT_STR);
        Date appliedDate = businessCalendar.apply(baseDate, durationString);
        System.out.println(baseDateString + " -> " + durationString + " -> " + CalendarUtil.formatDateTime(appliedDate));
    }
}
