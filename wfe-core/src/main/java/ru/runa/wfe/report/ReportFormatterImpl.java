package ru.runa.wfe.report;

public class ReportFormatterImpl implements ReportFormatter {

    @Override
    public ReportTimeFormatter timeFormat() {
        return new ReportTimeFormatterImpl(false);
    }

    @Override
    public ReportTimeFormatter timeMultilineFormat() {
        return new ReportTimeFormatterImpl(true);
    }
}
