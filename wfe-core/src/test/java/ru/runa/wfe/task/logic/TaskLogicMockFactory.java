package ru.runa.wfe.task.logic;

import ru.runa.wfe.audit.dao.ProcessLogDao2;
import ru.runa.wfe.definition.dao.ProcessDefinitionLoader;
import ru.runa.wfe.execution.ExecutionContextFactory;
import ru.runa.wfe.presentation.hibernate.BatchPresentationCompilerFactory;
import ru.runa.wfe.ss.logic.SubstitutionLogic;
import ru.runa.wfe.task.dao.TaskDao;
import ru.runa.wfe.task.dto.WfTaskFactory;
import ru.runa.wfe.user.dao.ExecutorDao;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.reset;

public class TaskLogicMockFactory {

    private static TaskLogicMockFactory singleton;

    private final WfTaskFactory taskFactory = mock(WfTaskFactory.class);
    private final ExecutorDao executorDao = mock(ExecutorDao.class);
    private final SubstitutionLogic substitutionLogic = mock(SubstitutionLogic.class);
    private final ProcessDefinitionLoader processDefinitionLoader = mock(ProcessDefinitionLoader.class);
    @SuppressWarnings("unchecked")
    private final TaskDao taskDao = mock(TaskDao.class);
    private final ExecutionContextFactory exeContextFactory = mock(ExecutionContextFactory.class);
    private final BatchPresentationCompilerFactory<?> batchCompilerFactory = mock(BatchPresentationCompilerFactory.class);
    @SuppressWarnings("unchecked")
    private final ProcessLogDao2 logDAO = mock(ProcessLogDao2.class);

    public static final TaskLogicMockFactory getFactory() {
        return singleton;
    }

    public TaskLogicMockFactory() {
        singleton = this;
    }

    @SuppressWarnings("unchecked")
    public void setContextRules(ITestCaseDataSet dataset) {
        reset(taskFactory);
        reset(executorDao);
        reset(substitutionLogic);
        reset(processDefinitionLoader);
        reset(taskDao);
        reset(exeContextFactory);
        reset(batchCompilerFactory);
        reset(logDAO);

        if (dataset == null) {
            return;
        }

        dataset.mockRules(batchCompilerFactory);
        dataset.mockRules(exeContextFactory);
        dataset.mockRules(taskDao);
        dataset.mockRules(processDefinitionLoader);
        dataset.mockRules(logDAO);
        dataset.mockRules(substitutionLogic);
        dataset.mockRules(taskFactory);
        dataset.mockRules(executorDao);
    }

    public WfTaskFactory createMockWfTaskFactory() {
        return taskFactory;
    }

    public ExecutorDao createMockExecutorDAO() {
        return executorDao;
    }

    public SubstitutionLogic createMockSubstitutionLogic() {
        return substitutionLogic;
    }

    public ProcessDefinitionLoader createMockProcessDefinitionLoader() {
        return processDefinitionLoader;
    }

    public TaskDao createMockTaskDAO() {
        return taskDao;
    }

    public ExecutionContextFactory createMockExecutionContextFactory() {
        return exeContextFactory;
    }

    public BatchPresentationCompilerFactory<?> createMockBatchPresentationCompilerFactory() {
        return batchCompilerFactory;
    }

    public ProcessLogDao2 createMockProcessLogDAO() {
        return logDAO;
    }
}
