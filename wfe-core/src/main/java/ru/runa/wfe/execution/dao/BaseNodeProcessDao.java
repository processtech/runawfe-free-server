package ru.runa.wfe.execution.dao;

import java.util.List;
import ru.runa.wfe.execution.NodeProcess;
import ru.runa.wfe.execution.Process;
import ru.runa.wfe.execution.Token;

public interface BaseNodeProcessDao<T extends Token, P extends Process<T>, NP extends NodeProcess<P, T>> {

    List<NP> getNodeProcesses(P process, T parentToken, String nodeId, Boolean finished);

    List<P> getSubprocesses(P process);

    List<P> getSubprocessesRecursive(P process);

    List<P> getSubprocesses(T token);

    List<P> getSubprocesses(P process, String nodeId, T parentToken, Boolean finished);
}
