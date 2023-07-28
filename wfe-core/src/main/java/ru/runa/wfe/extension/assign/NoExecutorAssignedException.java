package ru.runa.wfe.extension.assign;

/**
 * Indicates task assignment failure caused by no executors found
 */
public class NoExecutorAssignedException extends AssignmentException {
    private static final long serialVersionUID = 1L;

    public NoExecutorAssignedException() {
        super("error.assignment.executors.empty");
    }

    @Override
    protected String getResourceBaseName() {
        return "core.error";
    }
}
