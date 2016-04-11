package ru.runa.wfe.office.shared;

import java.util.Map;

import ru.runa.wf.logic.bot.BotFileDataProvider;
import ru.runa.wfe.definition.IFileDataProvider;
import ru.runa.wfe.execution.ExecutionContext;
import ru.runa.wfe.extension.ActionHandler;
import ru.runa.wfe.extension.handler.TaskHandlerBase;
import ru.runa.wfe.task.dto.WfTask;
import ru.runa.wfe.user.User;
import ru.runa.wfe.var.IVariableProvider;

public abstract class OfficeFilesSupplierHandler<T extends FilesSupplierConfig> extends TaskHandlerBase implements ActionHandler {

    protected T config;

    protected abstract FilesSupplierConfigParser<T> createParser();

    @Override
    public void setConfiguration(String configuration) throws Exception {
        FilesSupplierConfigParser<T> parser = createParser();
        this.config = parser.parse(configuration);
    }

    protected abstract Map<String, Object> executeAction(IVariableProvider variableProvider, IFileDataProvider fileDataProvider) throws Exception;

    @Override
    public void execute(ExecutionContext context) throws Exception {
        Map<String, Object> result = executeAction(context.getVariableProvider(), context.getProcessDefinition());
        if (result != null) {
            context.setVariableValues(result);
        }
    }

    @Override
    public Map<String, Object> handle(User user, IVariableProvider variableProvider, WfTask task) throws Exception {
        return executeAction(variableProvider, new BotFileDataProvider(embeddedFile));
    }
}
