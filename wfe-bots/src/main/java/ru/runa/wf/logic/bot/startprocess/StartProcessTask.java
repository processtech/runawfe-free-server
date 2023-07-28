package ru.runa.wf.logic.bot.startprocess;

public class StartProcessTask {
    private final String name;
    private final StartProcessVariableMapping[] startProcessVariableMappings;
    private String startedProcessIdValueName;

    /**
     * @param name
     *            process name
     * @param variables
     *            {@link StartProcessVariableMapping}
     */
    public StartProcessTask(String name, StartProcessVariableMapping[] variables) {
        this.name = name;
        this.startProcessVariableMappings = variables.clone();
    }

    public StartProcessTask(String name, StartProcessVariableMapping[] variables, String startedProcessIdName) {
        this(name, variables);
        startedProcessIdValueName = startedProcessIdName;
    }

    public String getName() {
        return name;
    }

    public int getVariablesCount() {
        return startProcessVariableMappings.length;
    }

    public StartProcessVariableMapping getStartProcessVariableMapping(int i) {
        return startProcessVariableMappings[i];
    }

    public String getStartedProcessIdValueName() {
        return startedProcessIdValueName;
    }
}
