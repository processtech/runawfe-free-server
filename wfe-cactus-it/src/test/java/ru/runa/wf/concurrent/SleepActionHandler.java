package ru.runa.wf.concurrent;

import ru.runa.wfe.execution.ExecutionContext;
import ru.runa.wfe.extension.ActionHandler;

/**
 * Test action handler. Sleeps 2 seconds.
 */
public class SleepActionHandler implements ActionHandler {

    private static final long serialVersionUID = 1L;

    public void setConfiguration(String configurationName) {
    }

    @Override
    public void execute(ExecutionContext executionContext) throws Exception {
        Thread.sleep(2000);
    }
}
