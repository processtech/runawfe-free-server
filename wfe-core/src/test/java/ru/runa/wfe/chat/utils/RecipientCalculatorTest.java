package ru.runa.wfe.chat.utils;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import ru.runa.wfe.execution.logic.ExecutionLogic;
import ru.runa.wfe.user.Actor;
import ru.runa.wfe.user.User;
import ru.runa.wfe.user.dao.ExecutorDao;
import java.util.Set;

import static com.google.common.collect.Sets.newHashSet;
import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.notNull;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class RecipientCalculatorTest {

    @Mock
    private ExecutionLogic executionLogic;
    @Mock
    private ExecutorDao executorDao;
    @Mock
    private User user;
    private Set<Actor> actors;
    private RecipientCalculator calculator;
    private final Long processId = 1L;

    @Before
    public void init() {
        actors = newHashSet(createActor("first"), createActor("second"), createActor("third"));
        calculator = new RecipientCalculator(executorDao, executionLogic);
    }

    @Test
    public void whenMessageIsNotPrivate_thenReturnAllActors() {
        when(executionLogic.getAllExecutorsByProcessId(notNull(), eq(processId), eq(true)))
                .thenReturn(newHashSet(actors));

        Set<Actor> expected = actors;
        Set<Actor> actual = calculator.calculateRecipients(user, false, "Public", processId);
        assertEquals(expected, actual);
    }

    @Test
    public void whenMessageIsPrivate_thenReturnMentionedActors() {
        when(executorDao.getExecutor(eq("first"))).thenReturn(createActor("first"));
        when(executorDao.getExecutor(eq("second"))).thenReturn(createActor("second"));

        Set<Actor> expected = newHashSet(createActor("first"), createActor("second"));
        Set<Actor> actual = calculator.calculateRecipients(user, true, "@first @second Private", processId);
        assertEquals(expected, actual);
    }

    private static Actor createActor(String name) {
        return new Actor(name, "");
    }
}