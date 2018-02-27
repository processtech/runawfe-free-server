package ru.runa.wfe.datasource;

import java.util.HashMap;
import java.util.Map;

import com.google.common.base.Throwables;

class DataSourceCreator {

    private static final Map<DataSourceType, Class<? extends DataSource>> DATA_SOURCE_TYPES = new HashMap<>();
    static {
        DATA_SOURCE_TYPES.put(DataSourceType.Excel, ExcelDataSource.class);
        DATA_SOURCE_TYPES.put(DataSourceType.JDBC, JdbcDataSource.class);
        DATA_SOURCE_TYPES.put(DataSourceType.WildFly, WildFlyDataSource.class);
        DATA_SOURCE_TYPES.put(DataSourceType.JBoss, JbossDataSource.class);
    }

    static DataSource create(DataSourceType type) {
        try {
            return DATA_SOURCE_TYPES.get(type).newInstance();
        } catch (Exception e) {
            throw Throwables.propagate(e);
        }
    }

}
