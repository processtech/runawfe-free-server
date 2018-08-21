package ru.runa.wfe.audit.dao;

import ru.runa.wfe.audit.BaseProcessLog;
import ru.runa.wfe.execution.Process;
import ru.runa.wfe.execution.Token;

public interface ProcessLogAwareDao {
    void addLog(BaseProcessLog processLog, Process process, Token token);
}
