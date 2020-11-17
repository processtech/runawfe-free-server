package ru.runa.wfe.report;

import java.text.DateFormat;
import java.util.Date;

public class ReportTimeFormatterImpl implements ReportTimeFormatter {

    private final boolean multiline;

    public ReportTimeFormatterImpl(boolean multiline) {
        super();
        this.multiline = multiline;
    }

    @Override
    public String dateTime(Date date) {
        return date(date) + (multiline ? "\n" : " ") + time(date);
    }

    @Override
    public String date(Date date) {
        return DateFormat.getDateInstance(DateFormat.MEDIUM).format(date);
    }

    @Override
    public String time(Date date) {
        return DateFormat.getTimeInstance(DateFormat.MEDIUM).format(date);
    }

    @Override
    public String intervalInSeconds(Number interval) {
        long intervalInSeconds = Math.abs(interval.longValue());
        int seconds = (int) (intervalInSeconds % 60);
        int minutes = (int) ((intervalInSeconds / 60) % 60);
        int hours = (int) ((intervalInSeconds / (60 * 60)) % 24);
        int days = (int) ((intervalInSeconds / (60 * 60 * 24)));
        String sign = "";
        if(interval.longValue() < 0) {
            sign = "- ";
        }        
        return String.format("%s%d days%s%02d:%02d:%02d", sign, days, multiline ? "\n" : " ", hours, minutes, seconds);
    }

    @Override
    public String intervalInDays(Number interval) {
        return intervalInSeconds(interval.doubleValue() * 24 * 60 * 60);
    }

    @Override
    public String interval(Date startDate, Date endDate) {
        if (startDate == null || endDate == null) {
            return "";
        }
        return intervalInSeconds((endDate.getTime() - startDate.getTime()) / 1000);
    }

    @Override
    public String dbSpecificInterval(Number interval) {
        return null;
    }
}
