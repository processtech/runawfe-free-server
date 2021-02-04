package ru.runa.wfe.chat.utils;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.junit.MockitoJUnitRunner;
import ru.runa.wfe.execution.logic.ExecutionLogic;
import ru.runa.wfe.user.Actor;
import ru.runa.wfe.user.User;
import ru.runa.wfe.user.logic.ExecutorLogic;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class RecipientCalculatorsTest {

    @Mock
    private ExecutorLogic executorLogic;
    @Mock
    private ExecutionLogic executionLogic;
    @Mock
    private User user;
    @Mock
    private Actor firstActor;
    @Mock
    private Actor secondActor;
    private List<Actor> actors;
    private RecipientCalculators calculators;
    private final Long processId = 1L;

    @Before
    public void init() {
        actors = Arrays.asList(firstActor, secondActor, mock(Actor.class));
        calculators = new RecipientCalculators(executorLogic, executionLogic);
    }

    @Test
    public void whenMessageIsNotPrivate_thenReturnAllActors() {
        when(executionLogic.getAllExecutorsByProcessId(processId)).thenReturn(new HashSet<>(actors));

        Set<Actor> expected = new HashSet<>(actors);
        Set<Actor> actual = calculators.calculateRecipients(user, false, "Public", processId);
        Assert.assertEquals(expected, actual);
    }

    @Test
    public void whenMessageIsPrivate_thenReturnMentionedActors() {
        when(executorLogic.getExecutor(user, "first")).thenReturn(firstActor);
        when(executorLogic.getExecutor(user, "second")).thenReturn(secondActor);

        Set<Actor> expected = new HashSet<>(Arrays.asList(firstActor, secondActor));
        Set<Actor> actual = calculators.calculateRecipients(user, true, "@first @second Private", processId);
        Assert.assertEquals(expected, actual);
    }

}