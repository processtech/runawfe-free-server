package ru.runa.wfe.commons.dbmigration.impl;

import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import ru.runa.wfe.commons.Utils;
import ru.runa.wfe.commons.dbmigration.DbMigration;
import ru.runa.wfe.commons.dbmigration.DbMigrationPostProcessor;
import ru.runa.wfe.definition.dao.ProcessDefinitionLoader;
import ru.runa.wfe.execution.ExecutionContext;
import ru.runa.wfe.execution.Token;
import ru.runa.wfe.execution.dao.TokenDao;
import ru.runa.wfe.lang.BaseMessageNode;
import ru.runa.wfe.lang.ProcessDefinition;

public class AddTokenMessageSelectorPatch extends DbMigration implements DbMigrationPostProcessor {
    @Autowired
    TokenDao tokenDao;
    @Autowired
    ProcessDefinitionLoader processDefinitionLoader;

    @Override
    protected void executeDDLBefore() {
        executeUpdates(
                getDDLCreateColumn("BPM_TOKEN", new VarcharColumnDef("MESSAGE_SELECTOR", 1024)),
                getDDLCreateIndex("BPM_TOKEN", "IX_MESSAGE_SELECTOR", "MESSAGE_SELECTOR")
        );
    }

    @Override
    public void postExecute() {
        List<Token> tokens = tokenDao.findByMessageSelectorIsNullAndExecutionStatusIsActive();
        log.info("Updating " + tokens.size() + " tokens message selector");
        for (Token token : tokens) {
            ProcessDefinition processDefinition = processDefinitionLoader.getDefinition(token.getProcess());
            BaseMessageNode messageNode = (BaseMessageNode) processDefinition.getNodeNotNull(token.getNodeId());
            ExecutionContext executionContext = new ExecutionContext(processDefinition, token.getProcess());
            String messageSelector = Utils.getReceiveMessageNodeSelector(executionContext.getVariableProvider(), messageNode);
            token.setMessageSelector(messageSelector);
        }
    }
}
