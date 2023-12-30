package ru.runa.wfe.execution.process.check;

import lombok.Getter;

@Getter
public enum FrozenProcessFilter {

    PROCESS_NAME("processName", "frozen_processes.process_name"),
    PROCESS_VERSION("processVersion", "frozen_processes.process_version"),
    NODE_ENTER_DATE("nodeEnterDate", "frozen_processes.node_enter_date"),
    NODE_TYPE("nodeType", "frozen_processes.node_type");

    private final String name;
    private final String labelName;

    private FrozenProcessFilter(String name, String labelName) {
        this.name = name;
        this.labelName = labelName;
    }
}
