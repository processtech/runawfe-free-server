package ru.runa.wfe.chat.utils;

import java.util.Set;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import ru.runa.wfe.execution.logic.ExecutionLogic;
import ru.runa.wfe.user.Actor;
import ru.runa.wfe.user.ExecutorDoesNotExistException;
import ru.runa.wfe.user.Group;
import ru.runa.wfe.user.User;
import ru.runa.wfe.user.dao.ExecutorDao;

import static com.google.common.collect.Sets.newHashSet;
import static org.junit.Assert.assertEquals;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.ArgumentMatchers.notNull;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class RecipientCalculatorTest {
    private static final Long PROCESS_ID = 1L;

    @Mock
    private ExecutionLogic executionLogic;
    @Mock
    private ExecutorDao executorDao;
    @Mock
    private User user;
    private Set<Actor> actors;
    @InjectMocks
    private RecipientCalculator calculator;

    @Before
    public void init() {
        actors = newHashSet(actor("first"), actor("second"), actor("third"));
    }

    @Test
    public void whenMessageIsNotPrivate_thenReturnAllActors() {
        when(executionLogic.getAllExecutorsByProcessId(notNull(), eq(PROCESS_ID), eq(true)))
                .thenReturn(newHashSet(actors));

        Set<Actor> expected = actors;
        Set<Actor> actual = calculator.calculateRecipients(user, false, "Public", PROCESS_ID);
        assertEquals(expected, actual);
    }

    @Test
    public void whenMessageIsPrivate_thenReturnMentionedActors() {
        when(user.getActor()).thenReturn(actor("first"));
        when(executorDao.getExecutor("first")).thenReturn(actor("first"));
        when(executorDao.getExecutor("second")).thenReturn(actor("second"));
        when(executionLogic.getAllExecutorsByProcessId(notNull(), notNull(), eq(true)))
                .thenReturn(newHashSet(actor("first"), actor("second"), actor("third"), actor("fourth")));

        Set<Actor> expected = newHashSet(actor("first"), actor("second"));
        Set<Actor> actual = calculator.calculateRecipients(user, true, " @first @second Private", PROCESS_ID);
        assertEquals(expected, actual);
    }

    @Test
    public void whenMessageIsPrivate_thenReturnMentionedActorsIgnoringWhitespacesAndBrTags() {
        when(user.getActor()).thenReturn(actor("first"));
        when(executorDao.getExecutor("first")).thenReturn(actor("first"));
        when(executorDao.getExecutor("second")).thenReturn(actor("second"));
        when(executionLogic.getAllExecutorsByProcessId(notNull(), notNull(), eq(true)))
                .thenReturn(newHashSet(actor("first"), actor("second")));

        Set<Actor> expected = newHashSet(actor("first"), actor("second"));
        Set<Actor> actual = calculator.calculateRecipients(user, true, "@first <br /><br /> @second<br />  @ Private", PROCESS_ID);
        assertEquals(expected, actual);
    }

    @Test
    public void whenMentionedExecutorIsGroup_thenReturnGroupActors() {
        when(user.getActor()).thenReturn(actor("first"));
        when(executorDao.getExecutor("group")).thenReturn(group());
        when(executorDao.getGroupActors(group())).thenReturn(actors);
        when(executionLogic.getAllExecutorsByProcessId(notNull(), notNull(), eq(true)))
                .thenReturn(newHashSet(actor("first"), actor("second"), actor("third")));

        Set<Actor> expected = actors;
        Set<Actor> actual = calculator.calculateRecipients(user, true, "@group Private", PROCESS_ID);
        assertEquals(expected, actual);
    }

    @Test
    public void whenExceptionIsCaught_thenMethodContinuesToRun() {
        when(user.getActor()).thenReturn(actor("first"));
        when(executorDao.getExecutor("first")).thenReturn(actor("first"));
        doThrow(new ExecutorDoesNotExistException("incorrect", Actor.class))
                .when(executorDao).getExecutor("incorrect");
        when(executionLogic.getAllExecutorsByProcessId(notNull(), notNull(), eq(true)))
                .thenReturn(newHashSet(actor("first"), actor("incorrect")));

        Set<Actor> expected = newHashSet(actor("first"));
        Set<Actor> actual = calculator.calculateRecipients(user, true, "@incorrect @first Private", PROCESS_ID);
        assertEquals(expected, actual);
    }

    private static Actor actor(String name) {
        return new Actor(name, "");
    }

    private static Group group() {
        return new Group("group", "");
    }
}

