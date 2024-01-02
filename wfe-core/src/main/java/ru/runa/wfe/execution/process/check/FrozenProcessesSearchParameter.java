package ru.runa.wfe.execution.process.check;

import lombok.Getter;

@Getter
public enum FrozenProcessesSearchParameter {

    SEARCH_FROZEN_IN_PARALLEL_GATEWAYS("searchFrozenInParallelGateways", null, "label.process_frozen_in_parallel_gateway", false),
    SEARCH_FROZEN_IN_UNEXPECTED_NODES("searchFrozenInUnexpectedNodes", null, "label.process_frozen_in_unexpected_node", true),
    SEARCH_FROZEN_IN_TASK_NODES("searchFrozenInTaskNodes", "searchFrozenInTaskNodesTimeThreshold", "label.process_frozen_in_task_node", false),
    SEARCH_FROZEN_IN_TIMER_NODES("searchFrozenInTimerNodes", null, "label.process_frozen_in_timer_node", true),
    SEARCH_FROZEN_BY_SUBPROCESSES("searchFrozenSubprocesses", null, "label.process_frozen_in_subprocess", true),
    SEARCH_FROZEN_BY_SIGNAL_TIME_EXCEEDED("searchFrozenBySignalTimeExceeded", "searchFrozenBySignalTimeExceededTimeLapse",
            "label.process_frozen_by_awaiting_a_signal", false),
    SEARCH_FROZEN_BY_SIGNAL("searchFrozenBySignal", null, "label.process_frozen_by_signal", false);

    private final String name;
    private final String valueName;
    private final String nameLabel;
    private final boolean defaultSearch;

    private FrozenProcessesSearchParameter(String name, String valueName, String nameLabel, boolean defaultSearch) {
        this.name = name;
        this.valueName = valueName;
        this.nameLabel = nameLabel;
        this.defaultSearch = defaultSearch;
    }
}
