package ru.runa.wfe.datasource;

public enum JdbcDataSourceType {

    MySql("jdbc:mysql://<HOST>:<PORT=3306>"),
    PostgreSql("jdbc:postgresql://<HOST>:<PORT=5432>"),
    Oracle("jdbc:oracle:thin:@<HOST>:<PORT=1521>"),
    MsSqlServer("jdbc:jtds:sqlserver://<HOST>:<PORT=1433>"),
    IbmDb2("jdbc:db2://<HOST>:<PORT=50000>"),
    Other("jdbc:other://<HOST>:<PORT>");

    private String urlSample;

    private JdbcDataSourceType(String urlSample) {
        this.urlSample = urlSample;
    }

    public String urlSample() {
        return urlSample;
    }

}
