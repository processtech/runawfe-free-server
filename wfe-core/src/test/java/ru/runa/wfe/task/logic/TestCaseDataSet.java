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

public abstract class TestCaseDataSet implements ITestCaseDataSet {

    @Override
    public void mockRules(WfTaskFactory tastFactory) {
    }

    @Override
    public void mockRules(ExecutorDao executorDao) {
    }

    @Override
    public void mockRules(SubstitutionLogic substitutionLogic) {
    }

    @Override
    public void mockRules(ProcessDefinitionLoader processDefinitionLoader) {
    }

    @Override
    public void mockRules(TaskDao taskDao) {
    }

    @Override
    public void mockRules(ExecutionContextFactory executionContextFactory) {
    }

    @Override
    public void mockRules(BatchPresentationCompilerFactory<?> batchCompilerFactory) {
    }

    @Override
    public void mockRules(ProcessLogDao logDAO) {
    }

    @Override
    public SubstitutionCriteria getCriteria() {
        return null;
    }

    @Override
    public Set<Long> getIds() {
        return null;
    }

    @Override
    public ExecutionContext getExeContext() {
        return null;
    }

    @Override
    public Task getTask() {
        return null;
    }

    @Override
    public Actor getActor() {
        return null;
    }

    @Override
    public Actor getAssignedActor() {
        return null;
    }

    @Override
    public Actor getSubstitutorActor() {
        return null;
    }

    @Override
    public BatchPresentation getBatchPresentation() {
        return null;
    }

    @Override
    public Set<Executor> getExecutorsToGetTasksByMembership() {
        return null;
    }

    @Override
    public EscalationGroup getEscalationGroup() {
        return null;
    }
}
