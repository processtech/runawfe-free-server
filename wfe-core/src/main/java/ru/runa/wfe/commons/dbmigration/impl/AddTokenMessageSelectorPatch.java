package ru.runa.wfe.commons.dbmigration.impl;

import java.sql.Types;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import ru.runa.wfe.commons.Utils;
import ru.runa.wfe.commons.dbmigration.DbMigration;
import ru.runa.wfe.commons.dbmigration.DbMigrationPostProcessor;
import ru.runa.wfe.definition.dao.ProcessDefinitionLoader;
import ru.runa.wfe.execution.CurrentToken;
import ru.runa.wfe.execution.ExecutionContext;
import ru.runa.wfe.execution.dao.CurrentTokenDao;
import ru.runa.wfe.lang.BaseMessageNode;
import ru.runa.wfe.lang.ParsedProcessDefinition;

public class AddTokenMessageSelectorPatch extends DbMigration implements DbMigrationPostProcessor {
    @Autowired
    CurrentTokenDao currentTokenDao;
    @Autowired
    ProcessDefinitionLoader processDefinitionLoader;

    @Override
    protected void executeDDLBefore() {
        executeUpdates(
                getDDLCreateColumn("BPM_TOKEN", new ColumnDef("MESSAGE_SELECTOR", dialect.getTypeName(Types.VARCHAR, 1024, 1024, 1024))),
                getDDLCreateIndex("BPM_TOKEN", "IX_MESSAGE_SELECTOR", "MESSAGE_SELECTOR")
        );
    }

    @Override
    public void postExecute() {
        List<CurrentToken> tokens = currentTokenDao.findByMessageSelectorIsNullAndExecutionStatusIsActive();
        log.info("Updating " + tokens.size() + " tokens message selector");
        for (CurrentToken token : tokens) {
            ParsedProcessDefinition parsedProcessDefinition = processDefinitionLoader.getDefinition(token.getProcess());
            BaseMessageNode messageNode = (BaseMessageNode) parsedProcessDefinition.getNodeNotNull(token.getNodeId());
            ExecutionContext executionContext = new ExecutionContext(parsedProcessDefinition, token.getProcess());
            String messageSelector = Utils.getReceiveMessageNodeSelector(executionContext.getVariableProvider(), messageNode);
            token.setMessageSelector(messageSelector);
        }
    }
}
