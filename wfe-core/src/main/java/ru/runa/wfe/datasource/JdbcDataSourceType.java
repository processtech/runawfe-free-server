package ru.runa.wfe.datasource;

public enum JdbcDataSourceType {

    SqlServer("jdbc:sqlserver://<HOST>:<PORT=1433>;databaseName=<DBNAME>;", "com.microsoft.sqlserver.jdbc.SQLServerDriver", "select @@version"),
    Oracle("jdbc:oracle:thin:@<HOST>:<PORT=1521>:<DBNAME>", "oracle.jdbc.driver.OracleDriver", "select * from v$version"),
    PostgreSql("jdbc:postgresql://<HOST>:<PORT=5432>/<DBNAME>", "org.postgresql.Driver", "select version()");

    private String urlSample;
    private String driverClassName;
    private String serverVersionQuery;

    private JdbcDataSourceType(String urlSample, String driverClassName, String serverVersionQuery) {
        this.urlSample = urlSample;
        this.driverClassName = driverClassName;
        this.serverVersionQuery = serverVersionQuery;
    }

    public String urlSample() {
        return urlSample;
    }

    public String driverClassName() {
        return driverClassName;
    }

    public String serverVersionQuery() {
        return serverVersionQuery;
    }

}
