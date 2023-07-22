package ru.runa.wfe.audit.dao;

import ru.runa.wfe.audit.ProcessLog;
import ru.runa.wfe.execution.Process;
import ru.runa.wfe.execution.Token;

public interface ProcessLogAwareDao {
    public void addLog(ProcessLog processLog, Process process, Token token);
}
