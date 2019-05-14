package ru.runa.wfe.datasource;

public enum JdbcDataSourceType {

    SqlServer("jdbc:sqlserver://<HOST>:<PORT=1433>;databaseName=<DBNAME>;", "com.microsoft.sqlserver.jdbc.SQLServerDriver"),
    Oracle("jdbc:oracle:thin:@<HOST>:<PORT=1521>:<DBNAME>", "oracle.jdbc.driver.OracleDriver"),
    PostgreSql("jdbc:postgresql://<HOST>:<PORT=5432>/<DBNAME>", "org.postgresql.Driver");

    private String urlSample;
    private String driverClassName;

    private JdbcDataSourceType(String urlSample, String driverClassName) {
        this.urlSample = urlSample;
        this.driverClassName = driverClassName;
    }

    public String urlSample() {
        return urlSample;
    }

    public String driverClassName() {
        return driverClassName;
    }

}
