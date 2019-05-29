package ru.runa.wfe.office.storage.handler;

import java.util.HashMap;
import java.util.Map;
import lombok.val;
import ru.runa.wfe.InternalApplicationException;
import ru.runa.wfe.datasource.DataSource;
import ru.runa.wfe.datasource.DataSourceStorage;
import ru.runa.wfe.datasource.DataSourceStuff;
import ru.runa.wfe.datasource.ExcelDataSource;
import ru.runa.wfe.datasource.JdbcDataSource;
import ru.runa.wfe.definition.FileDataProvider;
import ru.runa.wfe.office.shared.FilesSupplierConfigParser;
import ru.runa.wfe.office.shared.OfficeFilesSupplierHandler;
import ru.runa.wfe.office.storage.OracleStoreService;
import ru.runa.wfe.office.storage.PostgreSqlStoreService;
import ru.runa.wfe.office.storage.SqlServerStoreService;
import ru.runa.wfe.office.storage.StoreHelper;
import ru.runa.wfe.office.storage.StoreService;
import ru.runa.wfe.office.storage.StoreServiceImpl;
import ru.runa.wfe.office.storage.binding.DataBinding;
import ru.runa.wfe.office.storage.binding.DataBindings;
import ru.runa.wfe.office.storage.binding.ExecutionResult;
import ru.runa.wfe.office.storage.services.StoreHelperImpl;
import ru.runa.wfe.var.VariableProvider;
import ru.runa.wfe.var.dto.WfVariable;

public class ExternalStorageHandler extends OfficeFilesSupplierHandler<DataBindings> {

    @Override
    protected FilesSupplierConfigParser<DataBindings> createParser() {
        return new StorageBindingsParser();
    }

    @Override
    protected Map<String, Object> executeAction(VariableProvider variableProvider, FileDataProvider fileDataProvider) throws Exception {
        val result = new HashMap<String, Object>();
        StoreService storeService;
        String dsName = config.getInputFilePath();
        if (dsName.startsWith(DataSourceStuff.PATH_PREFIX_DATA_SOURCE) || dsName.startsWith(DataSourceStuff.PATH_PREFIX_DATA_SOURCE_VARIABLE)) {
            if (dsName.startsWith(DataSourceStuff.PATH_PREFIX_DATA_SOURCE)) {
                dsName = dsName.substring(dsName.indexOf(':') + 1);
            } else {
                dsName = (String) variableProvider.getValue(dsName.substring(dsName.indexOf(':') + 1));
            }
            DataSource ds = DataSourceStorage.getDataSource(dsName);
            if (ds instanceof JdbcDataSource) {
                switch (((JdbcDataSource) ds).getDbType()) {
                case SqlServer:
                    storeService = new SqlServerStoreService(variableProvider);
                    break;
                case Oracle:
                    storeService = new OracleStoreService(variableProvider);
                    break;
                case PostgreSql:
                    storeService = new PostgreSqlStoreService(variableProvider);
                    break;
                default:
                    throw new InternalApplicationException("Database type " + ((JdbcDataSource) ds).getDbType().name() + " not supported.");
                }
            } else if (ds instanceof ExcelDataSource) {
                storeService = new StoreServiceImpl(variableProvider);
            } else {
                throw new InternalApplicationException("Data source type " + ds.getClass().getSimpleName() + " not supported.");
            }
        } else {
            storeService = new StoreServiceImpl(variableProvider);
        }
        StoreHelper storeHelper = new StoreHelperImpl(config, variableProvider, storeService);
        for (DataBinding binding : config.getBindings()) {
            WfVariable variable = variableProvider.getVariableNotNull(binding.getVariableName());
            binding.getConstraints().applyPlaceholders(variableProvider);
            storeHelper.setVariableFormat(variable.getDefinition().getFormatNotNull());
            ExecutionResult executionResult = storeHelper.execute(binding, variable);
            if (executionResult.isNeedReturn()) {
                result.put(binding.getVariableName(), executionResult.getValue());
            }
        }
        return result;
    }
}
