package ru.runa.wfe.task.logic;

import com.google.common.collect.Sets;
import java.util.Set;
import lombok.extern.apachecommons.CommonsLog;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.testng.AbstractTestNGSpringContextTests;
import org.testng.Assert;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import ru.runa.wfe.execution.ExecutionContext;
import ru.runa.wfe.ss.SubstitutionCriteria;
import ru.runa.wfe.task.Task;
import ru.runa.wfe.user.Actor;
import ru.runa.wfe.user.ExecutorDoesNotExistException;
import ru.runa.wfe.user.dao.ExecutorDao;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@Test
@ContextConfiguration(locations = { "classpath:ru/runa/wfe/task/logic/test.context.xml" })
@CommonsLog
public class CheckSubstitutionRulesBoundConditionsTests extends AbstractTestNGSpringContextTests {

    @Autowired
    ITaskListBuilderTestProvider taskListBuilder;

    @DataProvider(name = "testcases")
    public Object[][] getTestcases() {
        return new Object[][] { { "applies with one of DataAccessException testcase", TaskListBuilderImpl.SUBSTITUTION_APPLIES, new TestCaseDataSet() {
            @Override
            public void mockRules(ExecutorDao executorDao) {
                Actor actor = mock(Actor.class);
                when(actor.isActive()).thenReturn(true);
                when(executorDao.getActor(new Long(1))).thenReturn(actor);
                when(executorDao.getActor(new Long(2))).thenThrow(mock(org.springframework.dao.DataAccessException.class));
                when(executorDao.getActor(new Long(3))).thenReturn(actor);
                when(executorDao.getActor(new Long(4))).thenReturn(actor);
            }

            @Override
            public SubstitutionCriteria getCriteria() {
                SubstitutionCriteria criteria = mock(SubstitutionCriteria.class);
                when(criteria.isSatisfied(any(ExecutionContext.class), any(Task.class), any(Actor.class), any(Actor.class))).thenReturn(true);
                return criteria;
            }

            @Override
            public Set<Long> getIds() {
                return Sets.newHashSet(1L, 2L, 3L, 4L);
            }

        } }, { "applies with one of ExecutorDoesNotExistException testcase", TaskListBuilderImpl.SUBSTITUTION_APPLIES, new TestCaseDataSet() {
            @Override
            public void mockRules(ExecutorDao executorDao) {
                Actor actor = mock(Actor.class);
                when(actor.isActive()).thenReturn(true);
                when(executorDao.getActor(new Long(1))).thenReturn(actor);
                when(executorDao.getActor(new Long(2))).thenThrow(mock(ExecutorDoesNotExistException.class));
                when(executorDao.getActor(new Long(3))).thenReturn(actor);
                when(executorDao.getActor(new Long(4))).thenReturn(actor);
            }

            @Override
            public SubstitutionCriteria getCriteria() {
                SubstitutionCriteria criteria = mock(SubstitutionCriteria.class);
                when(criteria.isSatisfied(any(ExecutionContext.class), any(Task.class), any(Actor.class), any(Actor.class))).thenReturn(true);
                return criteria;
            }

            @Override
            public Set<Long> getIds() {
                return Sets.newHashSet(1L, 2L, 3L, 4L);
            }

        } }, { "applies testcase", TaskListBuilderImpl.SUBSTITUTION_APPLIES, new TestCaseDataSet() {

            Actor actor;

            @Override
            public void mockRules(ExecutorDao executorDao) {
                actor = mock(Actor.class);
                when(actor.isActive()).thenReturn(true);
                when(executorDao.getActor(new Long(1))).thenReturn(actor);
            }

            @Override
            public SubstitutionCriteria getCriteria() {
                SubstitutionCriteria criteria = mock(SubstitutionCriteria.class);
                when(criteria.isSatisfied(any(ExecutionContext.class), any(Task.class), any(Actor.class), eq(actor))).thenReturn(true);
                return criteria;
            }

            @Override
            public Set<Long> getIds() {
                return Sets.newHashSet(1L);
            }
        } }, { "can substitute testcase", TaskListBuilderImpl.CAN_I_SUBSTITUTE, new TestCaseDataSet() {

            Actor actor;

            @Override
            public void mockRules(ExecutorDao executorDao) {
                actor = mock(Actor.class);
                when(executorDao.getActor(new Long(1))).thenReturn(actor);
            }

            @Override
            public SubstitutionCriteria getCriteria() {
                SubstitutionCriteria criteria = mock(SubstitutionCriteria.class);
                when(criteria.isSatisfied(any(ExecutionContext.class), any(Task.class), any(Actor.class), eq(actor))).thenReturn(true);
                return criteria;
            }

            @Override
            public Actor getSubstitutorActor() {
                return actor;
            }

            @Override
            public Set<Long> getIds() {
                return Sets.newHashSet(1L);
            }

        } }, { "applies and can substitute testcase", TaskListBuilderImpl.SUBSTITUTION_APPLIES | TaskListBuilderImpl.CAN_I_SUBSTITUTE, new TestCaseDataSet() {

            Actor actor;

            @Override
            public void mockRules(ExecutorDao executorDao) {
                actor = mock(Actor.class);
                when(actor.isActive()).thenReturn(true);
                when(executorDao.getActor(new Long(1))).thenReturn(actor);
            }

            @Override
            public SubstitutionCriteria getCriteria() {
                SubstitutionCriteria criteria = mock(SubstitutionCriteria.class);
                when(criteria.isSatisfied(any(ExecutionContext.class), any(Task.class), any(Actor.class), eq(actor))).thenReturn(true);
                return criteria;
            }

            @Override
            public Actor getSubstitutorActor() {
                return actor;
            }

            @Override
            public Set<Long> getIds() {
                return Sets.newHashSet(1L);
            }
        } } };
    }

    @Test(dataProvider = "testcases")
    void runTests(String testName, int expected, TestCaseDataSet testCase) {

        log.info(String.format("start test: %s", testName));

        TaskLogicMockFactory.getFactory().setContextRules(testCase);

        int rules = taskListBuilder.checkSubstitutionRules(testCase.getCriteria(), testCase.getIds(), testCase.getExeContext(), testCase.getTask(),
                testCase.getAssignedActor(), testCase.getSubstitutorActor());

        Assert.assertEquals(rules, expected);

        TaskLogicMockFactory.getFactory().setContextRules(null);
    }
}
