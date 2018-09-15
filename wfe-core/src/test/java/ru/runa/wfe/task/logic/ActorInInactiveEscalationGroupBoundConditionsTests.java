package ru.runa.wfe.task.logic;

import com.google.common.collect.Lists;
import java.util.List;
import java.util.Set;
import lombok.extern.apachecommons.CommonsLog;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.testng.AbstractTestNGSpringContextTests;
import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import org.testng.collections.Sets;
import ru.runa.wfe.audit.BaseProcessLog;
import ru.runa.wfe.audit.CurrentProcessLog;
import ru.runa.wfe.audit.CurrentTaskEscalationLog;
import ru.runa.wfe.audit.dao.ProcessLogDao;
import ru.runa.wfe.audit.presentation.ExecutorIdsValue;
import ru.runa.wfe.user.Actor;
import ru.runa.wfe.user.EscalationGroup;
import ru.runa.wfe.user.Executor;
import ru.runa.wfe.user.Group;
import ru.runa.wfe.user.dao.ExecutorDao;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.reset;
import static org.mockito.Mockito.when;

@Test
@ContextConfiguration(locations = { "classpath:ru/runa/wfe/task/logic/test.context.xml" })
@CommonsLog
public class ActorInInactiveEscalationGroupBoundConditionsTests extends AbstractTestNGSpringContextTests {

    @Autowired
    ITaskListBuilderTestProvider taskListBuilder;

    @DataProvider(name = "testcases")
    public Object[][] getTestcases() {
        return new Object[][] { { "test with DataAccessException", false, new ActorInInactiveEscalationGroupTestCaseDataSet() {

            @Override
            public ActorInInactiveEscalationGroupTestCaseDataSet initialize() {
                setExceptionToGetAllLogs(mock(DataAccessException.class));
                return super.initialize();
            }
        }.initialize() }, { "test with NullPointerException", true, new ActorInInactiveEscalationGroupTestCaseDataSet() {

            @Override
            public ActorInInactiveEscalationGroupTestCaseDataSet initialize() {
                addTaskEscalationLog("test1", "1", mock(NullPointerException.class), 1L);
                addTaskEscalationLog("test2", "1", null, 2L);
                setActorId(2L);
                when(actor.isActive()).thenReturn(false);
                groupActors.add(actor);
                return super.initialize();
            }
        }.initialize() }, { "test with null ids pointers", false, new ActorInInactiveEscalationGroupTestCaseDataSet() {

            @Override
            public ActorInInactiveEscalationGroupTestCaseDataSet initialize() {
                setActorId(1L);
                resetGroup();
                setGroupProcessId(null);
                setGroupNodeId(null);
                setOriginalexecutorToActor(2L);
                return super.initialize();
            }
        }.initialize() } };
    }

    @Test(dataProvider = "testcases")
    void runTests(String testName, boolean expected, ITestCaseDataSet testCase) {

        log.info(String.format("start test: %s", testName));

        TaskLogicMockFactory.getFactory().setContextRules(testCase);

        boolean res = taskListBuilder.isActorInInactiveEscalationGroup(testCase.getActor(), testCase.getEscalationGroup());

        Assert.assertEquals(res, expected);

        TaskLogicMockFactory.getFactory().setContextRules(null);
    }

    public static class ActorInInactiveEscalationGroupTestCaseDataSet extends TestCaseDataSet {

        protected Actor actor = mock(Actor.class);
        protected EscalationGroup group = mock(EscalationGroup.class);
        protected Executor originalExecutor = mock(Executor.class);
        protected Set<Actor> groupActors = Sets.newHashSet();
        protected List<BaseProcessLog> pLogs = Lists.newArrayList();
        protected Throwable getAllLogsException = null;

        public ActorInInactiveEscalationGroupTestCaseDataSet() {
            when(group.getOriginalExecutor()).thenReturn(originalExecutor);
            when(group.getProcessId()).thenReturn(1L);
            when(group.getNodeId()).thenReturn("1");
        }

        public ActorInInactiveEscalationGroupTestCaseDataSet initialize() {
            return this;
        }

        @Override
        public void mockRules(ExecutorDao executorDao) {
            when(executorDao.getGroupActors(any(EscalationGroup.class))).thenReturn(groupActors);
            for (Actor a : groupActors) {
                when(executorDao.getActor(a.getId())).thenReturn(a);
            }
        }

        @Override
        public void mockRules(ProcessLogDao processLogDao) {
            if (getAllLogsException != null) {
                when(processLogDao.getAll(group.getProcessId())).thenThrow(getAllLogsException);
            } else {
                Mockito.<List<? extends BaseProcessLog>>when(processLogDao.getAll(group.getProcessId())).thenReturn(pLogs);
            }
        }

        public void setExceptionToGetAllLogs(Throwable e) {
            getAllLogsException = e;
        }

        @Override
        public Actor getActor() {
            return actor;
        }

        @Override
        public EscalationGroup getEscalationGroup() {
            return group;

        }

        public void setActorId(Long id) {
            when(actor.getId()).thenReturn(id);
        }

        public void resetGroup() {
            reset(group);
        }

        public void setGroupProcessId(Long id) {
            when(group.getProcessId()).thenReturn(id);
        }

        public void setGroupNodeId(String id) {
            when(group.getNodeId()).thenReturn("0");
        }

        public void setOriginalexecutorToActor(Long id) {
            originalExecutor = mock(Actor.class);
            when(originalExecutor.getId()).thenReturn(id);
            when(group.getOriginalExecutor()).thenReturn(originalExecutor);
        }

        public void setOriginalexecutorToGroup() {
            originalExecutor = mock(Group.class);
            when(group.getOriginalExecutor()).thenReturn(originalExecutor);
        }

        public void addProcessLog() {
            pLogs.add(mock(CurrentProcessLog.class));
        }

        public void addTaskEscalationLog(String taskName, String nid, Throwable exc, Long... ids) {
            CurrentTaskEscalationLog mockLog = mock(CurrentTaskEscalationLog.class);
            when(mockLog.getNodeId()).thenReturn(nid);
            if (exc != null) {
                when(mockLog.getPatternArguments()).thenThrow(exc);
            } else {
                ExecutorIdsValue idsval = mock(ExecutorIdsValue.class);
                when(idsval.getIds()).thenReturn(Lists.newArrayList(ids));
                when(mockLog.getPatternArguments()).thenReturn(new Object[] { taskName, idsval });
            }
            pLogs.add(mockLog);
        }
    }

}
