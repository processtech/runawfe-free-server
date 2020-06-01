/*
 * This file is part of the RUNA WFE project.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public License
 * as published by the Free Software Foundation; version 2.1
 * of the License.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA.
 */

package ru.runa.wf.delegate;

import com.google.common.collect.Lists;
import java.util.List;
import lombok.SneakyThrows;
import lombok.val;
import org.apache.cactus.ServletTestCase;
import ru.runa.wf.service.WfServiceTestHelper;
import ru.runa.wfe.definition.dto.WfDefinition;
import ru.runa.wfe.presentation.BatchPresentation;
import ru.runa.wfe.security.Permission;
import ru.runa.wfe.security.SecuredSingleton;
import ru.runa.wfe.ss.Substitution;
import ru.runa.wfe.ss.SubstitutionCriteria;
import ru.runa.wfe.ss.SubstitutionCriteriaSwimlane;
import ru.runa.wfe.task.dto.WfTask;
import ru.runa.wfe.user.Actor;
import ru.runa.wfe.user.User;

public class GetNewSubstitutorTaskListTest extends ServletTestCase {
    private static final String PROCESS_FILE_URL = WfServiceTestHelper.ONE_SWIMLANE_FILE_NAME;
    private final static String PROCESS_NAME = WfServiceTestHelper.ONE_SWIMLANE_PROCESS_NAME;

    private final static String nameSubstitutedActor = "substitutedActor";
    private final static String nameSubstitutor = "substitutor";
    private final static String nameSubstitutor2 = "substitutor2";

    private final static String pwdSubstitutedActor = "substitutedActor";
    private final static String pwdSubstitutor = "substitutor";
    private final static String pwdSubstitutor2 = "substitutor2";

    private String PREFIX = getClass().getName();
    private WfServiceTestHelper h;
    private BatchPresentation batchPresentation;

    private User substituted = null;
    private User substitutor = null;
    private User substitutor2 = null;

    private SubstitutionCriteria substitutionCriteria_always;
    private SubstitutionCriteriaSwimlane substitutionCriteria_requester;
    private SubstitutionCriteriaSwimlane substitutionCriteria_no_requester;

    @Override
    protected void setUp() {
        h = new WfServiceTestHelper(PREFIX);

        Actor substitutedActor = h.createActorIfNotExist(nameSubstitutedActor, PREFIX);
        h.getExecutorService().setPassword(h.getAdminUser(), substitutedActor, nameSubstitutedActor);
        Actor substitutor = h.createActorIfNotExist(nameSubstitutor, PREFIX);
        h.getExecutorService().setPassword(h.getAdminUser(), substitutor, nameSubstitutor);
        Actor substitutor2 = h.createActorIfNotExist(nameSubstitutor2, PREFIX);
        h.getExecutorService().setPassword(h.getAdminUser(), substitutor2, nameSubstitutor2);

        {
            val pp = Lists.newArrayList(Permission.LOGIN);
            h.getAuthorizationService().setPermissions(h.getAdminUser(), substitutedActor.getId(), pp, SecuredSingleton.SYSTEM);
            h.getAuthorizationService().setPermissions(h.getAdminUser(), substitutor.getId(), pp, SecuredSingleton.SYSTEM);
            h.getAuthorizationService().setPermissions(h.getAdminUser(), substitutor2.getId(), pp, SecuredSingleton.SYSTEM);
        }
        {
            val pp = Lists.newArrayList(Permission.READ);
            h.getAuthorizationService().setPermissions(h.getAdminUser(), substitutedActor.getId(), pp, substitutor);
            h.getAuthorizationService().setPermissions(h.getAdminUser(), substitutor.getId(), pp, substitutedActor);
            h.getAuthorizationService().setPermissions(h.getAdminUser(), substitutor2.getId(), pp, substitutedActor);
        }

        substituted = h.getAuthenticationService().authenticateByLoginPassword(nameSubstitutedActor, pwdSubstitutedActor);
        this.substitutor = h.getAuthenticationService().authenticateByLoginPassword(nameSubstitutor, pwdSubstitutor);
        this.substitutor2 = h.getAuthenticationService().authenticateByLoginPassword(nameSubstitutor2, pwdSubstitutor2);

        substitutionCriteria_always = null;
        substitutionCriteria_requester = new SubstitutionCriteriaSwimlane();
        substitutionCriteria_requester.setConfiguration(PROCESS_NAME + ".requester");
        substitutionCriteria_requester.setName(PROCESS_NAME + ".requester");
        substitutionCriteria_requester = h.createSubstitutionCriteria(substitutionCriteria_requester);
        substitutionCriteria_no_requester = new SubstitutionCriteriaSwimlane();
        substitutionCriteria_no_requester.setConfiguration(PROCESS_NAME + ".No_requester");
        substitutionCriteria_no_requester.setName(PROCESS_NAME + ".No_requester");
        substitutionCriteria_no_requester = h.createSubstitutionCriteria(substitutionCriteria_no_requester);

        byte[] parBytes = WfServiceTestHelper.readBytesFromFile(PROCESS_FILE_URL);
        h.getDefinitionService().deployProcessDefinition(h.getAdminUser(), parBytes, Lists.newArrayList("testProcess"));
        WfDefinition definition = h.getDefinitionService().getLatestProcessDefinition(h.getAdminUser(), PROCESS_NAME);
        h.getAuthorizationService().setPermissions(h.getAdminUser(), substitutedActor.getId(), Lists.newArrayList(Permission.START_PROCESS),
                definition);

        batchPresentation = h.getTaskBatchPresentation();
    }

    @Override
    protected void tearDown() {
        h.getDefinitionService().undeployProcessDefinition(h.getAdminUser(), PROCESS_NAME, null);
        h.releaseResources();
        h.removeSubstitutionCriteria(substitutionCriteria_always);
        h.removeSubstitutionCriteria(substitutionCriteria_requester);
        h.removeSubstitutionCriteria(substitutionCriteria_no_requester);
    }

    /*
     * Simple test case. Using process one_swimline_process and one substitutor with always subsitution rules. Checking correct task's list on
     * active/inactive actors.
     */
    public void testSubstitutionSimple() {
        Substitution substitution1 = h.createActorSubstitutor(substituted,
                "ru.runa.af.organizationfunction.ExecutorByNameFunction(" + nameSubstitutor + ")", substitutionCriteria_always, true);
        Substitution substitution2 = h.createActorSubstitutor(substituted,
                "ru.runa.af.organizationfunction.ExecutorByNameFunction(" + nameSubstitutor2 + ")", substitutionCriteria_always, true);
        {
            // Will check precondition - no tasks to all actor's
            checkTaskList(substituted, 0);
            checkTaskList(substitutor, 0);
            checkTaskList(substitutor2, 0);
        }

        h.getExecutionService().startProcess(substituted, PROCESS_NAME, null);

        {
            checkTaskList(substituted, 1);
            checkTaskList(substitutor, 0);
            checkTaskList(substitutor2, 0);
        }
        setStatus(substituted, false);
        {
            checkTaskList(substituted, 1);
            checkTaskList(substitutor, 1);
            checkTaskList(substitutor2, 0);
        }
        setStatus(substitutor, false);
        {
            checkTaskList(substituted, 1);
            checkTaskList(substitutor, 0);
            checkTaskList(substitutor2, 1);
        }
        setStatus(substitutor, true);
        setStatus(substituted, true);
        {
            checkTaskList(substituted, 1);
            checkTaskList(substitutor, 0);
            checkTaskList(substitutor2, 0);
        }
        List<WfTask> tasks;
        tasks = h.getTaskService().getMyTasks(substituted, batchPresentation);
        h.getTaskService().completeTask(substituted, tasks.get(0).getId(), null);
        {
            checkTaskList(substituted, 1);
            checkTaskList(substitutor, 0);
            checkTaskList(substitutor2, 0);
        }
        setStatus(substituted, false);
        {
            checkTaskList(substituted, 1);
            checkTaskList(substitutor, 1);
            checkTaskList(substitutor2, 0);
        }
        setStatus(substitutor, false);
        {
            checkTaskList(substituted, 1);
            checkTaskList(substitutor, 0);
            checkTaskList(substitutor2, 1);
        }
        setStatus(substitutor, true);
        tasks = h.getTaskService().getMyTasks(substituted, batchPresentation);
        h.getTaskService().completeTask(substitutor, tasks.get(0).getId(), null);
        {
            checkTaskList(substituted, 0);
            checkTaskList(substitutor, 0);
            checkTaskList(substitutor2, 0);
        }
        h.removeCriteriaFromSubstitution(substitution1);
        h.removeCriteriaFromSubstitution(substitution2);
    }

    public void testSubstitutionByCriteria() {
        Substitution substitution1 = h.createActorSubstitutor(substituted,
                "ru.runa.af.organizationfunction.ExecutorByNameFunction(" + nameSubstitutor + ")", substitutionCriteria_requester, true);
        Substitution substitution2 = h.createActorSubstitutor(substituted,
                "ru.runa.af.organizationfunction.ExecutorByNameFunction(" + nameSubstitutor2 + ")", substitutionCriteria_always, true);
        {
            // Will heck precondition - no tasks to all actor's
            checkTaskList(substituted, 0);
            checkTaskList(substitutor, 0);
            checkTaskList(substitutor2, 0);
        }

        h.getExecutionService().startProcess(substituted, PROCESS_NAME, null);

        {
            checkTaskList(substituted, 1);
            checkTaskList(substitutor, 0);
            checkTaskList(substitutor2, 0);
        }
        setStatus(substituted, false);
        {
            checkTaskList(substituted, 1);
            checkTaskList(substitutor, 1);
            checkTaskList(substitutor2, 0);
        }
        setStatus(substitutor, false);
        {
            checkTaskList(substituted, 1);
            checkTaskList(substitutor, 0);
            checkTaskList(substitutor2, 1);
        }
        setStatus(substitutor, true);
        setStatus(substituted, true);
        {
            checkTaskList(substituted, 1);
            checkTaskList(substitutor, 0);
            checkTaskList(substitutor2, 0);
        }
        List<WfTask> tasks;
        tasks = h.getTaskService().getMyTasks(substituted, batchPresentation);
        h.getTaskService().completeTask(substituted, tasks.get(0).getId(), null);
        {
            checkTaskList(substituted, 1);
            checkTaskList(substitutor, 0);
            checkTaskList(substitutor2, 0);
        }
        setStatus(substituted, false);
        {
            checkTaskList(substituted, 1);
            checkTaskList(substitutor, 1);
            checkTaskList(substitutor2, 0);
        }
        setStatus(substitutor, false);
        {
            checkTaskList(substituted, 1);
            checkTaskList(substitutor, 0);
            checkTaskList(substitutor2, 1);
        }
        setStatus(substitutor, true);
        tasks = h.getTaskService().getMyTasks(substituted, batchPresentation);
        h.getTaskService().completeTask(substitutor, tasks.get(0).getId(), null);
        {
            checkTaskList(substituted, 0);
            checkTaskList(substitutor, 0);
            checkTaskList(substitutor2, 0);
        }
        h.removeCriteriaFromSubstitution(substitution1);
        h.removeCriteriaFromSubstitution(substitution2);
    }

    public void testSubstitutionByFalseCriteria() {
        Substitution substitution1 = h.createActorSubstitutor(substituted,
                "ru.runa.af.organizationfunction.ExecutorByNameFunction(" + nameSubstitutor + ")", substitutionCriteria_no_requester, true);
        Substitution substitution2 = h.createActorSubstitutor(substituted,
                "ru.runa.af.organizationfunction.ExecutorByNameFunction(" + nameSubstitutor2 + ")", substitutionCriteria_always, true);
        {
            // Will heck precondition - no tasks to all actor's
            checkTaskList(substituted, 0);
            checkTaskList(substitutor, 0);
            checkTaskList(substitutor2, 0);
        }

        h.getExecutionService().startProcess(substituted, PROCESS_NAME, null);

        {
            checkTaskList(substituted, 1);
            checkTaskList(substitutor, 0);
            checkTaskList(substitutor2, 0);
        }
        setStatus(substituted, false);
        {
            checkTaskList(substituted, 1);
            checkTaskList(substitutor, 0);
            checkTaskList(substitutor2, 1);
        }
        setStatus(substitutor, false);
        {
            checkTaskList(substituted, 1);
            checkTaskList(substitutor, 0);
            checkTaskList(substitutor2, 1);
        }
        setStatus(substitutor, true);
        setStatus(substituted, true);
        {
            checkTaskList(substituted, 1);
            checkTaskList(substitutor, 0);
            checkTaskList(substitutor2, 0);
        }
        List<WfTask> tasks;
        tasks = h.getTaskService().getMyTasks(substituted, batchPresentation);
        h.getTaskService().completeTask(substituted, tasks.get(0).getId(), null);
        {
            checkTaskList(substituted, 1);
            checkTaskList(substitutor, 0);
            checkTaskList(substitutor2, 0);
        }
        setStatus(substituted, false);
        {
            checkTaskList(substituted, 1);
            checkTaskList(substitutor, 0);
            checkTaskList(substitutor2, 1);
        }
        setStatus(substitutor, false);
        {
            checkTaskList(substituted, 1);
            checkTaskList(substitutor, 0);
            checkTaskList(substitutor2, 1);
        }
        setStatus(substitutor, true);
        tasks = h.getTaskService().getMyTasks(substituted, batchPresentation);
        h.getTaskService().completeTask(substituted, tasks.get(0).getId(), null);
        {
            checkTaskList(substituted, 0);
            checkTaskList(substitutor, 0);
            checkTaskList(substitutor2, 0);
        }
        h.removeCriteriaFromSubstitution(substitution1);
        h.removeCriteriaFromSubstitution(substitution2);
    }

    public void testSubstitutionFalseTermination() {
        Substitution substitution1 = h.createTerminator(substituted, substitutionCriteria_no_requester, true);
        Substitution substitution2 = h.createActorSubstitutor(substituted,
                "ru.runa.af.organizationfunction.ExecutorByNameFunction(" + nameSubstitutor + ")", substitutionCriteria_always, true);
        Substitution substitution3 = h.createActorSubstitutor(substituted,
                "ru.runa.af.organizationfunction.ExecutorByNameFunction(" + nameSubstitutor2 + ")", substitutionCriteria_always, true);
        {
            // Will heck precondition - no tasks to all actor's
            checkTaskList(substituted, 0);
            checkTaskList(substitutor, 0);
            checkTaskList(substitutor2, 0);
        }

        h.getExecutionService().startProcess(substituted, PROCESS_NAME, null);

        {
            checkTaskList(substituted, 1);
            checkTaskList(substitutor, 0);
            checkTaskList(substitutor2, 0);
        }
        setStatus(substituted, false);
        {
            checkTaskList(substituted, 1);
            checkTaskList(substitutor, 1);
            checkTaskList(substitutor2, 0);
        }
        setStatus(substitutor, false);
        {
            checkTaskList(substituted, 1);
            checkTaskList(substitutor, 0);
            checkTaskList(substitutor2, 1);
        }
        setStatus(substitutor, true);
        setStatus(substituted, true);
        {
            checkTaskList(substituted, 1);
            checkTaskList(substitutor, 0);
            checkTaskList(substitutor2, 0);
        }
        List<WfTask> tasks;
        tasks = h.getTaskService().getMyTasks(substituted, batchPresentation);
        h.getTaskService().completeTask(substituted, tasks.get(0).getId(), null);
        {
            checkTaskList(substituted, 1);
            checkTaskList(substitutor, 0);
            checkTaskList(substitutor2, 0);
        }
        setStatus(substituted, false);
        {
            checkTaskList(substituted, 1);
            checkTaskList(substitutor, 1);
            checkTaskList(substitutor2, 0);
        }
        setStatus(substitutor, false);
        {
            checkTaskList(substituted, 1);
            checkTaskList(substitutor, 0);
            checkTaskList(substitutor2, 1);
        }
        setStatus(substitutor, true);
        tasks = h.getTaskService().getMyTasks(substituted, batchPresentation);
        h.getTaskService().completeTask(substitutor, tasks.get(0).getId(), null);
        {
            checkTaskList(substituted, 0);
            checkTaskList(substitutor, 0);
            checkTaskList(substitutor2, 0);
        }
        h.removeCriteriaFromSubstitution(substitution1);
        h.removeCriteriaFromSubstitution(substitution2);
        h.removeCriteriaFromSubstitution(substitution3);
    }

    public void testSubstitutionTrueTermination() {
        Substitution substitution1 = h.createTerminator(substituted, substitutionCriteria_requester, true);
        Substitution substitution2 = h.createActorSubstitutor(substituted,
                "ru.runa.af.organizationfunction.ExecutorByNameFunction(" + nameSubstitutor + ")", substitutionCriteria_always, true);
        Substitution substitution3 = h.createActorSubstitutor(substituted,
                "ru.runa.af.organizationfunction.ExecutorByNameFunction(" + nameSubstitutor2 + ")", substitutionCriteria_always, true);
        {
            // Will heck precondition - no tasks to all actor's
            checkTaskList(substituted, 0);
            checkTaskList(substitutor, 0);
            checkTaskList(substitutor2, 0);
        }

        h.getExecutionService().startProcess(substituted, PROCESS_NAME, null);

        {
            checkTaskList(substituted, 1);
            checkTaskList(substitutor, 0);
            checkTaskList(substitutor2, 0);
        }
        setStatus(substituted, false);
        {
            checkTaskList(substituted, 1);
            checkTaskList(substitutor, 0);
            checkTaskList(substitutor2, 0);
        }
        setStatus(substitutor, false);
        {
            checkTaskList(substituted, 1);
            checkTaskList(substitutor, 0);
            checkTaskList(substitutor2, 0);
        }
        setStatus(substitutor, true);
        setStatus(substituted, true);
        {
            checkTaskList(substituted, 1);
            checkTaskList(substitutor, 0);
            checkTaskList(substitutor2, 0);
        }
        List<WfTask> tasks;
        tasks = h.getTaskService().getMyTasks(substituted, batchPresentation);
        h.getTaskService().completeTask(substituted, tasks.get(0).getId(), null);
        {
            checkTaskList(substituted, 1);
            checkTaskList(substitutor, 0);
            checkTaskList(substitutor2, 0);
        }
        setStatus(substituted, false);
        {
            checkTaskList(substituted, 1);
            checkTaskList(substitutor, 0);
            checkTaskList(substitutor2, 0);
        }
        setStatus(substitutor, false);
        {
            checkTaskList(substituted, 1);
            checkTaskList(substitutor, 0);
            checkTaskList(substitutor2, 0);
        }
        setStatus(substitutor, true);
        tasks = h.getTaskService().getMyTasks(substituted, batchPresentation);
        h.getTaskService().completeTask(substitutor, tasks.get(0).getId(), null);
        {
            checkTaskList(substituted, 0);
            checkTaskList(substitutor, 0);
            checkTaskList(substitutor2, 0);
        }
        h.removeCriteriaFromSubstitution(substitution1);
        h.removeCriteriaFromSubstitution(substitution2);
        h.removeCriteriaFromSubstitution(substitution3);
    }

    @SneakyThrows
    private void checkTaskList(User user, int expectedLength) {
        List<WfTask> tasks = h.getTaskService().getMyTasks(user, batchPresentation);
        Thread.sleep(50);
        tasks = h.getTaskService().getMyTasks(user, batchPresentation);
        assertEquals("getTasks() returns wrong tasks number (expected " + expectedLength + ", but was " + tasks.size() + ")", expectedLength,
                tasks.size());
        // Let's change actor status to check correct working.
        Actor actor = user.getActor();
        boolean actorStatus = actor.isActive();

        setStatus(user, !actorStatus);
        setStatus(user, actorStatus);

        tasks = h.getTaskService().getMyTasks(user, batchPresentation);
        Thread.sleep(50);
        tasks = h.getTaskService().getMyTasks(user, batchPresentation);
        assertEquals("getTasks() returns wrong tasks number (expected " + expectedLength + ", but was " + tasks.size() + ")", expectedLength,
                tasks.size());
        actorStatus = h.getExecutorService().<Actor>getExecutor(h.getAdminUser(), substituted.getActor().getId()).isActive();

        setStatus(substituted, !actorStatus);

        if (!actorStatus) {
            tasks = h.getTaskService().getMyTasks(substitutor, batchPresentation);
            Thread.sleep(50);
            tasks = h.getTaskService().getMyTasks(substitutor, batchPresentation);
            assertEquals("getTasks() returns wrong tasks number (expected " + 0 + ", but was " + tasks.size() + ")", 0, tasks.size());
            tasks = h.getTaskService().getMyTasks(substitutor2, batchPresentation);
            assertEquals("getTasks() returns wrong tasks number (expected " + 0 + ", but was " + tasks.size() + ")", 0, tasks.size());
        }

        setStatus(substituted, actorStatus);

        tasks = h.getTaskService().getMyTasks(user, batchPresentation);
        Thread.sleep(50);
        tasks = h.getTaskService().getMyTasks(user, batchPresentation);
        assertEquals("getTasks() returns wrong tasks number (expected " + expectedLength + ", but was " + tasks.size() + ")", expectedLength,
                tasks.size());
    }

    private void setStatus(User user, boolean actorStatus) {
        h.getExecutorService().setStatus(h.getAdminUser(), user.getActor(), actorStatus);
        // hibernate merge workaround
        Actor actor = h.createActorIfNotExist(user.getActor().getName(), PREFIX);
        user.setActor(actor);
    }
}
