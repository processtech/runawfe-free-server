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

public abstract class TestCaseDataSet implements ITestCaseDataSet {

    @Override
    public void mockRules(IWfTaskFactory tastFactory) {

    }

    @Override
    public void mockRules(IExecutorDao executorDao) {

    }

    @Override
    public void mockRules(ISubstitutionLogic substitutionLogic) {

    }

    @Override
    public void mockRules(IProcessDefinitionLoader processDefinitionLoader) {

    }

    @Override
    public void mockRules(IGenericDAO<Task> taskDao) {

    }

    @Override
    public void mockRules(IExecutionContextFactory exeContextFactory) {

    }

    @Override
    public void mockRules(IBatchPresentationCompilerFactory<?> batchCompilerFactory) {

    }

    @Override
    public void mockRules(IProcessLogDAO<ProcessLog> logDAO) {

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
