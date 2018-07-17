package ru.runa.wfe.datasource;

public enum JdbcDataSourceType {

    SqlServer("jdbc:sqlserver://<HOST>:<PORT=1433>;databaseName=<DBNAME>;"),
    Oracle("jdbc:oracle:thin:@<HOST>:<PORT=1521>:<DBNAME>"),
    PostgreSql("jdbc:postgresql://<HOST>:<PORT=5432>/<DBNAME>");

    private String urlSample;

    private JdbcDataSourceType(String urlSample) {
        this.urlSample = urlSample;
    }

    public String urlSample() {
        return urlSample;
    }

}
