package ru.runa.wfe.audit.dao;

import ru.runa.wfe.audit.IProcessLog;
import ru.runa.wfe.execution.Process;
import ru.runa.wfe.execution.Token;

public class DoNothingProcessLogAwareDao implements ProcessLogAwareDao {

    @Override
    public void addLog(IProcessLog processLog, Process process, Token token) {
    }
}
