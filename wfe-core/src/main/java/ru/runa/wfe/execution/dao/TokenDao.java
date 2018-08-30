package ru.runa.wfe.execution.dao;

import java.util.Collections;
import java.util.List;
import lombok.val;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import ru.runa.wfe.commons.dao.GenericDao2;
import ru.runa.wfe.execution.ArchivedToken;
import ru.runa.wfe.execution.CurrentProcess;
import ru.runa.wfe.execution.CurrentToken;
import ru.runa.wfe.execution.ExecutionStatus;
import ru.runa.wfe.execution.Process;
import ru.runa.wfe.execution.QCurrentToken;
import ru.runa.wfe.execution.Token;

@Component
public class TokenDao extends GenericDao2<Token, CurrentToken, CurrentTokenDao, ArchivedToken, ArchivedTokenDao> {

    @Autowired
    TokenDao(CurrentTokenDao dao1, ArchivedTokenDao dao2) {
        super(dao1, dao2);
    }

    public List<? extends Token> findByProcessAndExecutionStatusIsNotEnded(Process process) {
        if (process.isArchive()) {
            // In archive, execution status is always ENDED.
            return Collections.emptyList();
        } else {
            return dao1.findByProcessAndExecutionStatusIsNotEnded((CurrentProcess) process);
        }
    }

    public List<CurrentToken> findByProcessAndExecutionStatus(CurrentProcess process, ExecutionStatus status) {
        val t = QCurrentToken.currentToken;
        return queryFactory.selectFrom(t).where(t.process.eq(process).and(t.executionStatus.eq(status))).fetch();
    }
}
