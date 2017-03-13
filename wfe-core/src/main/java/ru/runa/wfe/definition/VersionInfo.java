package ru.runa.wfe.definition;

import java.io.Serializable;
import java.util.Calendar;

import ru.runa.wfe.commons.CalendarUtil;

public class VersionInfo implements Serializable {
    private static final long serialVersionUID = 1L;
    private Calendar date = Calendar.getInstance();
    private String author = "";
    private String comment = "";

    public VersionInfo() {
    }

    public VersionInfo(String dateTimeAsString, String author, String comment) {
        this.date = CalendarUtil.convertToCalendar(dateTimeAsString, CalendarUtil.DATE_WITH_HOUR_MINUTES_FORMAT_STR);
        this.author = author;
        this.comment = comment;
    }

    public Calendar getDate() {
        return date;
    }

    public String getDateAsString() {
        return CalendarUtil.format(date, CalendarUtil.DATE_WITH_HOUR_MINUTES_FORMAT_STR);
    }

    public void setDate(Calendar date) {
        this.date = date;
    }

    public void setDate(String dateAsString) {
        this.date = CalendarUtil.convertToCalendar(dateAsString, CalendarUtil.DATE_WITHOUT_TIME_FORMAT_STR);
    }

    public void setDateTime(String dateTimeAsString) {
        this.date.setTime(CalendarUtil.convertToDate(dateTimeAsString, CalendarUtil.DATE_WITH_HOUR_MINUTES_FORMAT_STR));
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public String getComment() {
        return comment;
    }

    public void setComment(String comment) {
        this.comment = comment;
    }

    @Override
    public boolean equals(Object other) {
        if (other == null) {
            return false;
        }
        if (other == this) {
            return true;
        }
        if (other instanceof VersionInfo != true) {
            return false;
        }

        VersionInfo versionInfo = (VersionInfo) other;
        if (versionInfo.getDateAsString().equals(this.getDateAsString()) && versionInfo.getAuthor().equals(this.getAuthor())
                && versionInfo.getComment().equals(this.getComment())) {
            return true;
        }

        return false;

    }

    @Override
    public int hashCode() {
        return this.getDateAsString().concat(this.getAuthor()).concat(this.getComment()).hashCode();
    }
}