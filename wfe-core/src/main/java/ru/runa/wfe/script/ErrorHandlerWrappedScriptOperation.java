package ru.runa.wfe.script;

import ru.runa.wfe.script.common.ScriptExecutionContext;
import ru.runa.wfe.script.common.ScriptOperation;

public class ErrorHandlerWrappedScriptOperation extends ScriptOperation {

    final ScriptOperation innerOperation;

    final AdminScriptOperationErrorHandler errorHandler;

    public ErrorHandlerWrappedScriptOperation(ScriptOperation innerOperation, AdminScriptOperationErrorHandler errorHandler) {
        super();
        this.innerOperation = innerOperation;
        this.errorHandler = errorHandler;
    }

    @Override
    public void validate(ScriptExecutionContext context) {
        try {
            innerOperation.validate(context);
        } catch (Throwable e) {
            errorHandler.handle(e);
        }
    }

    @Override
    public void execute(ScriptExecutionContext context) {
        try {
            innerOperation.execute(context);
        } catch (Throwable e) {
            errorHandler.handle(e);
        }
    }
}
