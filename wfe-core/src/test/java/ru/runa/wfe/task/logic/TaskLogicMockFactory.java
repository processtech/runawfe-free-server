package ru.runa.wfe.task.logic;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.reset;
import ru.runa.wfe.audit.ProcessLog;
import ru.runa.wfe.audit.dao.IProcessLogDAO;
import ru.runa.wfe.commons.dao.IGenericDAO;
import ru.runa.wfe.definition.dao.IProcessDefinitionLoader;
import ru.runa.wfe.execution.IExecutionContextFactory;
import ru.runa.wfe.presentation.hibernate.IBatchPresentationCompilerFactory;
import ru.runa.wfe.ss.logic.ISubstitutionLogic;
import ru.runa.wfe.task.Task;
import ru.runa.wfe.task.dto.IWfTaskFactory;
import ru.runa.wfe.user.dao.IExecutorDAO;

public class TaskLogicMockFactory {

    private static TaskLogicMockFactory singleton;

    private final IWfTaskFactory taskFactory = mock(IWfTaskFactory.class);
    private final IExecutorDAO executorDAO = mock(IExecutorDAO.class);
    private final ISubstitutionLogic substitutionLogic = mock(ISubstitutionLogic.class);
    private final IProcessDefinitionLoader processDefinitionLoader = mock(IProcessDefinitionLoader.class);
    @SuppressWarnings("unchecked")
    private final IGenericDAO<Task> taskDAO = mock(IGenericDAO.class);
    private final IExecutionContextFactory exeContextFactory = mock(IExecutionContextFactory.class);
    private final IBatchPresentationCompilerFactory<?> batchCompilerFactory = mock(IBatchPresentationCompilerFactory.class);
    @SuppressWarnings("unchecked")
    private final IProcessLogDAO<ProcessLog> logDAO = mock(IProcessLogDAO.class);

    public static final TaskLogicMockFactory getFactory() {
        return singleton;
    }

    public TaskLogicMockFactory() {
        singleton = this;
    }

    @SuppressWarnings("unchecked")
    public void setContextRules(ITestCaseDataSet dataset) {
        reset(taskFactory);
        reset(executorDAO);
        reset(substitutionLogic);
        reset(processDefinitionLoader);
        reset(taskDAO);
        reset(exeContextFactory);
        reset(batchCompilerFactory);
        reset(logDAO);

        if (dataset == null) {
            return;
        }

        dataset.mockRules(batchCompilerFactory);
        dataset.mockRules(exeContextFactory);
        dataset.mockRules(taskDAO);
        dataset.mockRules(processDefinitionLoader);
        dataset.mockRules(logDAO);
        dataset.mockRules(substitutionLogic);
        dataset.mockRules(taskFactory);
        dataset.mockRules(executorDAO);
    }

    public IWfTaskFactory createMockWfTaskFactory() {
        return taskFactory;
    }

    public IExecutorDAO createMockExecutorDAO() {
        return executorDAO;
    }

    public ISubstitutionLogic createMockSubstitutionLogic() {
        return substitutionLogic;
    }

    public IProcessDefinitionLoader createMockProcessDefinitionLoader() {
        return processDefinitionLoader;
    }

    public IGenericDAO<Task> createMockGenericDAO() {
        return taskDAO;
    }

    public IExecutionContextFactory createMockExecutionContextFactory() {
        return exeContextFactory;
    }

    public IBatchPresentationCompilerFactory<?> createMockBatchPresentationCompilerFactory() {
        return batchCompilerFactory;
    }

    public IProcessLogDAO<ProcessLog> createMockProcessLogDAO() {
        return logDAO;
    }
}
