package ru.runa.wfe.audit.dao;

import ru.runa.wfe.audit.BaseProcessLog;
import ru.runa.wfe.execution.CurrentToken;
import ru.runa.wfe.execution.CurrentProcess;

public class DoNothingProcessLogAwareDao implements ProcessLogAwareDao {

    @Override
    public void addLog(BaseProcessLog processLog, CurrentProcess process, CurrentToken token) {
    }
}
