package ru.runa.wfe.commons;

public class InstallationProperties {

    private static final PropertyResources RESOURCES = new PropertyResources("installation.properties");

    public static String getInstallationUuid() {
        return RESOURCES.getStringProperty("installation.uuid");
    }

    public static String getInstallationDateString() {
        return RESOURCES.getStringProperty("installation.date");
    }

    public static String getReferrerUrl() {
        return RESOURCES.getStringProperty("ReferrerUrl");
    }

    /**
     * System statistic report root url
     */
    public static String getStatisticReportRootUrl() {
        return RESOURCES.getStringProperty("statistic.report.url");
    }

    /**
     * System statistic report days wait after error
     */
    public static int getStatisticReportDaysWaitAfterError() {
        return RESOURCES.getIntegerProperty("statistic.report.days.wait.after.error", 7);
    }
}
