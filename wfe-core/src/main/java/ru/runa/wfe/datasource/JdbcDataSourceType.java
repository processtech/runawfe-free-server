package ru.runa.wfe.datasource;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum JdbcDataSourceType {

    SqlServer("com.microsoft.sqlserver.jdbc.SQLServerDriver", ";databaseName="),
    Oracle("oracle.jdbc.driver.OracleDriver", ":"),
    PostgreSql("org.postgresql.Driver", "/");

    private final String driverClassName;
    private final String databaseDelimiter;

    // TODO DS needs this method yet
    public String urlSample() {
        return name();
    }

}
