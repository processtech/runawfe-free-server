package ru.runa.wfe.report;

import java.util.Date;

public interface ReportTimeFormatter {
    public String interval(Date startDate, Date endDate);

    public String dbSpecificInterval(Number interval);

    public String intervalInSeconds(Number interval);

    public String intervalInDays(Number interval);

    public String dateTime(Date date);

    public String date(Date date);

    public String time(Date date);
}
