package ru.runa.wfe.execution.dao;

import java.util.Collections;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import ru.runa.wfe.commons.dao.ArchiveAwareGenericDao;
import ru.runa.wfe.execution.ArchivedToken;
import ru.runa.wfe.execution.CurrentProcess;
import ru.runa.wfe.execution.CurrentToken;
import ru.runa.wfe.execution.Process;
import ru.runa.wfe.execution.Token;

@Component
public class TokenDao extends ArchiveAwareGenericDao<Token, CurrentToken, CurrentTokenDao, ArchivedToken, ArchivedTokenDao> {

    @Autowired
    TokenDao(CurrentTokenDao currentDao, ArchivedTokenDao archivedDao) {
        super(currentDao, archivedDao);
    }

    public List<? extends Token> findByProcessAndExecutionStatusIsNotEnded(Process process) {
        if (process.isArchived()) {
            // In archive, execution status is always ENDED.
            return Collections.emptyList();
        } else {
            return currentDao.findByProcessAndExecutionStatusIsNotEnded((CurrentProcess) process);
        }
    }

}
