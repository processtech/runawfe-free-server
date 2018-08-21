package ru.runa.wfe.audit.dao;

import ru.runa.wfe.audit.BaseProcessLog;
import ru.runa.wfe.execution.Process;
import ru.runa.wfe.execution.Token;

public class DoNothingProcessLogAwareDao implements ProcessLogAwareDao {

    @Override
    public void addLog(BaseProcessLog processLog, Process process, Token token) {
    }
}
