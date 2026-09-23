package ru.runa.wfe.office.storage.handler;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import ru.runa.wfe.datasource.DataSource;
import ru.runa.wfe.datasource.DataSourceStorage;
import ru.runa.wfe.definition.DefinitionVariableProvider;
import ru.runa.wfe.extension.StartConditionalHandler;
import ru.runa.wfe.lang.ConditionalEventModel;
import ru.runa.wfe.lang.ParsedProcessDefinition;
import ru.runa.wfe.office.storage.StoreService;
import ru.runa.wfe.office.storage.binding.DataBindings;
import ru.runa.wfe.office.storage.binding.ExecutionResult;
import ru.runa.wfe.office.storage.services.StoreHelper;
import ru.runa.wfe.var.UserTypeMap;
import ru.runa.wfe.var.VariableDefinition;
import ru.runa.wfe.var.VariableProvider;

public class StartInternalStorageHandler implements StartConditionalHandler {

    private DataBindings configuration;

    @Override
    public void setConfiguration(String configuration) throws Exception {
        ConditionalEventModel model = ConditionalEventModel.fromXml(configuration);
        if (model.getStorageUnsafe() == null) {
            throw new IllegalArgumentException("Storage configuration is required for StartInternalStorageHandler");
        }
        this.configuration = new StorageBindingsParser().parse(model.getStorageUnsafe().asXML());
    }

    @Override
    public List<Map<String, Object>> execute(ParsedProcessDefinition definition) throws Exception {

        StoreHelper storeHelper = createStoreHelper(definition);

        ExecutionResult result = storeHelper.findByComposite(configuration);

        if (!result.isNeedReturn()) {
            return Collections.emptyList();
        }

        @SuppressWarnings("unchecked")
        List<UserTypeMap> rows = (List<UserTypeMap>) result.getValue();
        String outputVariableName = configuration.getOutputFileVariableName();
        List<Map<String, Object>> variablesList = new ArrayList<>();

        for (UserTypeMap row : rows) {
            Map<String, Object> variables = new HashMap<>();
            variables.put(outputVariableName, row);
            variablesList.add(variables);
        }

        return variablesList;
    }

    private StoreHelper createStoreHelper(ParsedProcessDefinition definition) {
        VariableProvider variableProvider = new DefinitionVariableProvider(definition);
        DataSource dataSource = DataSourceStorage.parseDataSource(configuration.getInputFilePath(), variableProvider);
        StoreService storeService = StoreServiceFactory.create(dataSource, variableProvider);
        StoreHelper storeHelper = new StoreHelper(configuration, variableProvider, storeService);
        VariableDefinition variableDefinition = definition.getVariableNotNull(configuration.getOutputFileVariableName(), true);
        storeHelper.setVariableFormat(variableDefinition.getFormatNotNull());

        return storeHelper;
    }
}
