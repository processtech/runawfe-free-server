package ru.runa.wfe.task.logic;

import java.util.List;
import java.util.Set;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.testng.AbstractTestNGSpringContextTests;
import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import org.testng.collections.Sets;
import ru.runa.wfe.definition.DefinitionDoesNotExistException;
import ru.runa.wfe.definition.dao.ProcessDefinitionLoader;
import ru.runa.wfe.execution.CurrentProcess;
import ru.runa.wfe.lang.ProcessDefinition;
import ru.runa.wfe.presentation.BatchPresentation;
import ru.runa.wfe.task.Task;
import ru.runa.wfe.task.dto.WfTask;
import ru.runa.wfe.task.dto.WfTaskFactory;
import ru.runa.wfe.user.Actor;
import ru.runa.wfe.user.Executor;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@Test
@ContextConfiguration(locations = { "classpath:ru/runa/wfe/task/logic/test.context.xml" })
public class GetAcceptableTaskBoundConditionsTests extends AbstractTestNGSpringContextTests {

    private static final Log log = LogFactory.getLog(GetAcceptableTaskBoundConditionsTests.class);

    @Autowired
    ITaskListBuilderTestProvider taskListBuilder;

    @DataProvider(name = "testcases")
    public Object[][] getTestcases() {
        return new Object[][] { { "throw when processDefinitionLoader.getDefinition", new GetAcceptableTaskTestCaseDataSet() {

            @Override
            public GetAcceptableTaskTestCaseDataSet initialize() {
                setExceptionToGetDefinition(mock(DefinitionDoesNotExistException.class));
                setResult(null);
                return super.initialize();
            }

        }.initialize() } };
    }

    @Test(dataProvider = "testcases")
    void runTests(String testName, GetAcceptableTaskTestCaseDataSet testCase) {

        log.info(String.format("start test: %s", testName));

        TaskLogicMockFactory.getFactory().setContextRules(testCase);

        TaskInListState res = taskListBuilder.getAcceptableTask(testCase.getTask(), testCase.getActor(), testCase.getBatchPresentation(),
            testCase.getExecutorsToGetTasksByMembership());

        Assert.assertEquals(res.getTask().getId(), testCase.getResult().getId());

        TaskLogicMockFactory.getFactory().setContextRules(null);
    }

    public static class GetAcceptableTaskTestCaseDataSet extends TestCaseDataSet {
        protected Task task = mock(Task.class);
        protected Executor taskExecutor = mock(Executor.class);
        protected CurrentProcess process = mock(CurrentProcess.class);
        protected ProcessDefinition definition = mock(ProcessDefinition.class);
        protected Actor actor = mock(Actor.class);
        protected BatchPresentation batchPresentation = mock(BatchPresentation.class);
        protected Set<Executor> executorsToGetTasksByMembership = Sets.newHashSet();
        protected Throwable getDefExeption = null;
        protected WfTask result = mock(WfTask.class);

        public GetAcceptableTaskTestCaseDataSet() {
            when(task.getProcess()).thenReturn(process);
            when(task.getExecutor()).thenReturn(taskExecutor);
        }

        public GetAcceptableTaskTestCaseDataSet initialize() {
            return this;
        }

        @SuppressWarnings("unchecked")
        @Override
        public void mockRules(WfTaskFactory tastFactory) {
            when(tastFactory.create(any(Task.class), any(Actor.class), any(boolean.class), any(List.class))).thenReturn(result);
            when(tastFactory.create(any(Task.class), any(Actor.class), any(boolean.class), any(List.class), any(boolean.class))).thenReturn(result);
        }

        @Override
        public void mockRules(ProcessDefinitionLoader processDefinitionLoader) {
            if (getDefExeption != null) {
                when(processDefinitionLoader.getDefinition(process)).thenThrow(getDefExeption);
            } else {
                when(processDefinitionLoader.getDefinition(process)).thenReturn(definition);
            }
        }

        public void setExceptionToGetDefinition(Throwable e) {
            getDefExeption = e;
        }

        public void setResult(WfTask v) {
            result = v;
        }

        public WfTask getResult() {
            return result;
        }

        public void addTaskExecutorToMemberships() {
            executorsToGetTasksByMembership.add(taskExecutor);
        }

        @Override
        public Task getTask() {
            return task;
        }

        @Override
        public Actor getActor() {
            return actor;
        }

        @Override
        public BatchPresentation getBatchPresentation() {
            return batchPresentation;
        }

        @Override
        public Set<Executor> getExecutorsToGetTasksByMembership() {
            return executorsToGetTasksByMembership;
        }

    }

}
