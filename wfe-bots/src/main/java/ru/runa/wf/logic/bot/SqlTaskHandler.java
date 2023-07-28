package ru.runa.wf.logic.bot;

import ru.runa.wfe.extension.handler.sql.AbstractSqlHandler;
import ru.runa.wfe.service.client.FileVariableProxy;
import ru.runa.wfe.var.file.FileVariable;

/**
 * @created on 01.04.2005 as DatabaseTaskHandler
 */
public class SqlTaskHandler extends AbstractSqlHandler {

    @Override
    protected FileVariable unproxyFileVariable(FileVariable fileVariable) {
        if (fileVariable instanceof FileVariableProxy) {
            return ((FileVariableProxy) fileVariable).getUnproxiedFileVariable();
        }
        return super.unproxyFileVariable(fileVariable);
    }

}
