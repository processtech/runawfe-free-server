package ru.runa.wfe.datasource;

public enum JdbcDataSourceType {

    SqlServer("jdbc:sqlserver://<HOST>:<PORT=1433>;databaseName=<DBNAME>;"),
    Oracle("jdbc:oracle:thin:@<HOST>:<PORT=1521>:<DBNAME>"),
    PostgreSql("jdbc:postgresql://<HOST>:<PORT=5432>/<DBNAME>"),
    MySql("jdbc:mysql://<HOST>:<PORT=3306>/<DBNAME>"),
    Db2("jdbc:db2://<HOST>:<PORT=50000>/<DBNAME>"),
    Other("jdbc:other://<HOST>:<PORT>/<DBNAME>");

    private String urlSample;

    private JdbcDataSourceType(String urlSample) {
        this.urlSample = urlSample;
    }

    public String urlSample() {
        return urlSample;
    }

}
