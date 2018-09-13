package ru.runa.wfe.task.logic;

import java.util.Set;

import ru.runa.wfe.audit.ProcessLog;
import ru.runa.wfe.audit.dao.IProcessLogDAO;
import ru.runa.wfe.commons.dao.IGenericDAO;
import ru.runa.wfe.definition.dao.IProcessDefinitionLoader;
import ru.runa.wfe.execution.ExecutionContext;
import ru.runa.wfe.execution.IExecutionContextFactory;
import ru.runa.wfe.presentation.BatchPresentation;
import ru.runa.wfe.presentation.hibernate.IBatchPresentationCompilerFactory;
import ru.runa.wfe.ss.SubstitutionCriteria;
import ru.runa.wfe.ss.logic.ISubstitutionLogic;
import ru.runa.wfe.task.Task;
import ru.runa.wfe.task.dto.IWfTaskFactory;
import ru.runa.wfe.user.Actor;
import ru.runa.wfe.user.EscalationGroup;
import ru.runa.wfe.user.Executor;
import ru.runa.wfe.user.dao.IExecutorDao;

public interface ITestCaseDataSet {

    public void mockRules(IWfTaskFactory tastFactory);

    public void mockRules(IExecutorDao executorDao);

    public void mockRules(ISubstitutionLogic substitutionLogic);

    public void mockRules(IProcessDefinitionLoader processDefinitionLoader);

    public void mockRules(IGenericDAO<Task> taskDao);

    public void mockRules(IExecutionContextFactory exeContextFactory);

    public void mockRules(IBatchPresentationCompilerFactory<?> batchCompilerFactory);

    public void mockRules(IProcessLogDAO<ProcessLog> logDAO);

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
