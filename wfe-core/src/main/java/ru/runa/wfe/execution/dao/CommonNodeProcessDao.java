package ru.runa.wfe.execution.dao;

import com.google.common.collect.Lists;
import java.util.List;
import lombok.RequiredArgsConstructor;
import ru.runa.wfe.execution.NodeProcess;
import ru.runa.wfe.execution.Process;
import ru.runa.wfe.execution.Token;

@RequiredArgsConstructor
class CommonNodeProcessDao<T extends Token, P extends Process<T>, NP extends NodeProcess<P, T>>  {

    private final BaseNodeProcessDao<T, P, NP> owner;

    // TODO Optimizable: getNodeProcesses() can return Ps, not NPs; but where else is it used?
    List<P> getSubprocesses(P process) {
        List<NP> nodeProcesses = owner.getNodeProcesses(process, null, null, null);
        List<P> result = Lists.newArrayListWithExpectedSize(nodeProcesses.size());
        for (NP nodeProcess : nodeProcesses) {
            result.add(nodeProcess.getSubProcess());
        }
        return result;
    }

    // TODO Optimizable: introduce closure-table (ancestor_id, descendant_id, distance)?
    private void getSubprocessesRecursiveImpl(P process, List<P> result) {
        for (P subprocess : getSubprocesses(process)) {
            result.add(subprocess);
            getSubprocessesRecursiveImpl(subprocess, result);
        }
    }

    List<P> getSubprocessesRecursive(P process) {
        List<P> result = Lists.newArrayList();
        getSubprocessesRecursiveImpl(process, result);
        return result;
    }

    List<P> getSubprocesses(T token) {
        @SuppressWarnings("unchecked")
        List<NP> nodeProcesses = owner.getNodeProcesses((P)token.getProcess(), token, null, null);
        List<P> result = Lists.newArrayListWithExpectedSize(nodeProcesses.size());
        for (NP nodeProcess : nodeProcesses) {
            result.add(nodeProcess.getSubProcess());
        }
        return result;
    }

    List<P> getSubprocesses(P process, String nodeId, T parentToken, Boolean finished) {
        List<NP> nodeProcesses = owner.getNodeProcesses(process, parentToken, nodeId, finished);
        List<P> result = Lists.newArrayListWithExpectedSize(nodeProcesses.size());
        for (NP nodeProcess : nodeProcesses) {
            result.add(nodeProcess.getSubProcess());
        }
        return result;
    }
}
