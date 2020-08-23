package ru.runa.wfe.office.storage.handler;

import com.google.common.base.Preconditions;
import ru.runa.wfe.InternalApplicationException;
import ru.runa.wfe.datasource.DataSource;
import ru.runa.wfe.datasource.ExcelDataSource;
import ru.runa.wfe.datasource.JdbcDataSource;
import ru.runa.wfe.office.storage.OracleStoreService;
import ru.runa.wfe.office.storage.PostgreSqlStoreService;
import ru.runa.wfe.office.storage.SqlServerStoreService;
import ru.runa.wfe.office.storage.StoreService;
import ru.runa.wfe.office.storage.StoreServiceImpl;
import ru.runa.wfe.var.VariableProvider;

/**
 * @author Alekseev Mikhail
 * @since #1394
 */
public class StoreServiceFactory {
    private StoreServiceFactory() {
    }

    public static StoreService create(DataSource dataSource, VariableProvider variableProvider) {
        Preconditions.checkNotNull(dataSource, "Can't obtain StoreService: dataSource must not be null");
        if (dataSource instanceof JdbcDataSource) {
            switch (((JdbcDataSource) dataSource).getDbType()) {
                case SqlServer:
                    return new SqlServerStoreService(variableProvider);
                case Oracle:
                    return new OracleStoreService(variableProvider);
                case PostgreSql:
                    return new PostgreSqlStoreService(variableProvider);
                default:
                    throw new InternalApplicationException("Database type " + ((JdbcDataSource) dataSource).getDbType().name() + " not supported.");
            }
        } else if (dataSource instanceof ExcelDataSource) {
            return new StoreServiceImpl(variableProvider);
        } else {
            throw new InternalApplicationException("Data source type " + dataSource.getClass().getSimpleName() + " not supported.");
        }
    }
}
