package ru.runa.wfe.commons.bc;

import java.util.Calendar;
import java.util.Date;

/**
 * Business calendar is aware of working time.
 * 
 * @since 4.1.0 (refactored)
 */
public interface BusinessCalendar {

    /**
     * Applies specified duration to specified date.
     * 
     * @param date
     *            base date
     * @param durationString
     *            Syntax: [+|-]&lt;quantity&gt; [business] &lt;unit&gt;
     * @return calculated date
     */
    public Date apply(Date date, String durationString);

    /**
     * Applies specified duration to specified date.
     * 
     * @param date
     *            base date
     * @param duration
     *            business duration
     * @return calculated date
     */
    public Date apply(Date date, BusinessDuration duration);

    /**
     * Checks whether specified day is working or not (holiday)
     */
    public boolean isHoliday(Calendar calendar);
}
