package ru.runa.wfe.office.storage.handler;

import com.google.common.collect.Iterables;
import java.util.Collection;
import ru.runa.wfe.datasource.DataSourceStorage;
import ru.runa.wfe.execution.ExecutionContext;
import ru.runa.wfe.extension.ConditionalHandler;
import ru.runa.wfe.lang.ConditionalEventModel;
import ru.runa.wfe.office.excel.OnSheetConstraints;
import ru.runa.wfe.office.storage.StoreService;
import ru.runa.wfe.office.storage.binding.DataBinding;
import ru.runa.wfe.office.storage.binding.DataBindings;
import ru.runa.wfe.office.storage.binding.ExecutionResult;
import ru.runa.wfe.office.storage.services.StoreHelper;
import ru.runa.wfe.var.UserType;
import ru.runa.wfe.var.VariableProvider;
import ru.runa.wfe.var.format.UserTypeFormat;

public class ConditionalInternalStorageHandler implements ConditionalHandler {

    private DataBindings configuration;

    @Override
    public void setConfiguration(String configuration) throws Exception {
        ConditionalEventModel model = ConditionalEventModel.fromXml(configuration);

        if (model.getStorageUnsafe() == null) {
            throw new IllegalArgumentException("Storage configuration is required for ConditionalInternalStorageHandler");
        }

        this.configuration = new StorageBindingsParser().parse(model.getStorageUnsafe().asXML());
    }

    @Override
    public boolean evaluate(ExecutionContext ctx) throws Exception {

        VariableProvider variableProvider = ctx.getVariableProvider();

        StoreService storeService = StoreServiceFactory.create(
                DataSourceStorage.parseDataSource(configuration.getInputFilePath(), variableProvider),
                variableProvider
        );

        StoreHelper storeHelper = new StoreHelper(configuration, variableProvider, storeService);
        DataBinding binding = Iterables.getOnlyElement(configuration.getBindings());
        binding.getConstraints().applyPlaceholders(variableProvider);

        UserType userType = variableProvider.getUserType(
                ((OnSheetConstraints) binding.getConstraints()).getSheetName()
        );

        storeHelper.setVariableFormat(new UserTypeFormat(userType));

        ExecutionResult result = storeHelper.findByFilter(
                binding,
                userType,
                configuration.getCondition()
        );

        Object value = result.getValue();

        return value instanceof Collection && !((Collection<?>) value).isEmpty();
    }
}
