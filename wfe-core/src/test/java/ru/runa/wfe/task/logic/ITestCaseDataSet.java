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

interface ITestCaseDataSet {

    void mockRules(WfTaskFactory tastFactory);

    void mockRules(ExecutorDao executorDao);

    void mockRules(SubstitutionLogic substitutionLogic);

    void mockRules(ProcessDefinitionLoader processDefinitionLoader);

    void mockRules(TaskDao taskDao);

    void mockRules(ExecutionContextFactory exeContextFactory);

    void mockRules(BatchPresentationCompilerFactory<?> batchCompilerFactory);

    void mockRules(ProcessLogDao logDAO);

    SubstitutionCriteria getCriteria();

    Set<Long> getIds();

    ExecutionContext getExeContext();

    Task getTask();

    Actor getActor();

    Actor getAssignedActor();

    Actor getSubstitutorActor();

    BatchPresentation getBatchPresentation();

    Set<Executor> getExecutorsToGetTasksByMembership();

    EscalationGroup getEscalationGroup();
}
