package ru.runa.wfe.audit.dao;

import ru.runa.wfe.audit.BaseProcessLog;
import ru.runa.wfe.execution.CurrentProcess;
import ru.runa.wfe.execution.CurrentToken;

public interface ProcessLogAwareDao {
    void addLog(BaseProcessLog processLog, CurrentProcess process, CurrentToken token);
}
