package ru.runa.wfe.graph.view;

import java.util.List;

import com.google.common.collect.Lists;

/**
 * Represents multiple instance graph element.
 */
public class MultiSubprocessNodeGraphElement extends SubprocessNodeGraphElement {
    private static final long serialVersionUID = 1L;

    /**
     * Identities of subprocesses, forked by this graph element. Contains
     * subprocess definition id if used in definition page.
     */
    private final List<Long> subprocessIds = Lists.newArrayList();
    private final List<Long> accessibleSubprocessIds = Lists.newArrayList();
    private final List<Long> completedSubprocessIds = Lists.newArrayList();

    /**
     * Add process id to forked subprocesses list.
     *
     * @param id
     *            Process identity.
     */
    public void addSubprocessInfo(Long id, boolean accessible, boolean completed) {
        subprocessIds.add(id);
        if (accessible) {
            accessibleSubprocessIds.add(id);
        }
        if (completed) {
            completedSubprocessIds.add(id);
        }
    }

    /**
     * Identities of subprocesses, forked by this graph element.
     */
    public List<Long> getSubprocessIds() {
        return subprocessIds;
    }

    public List<Long> getAccessibleSubprocessIds() {
        return accessibleSubprocessIds;
    }

    public List<Long> getCompletedSubprocessIds() {
        return completedSubprocessIds;
    }

}
