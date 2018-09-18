package ru.runa.wfe.task.logic;

import java.util.Set;
import ru.runa.wfe.audit.dao.ProcessLogDao;
import ru.runa.wfe.definition.dao.ProcessDefinitionLoader;
import ru.runa.wfe.execution.ExecutionContext;
import ru.runa.wfe.execution.ExecutionContextFactory;
import ru.runa.wfe.presentation.BatchPresentation;
import ru.runa.wfe.presentation.hibernate.BatchPresentationCompilerFactory;
import ru.runa.wfe.ss.SubstitutionCriteria;
import ru.runa.wfe.ss.logic.SubstitutionLogic;
import ru.runa.wfe.task.Task;
import ru.runa.wfe.task.dao.TaskDao;
import ru.runa.wfe.task.dto.WfTaskFactory;
import ru.runa.wfe.user.Actor;
import ru.runa.wfe.user.EscalationGroup;
import ru.runa.wfe.user.Executor;
import ru.runa.wfe.user.dao.ExecutorDao;

public interface ITestCaseDataSet {

    public void mockRules(WfTaskFactory tastFactory);

    public void mockRules(ExecutorDao executorDao);

    public void mockRules(SubstitutionLogic substitutionLogic);

    public void mockRules(ProcessDefinitionLoader processDefinitionLoader);

    public void mockRules(TaskDao taskDao);

    public void mockRules(ExecutionContextFactory exeContextFactory);

    public void mockRules(BatchPresentationCompilerFactory<?> batchCompilerFactory);

    public void mockRules(ProcessLogDao logDAO);

    public SubstitutionCriteria getCriteria();

    public Set<Long> getIds();

    public ExecutionContext getExeContext();

    public Task getTask();

    public Actor getActor();

    public Actor getAssignedActor();

    public Actor getSubstitutorActor();

    public BatchPresentation getBatchPresentation();

    public Set<Executor> getExecutorsToGetTasksByMembership();

    public EscalationGroup getEscalationGroup();

}
