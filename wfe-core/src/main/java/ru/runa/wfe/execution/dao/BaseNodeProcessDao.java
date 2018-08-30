package ru.runa.wfe.execution.dao;

import com.google.common.collect.Lists;
import java.util.List;
import ru.runa.wfe.execution.NodeProcess;
import ru.runa.wfe.execution.Process;
import ru.runa.wfe.execution.Token;

public interface BaseNodeProcessDao<P extends Process, T extends Token<P, T>, NP extends NodeProcess<P, T>> {

    List<NP> getNodeProcesses(P process, T parentToken, String nodeId, Boolean finished);

    // TODO Optimizable: getNodeProcesses() can return Ps, not NPs; but where else is it used?
    default List<P> getSubprocesses(P process) {
        List<NP> nodeProcesses = getNodeProcesses(process, null, null, null);
        List<P> result = Lists.newArrayListWithExpectedSize(nodeProcesses.size());
        for (NP nodeProcess : nodeProcesses) {
            result.add(nodeProcess.getSubProcess());
        }
        return result;
    }

    // TODO Optimizable: introduce closure-table (ancestor_id, descendant_id, distance)?
    default void getSubprocessesRecursiveImpl(P process, List<P> result) {
        for (P subprocess : getSubprocesses(process)) {
            result.add(subprocess);
            getSubprocessesRecursiveImpl(subprocess, result);
        }
    }

    default List<P> getSubprocessesRecursive(P process) {
        List<P> result = Lists.newArrayList();
        getSubprocessesRecursiveImpl(process, result);
        return result;
    }

    default List<P> getSubprocesses(T token) {
        List<NP> nodeProcesses = getNodeProcesses(token.getProcess(), token, null, null);
        List<P> result = Lists.newArrayListWithExpectedSize(nodeProcesses.size());
        for (NP nodeProcess : nodeProcesses) {
            result.add(nodeProcess.getSubProcess());
        }
        return result;
    }

    default List<P> getSubprocesses(P process, String nodeId, T parentToken, Boolean finished) {
        List<NP> nodeProcesses = getNodeProcesses(process, parentToken, nodeId, finished);
        List<P> result = Lists.newArrayListWithExpectedSize(nodeProcesses.size());
        for (NP nodeProcess : nodeProcesses) {
            result.add(nodeProcess.getSubProcess());
        }
        return result;
    }
}
