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

import java.util.Collection;
import java.util.HashMap;
import java.util.List;

import org.apache.cactus.ServletTestCase;

import ru.runa.wf.service.WfServiceTestHelper;
import ru.runa.wfe.definition.DefinitionPermission;
import ru.runa.wfe.definition.dto.WfDefinition;
import ru.runa.wfe.presentation.BatchPresentation;
import ru.runa.wfe.security.ASystem;
import ru.runa.wfe.security.Permission;
import ru.runa.wfe.security.SystemPermission;
import ru.runa.wfe.ss.Substitution;
import ru.runa.wfe.ss.SubstitutionCriteria;
import ru.runa.wfe.ss.SubstitutionCriteriaSwimlane;
import ru.runa.wfe.task.dto.WfTask;
import ru.runa.wfe.user.Actor;
import ru.runa.wfe.user.ActorPermission;
import ru.runa.wfe.user.User;

import com.google.common.collect.Lists;

public class GetNewSubstitutorTaskListTest extends ServletTestCase {
    private final static String PREFIX = GetNewSubstitutorTaskListTest.class.getName();

    private static final String PROCESS_FILE_URL = WfServiceTestHelper.ONE_SWIMLANE_FILE_NAME;
    private final static String PROCESS_NAME = WfServiceTestHelper.ONE_SWIMLANE_PROCESS_NAME;

    private final static String nameSubstitutedActor = "substitutedActor";
    private final static String nameSubstitutor = "substitutor";
    private final static String nameSubstitutor2 = "substitutor2";

    private final static String pwdSubstitutedActor = "substitutedActor";
    private final static String pwdSubstitutor = "substitutor";
    private final static String pwdSubstitutor2 = "substitutor2";

    private User substituted = null;
    private User substitutor = null;
    private User substitutor2 = null;

    private SubstitutionCriteria substitutionCriteria_always;
    private SubstitutionCriteriaSwimlane substitutionCriteria_requester;
    private SubstitutionCriteriaSwimlane substitutionCriteria_no_requester;

    private WfServiceTestHelper testHelper;

    private BatchPresentation batchPresentation;

    @Override
    protected void setUp() throws Exception {
        testHelper = new WfServiceTestHelper(PREFIX);

        Actor substitutedActor = testHelper.createActorIfNotExist(nameSubstitutedActor, PREFIX);
        testHelper.getExecutorService().setPassword(testHelper.getAdminUser(), substitutedActor, nameSubstitutedActor);
        Actor substitutor = testHelper.createActorIfNotExist(nameSubstitutor, PREFIX);
        testHelper.getExecutorService().setPassword(testHelper.getAdminUser(), substitutor, nameSubstitutor);
        Actor substitutor2 = testHelper.createActorIfNotExist(nameSubstitutor2, PREFIX);
        testHelper.getExecutorService().setPassword(testHelper.getAdminUser(), substitutor2, nameSubstitutor2);

        {
            Collection<Permission> perm = Lists.newArrayList(SystemPermission.LOGIN_TO_SYSTEM);
            testHelper.getAuthorizationService().setPermissions(testHelper.getAdminUser(), substitutedActor.getId(), perm, ASystem.INSTANCE);
            testHelper.getAuthorizationService().setPermissions(testHelper.getAdminUser(), substitutor.getId(), perm, ASystem.INSTANCE);
            testHelper.getAuthorizationService().setPermissions(testHelper.getAdminUser(), substitutor2.getId(), perm, ASystem.INSTANCE);
        }
        {
            Collection<Permission> perm = Lists.newArrayList(ActorPermission.READ);
            testHelper.getAuthorizationService().setPermissions(testHelper.getAdminUser(), substitutedActor.getId(), perm, substitutor);
            testHelper.getAuthorizationService().setPermissions(testHelper.getAdminUser(), substitutor.getId(), perm, substitutedActor);
            testHelper.getAuthorizationService().setPermissions(testHelper.getAdminUser(), substitutor2.getId(), perm, substitutedActor);
        }

        substituted = testHelper.getAuthenticationService().authenticateByLoginPassword(nameSubstitutedActor, pwdSubstitutedActor);
        this.substitutor = testHelper.getAuthenticationService().authenticateByLoginPassword(nameSubstitutor, pwdSubstitutor);
        this.substitutor2 = testHelper.getAuthenticationService().authenticateByLoginPassword(nameSubstitutor2, pwdSubstitutor2);

        substitutionCriteria_always = null;
        substitutionCriteria_requester = new SubstitutionCriteriaSwimlane();
        substitutionCriteria_requester.setConfiguration(PROCESS_NAME + ".requester");
        substitutionCriteria_requester.setName(PROCESS_NAME + ".requester");
        substitutionCriteria_requester = testHelper.createSubstitutionCriteria(substitutionCriteria_requester);
        substitutionCriteria_no_requester = new SubstitutionCriteriaSwimlane();
        substitutionCriteria_no_requester.setConfiguration(PROCESS_NAME + ".No_requester");
        substitutionCriteria_no_requester.setName(PROCESS_NAME + ".No_requester");
        substitutionCriteria_no_requester = testHelper.createSubstitutionCriteria(substitutionCriteria_no_requester);

        byte[] parBytes = WfServiceTestHelper.readBytesFromFile(PROCESS_FILE_URL);
        testHelper.getDefinitionService().deployProcessDefinition(testHelper.getAdminUser(), parBytes, Lists.newArrayList("testProcess"));
        WfDefinition definition = testHelper.getDefinitionService().getLatestProcessDefinition(testHelper.getAdminUser(), PROCESS_NAME);
        Collection<Permission> definitionPermission = Lists.newArrayList(DefinitionPermission.START_PROCESS);
        testHelper.getAuthorizationService().setPermissions(testHelper.getAdminUser(), substitutedActor.getId(), definitionPermission, definition);

        batchPresentation = testHelper.getTaskBatchPresentation();
        super.setUp();
    }

    @Override
    protected void tearDown() throws Exception {
        testHelper.getDefinitionService().undeployProcessDefinition(testHelper.getAdminUser(), PROCESS_NAME, null);
        testHelper.releaseResources();
        testHelper.removeSubstitutionCriteria(substitutionCriteria_always);
        testHelper.removeSubstitutionCriteria(substitutionCriteria_requester);
        testHelper.removeSubstitutionCriteria(substitutionCriteria_no_requester);
        super.tearDown();
    }

    /*
     * Simple test case. Using process one_swimline_process and one substitutor with always subsitution rules. Checking correct task's list on
     * active/inactive actors.
     */
    public void testSubstitutionSimple() throws Exception {
        Substitution substitution1 = testHelper.createActorSubstitutor(substituted, "ru.runa.af.organizationfunction.ExecutorByNameFunction("
                + nameSubstitutor + ")", substitutionCriteria_always, true);
        Substitution substitution2 = testHelper.createActorSubstitutor(substituted, "ru.runa.af.organizationfunction.ExecutorByNameFunction("
                + nameSubstitutor2 + ")", substitutionCriteria_always, true);
        {
            // Will check precondition - no tasks to all actor's
            checkTaskList(substituted, 0);
            checkTaskList(substitutor, 0);
            checkTaskList(substitutor2, 0);
        }

        testHelper.getExecutionService().startProcess(substituted, PROCESS_NAME, null, null);

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
        tasks = testHelper.getTaskService().getMyTasks(substituted, batchPresentation);
        testHelper.getTaskService().completeTask(substituted, tasks.get(0).getId(), new HashMap<String, Object>(), null);
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
        tasks = testHelper.getTaskService().getMyTasks(substituted, batchPresentation);
        testHelper.getTaskService().completeTask(substitutor, tasks.get(0).getId(), new HashMap<String, Object>(), null);
        {
            checkTaskList(substituted, 0);
            checkTaskList(substitutor, 0);
            checkTaskList(substitutor2, 0);
        }
        testHelper.removeCriteriaFromSubstitution(substitution1);
        testHelper.removeCriteriaFromSubstitution(substitution2);
    }

    public void testSubstitutionByCriteria() throws Exception {
        Substitution substitution1 = testHelper.createActorSubstitutor(substituted, "ru.runa.af.organizationfunction.ExecutorByNameFunction("
                + nameSubstitutor + ")", substitutionCriteria_requester, true);
        Substitution substitution2 = testHelper.createActorSubstitutor(substituted, "ru.runa.af.organizationfunction.ExecutorByNameFunction("
                + nameSubstitutor2 + ")", substitutionCriteria_always, true);
        {
            // Will heck precondition - no tasks to all actor's
            checkTaskList(substituted, 0);
            checkTaskList(substitutor, 0);
            checkTaskList(substitutor2, 0);
        }

        testHelper.getExecutionService().startProcess(substituted, PROCESS_NAME, null, null);

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
        tasks = testHelper.getTaskService().getMyTasks(substituted, batchPresentation);
        testHelper.getTaskService().completeTask(substituted, tasks.get(0).getId(), new HashMap<String, Object>(), null);
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
        tasks = testHelper.getTaskService().getMyTasks(substituted, batchPresentation);
        testHelper.getTaskService().completeTask(substitutor, tasks.get(0).getId(), new HashMap<String, Object>(), null);
        {
            checkTaskList(substituted, 0);
            checkTaskList(substitutor, 0);
            checkTaskList(substitutor2, 0);
        }
        testHelper.removeCriteriaFromSubstitution(substitution1);
        testHelper.removeCriteriaFromSubstitution(substitution2);
    }

    public void testSubstitutionByFalseCriteria() throws Exception {
        Substitution substitution1 = testHelper.createActorSubstitutor(substituted, "ru.runa.af.organizationfunction.ExecutorByNameFunction("
                + nameSubstitutor + ")", substitutionCriteria_no_requester, true);
        Substitution substitution2 = testHelper.createActorSubstitutor(substituted, "ru.runa.af.organizationfunction.ExecutorByNameFunction("
                + nameSubstitutor2 + ")", substitutionCriteria_always, true);
        {
            // Will heck precondition - no tasks to all actor's
            checkTaskList(substituted, 0);
            checkTaskList(substitutor, 0);
            checkTaskList(substitutor2, 0);
        }

        testHelper.getExecutionService().startProcess(substituted, PROCESS_NAME, null, null);

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
        tasks = testHelper.getTaskService().getMyTasks(substituted, batchPresentation);
        testHelper.getTaskService().completeTask(substituted, tasks.get(0).getId(), new HashMap<String, Object>(), null);
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
        tasks = testHelper.getTaskService().getMyTasks(substituted, batchPresentation);
        testHelper.getTaskService().completeTask(substituted, tasks.get(0).getId(), new HashMap<String, Object>(), null);
        {
            checkTaskList(substituted, 0);
            checkTaskList(substitutor, 0);
            checkTaskList(substitutor2, 0);
        }
        testHelper.removeCriteriaFromSubstitution(substitution1);
        testHelper.removeCriteriaFromSubstitution(substitution2);
    }

    public void testSubstitutionFalseTermination() throws Exception {
        Substitution substitution1 = testHelper.createTerminator(substituted, substitutionCriteria_no_requester, true);
        Substitution substitution2 = testHelper.createActorSubstitutor(substituted, "ru.runa.af.organizationfunction.ExecutorByNameFunction("
                + nameSubstitutor + ")", substitutionCriteria_always, true);
        Substitution substitution3 = testHelper.createActorSubstitutor(substituted, "ru.runa.af.organizationfunction.ExecutorByNameFunction("
                + nameSubstitutor2 + ")", substitutionCriteria_always, true);
        {
            // Will heck precondition - no tasks to all actor's
            checkTaskList(substituted, 0);
            checkTaskList(substitutor, 0);
            checkTaskList(substitutor2, 0);
        }

        testHelper.getExecutionService().startProcess(substituted, PROCESS_NAME, null, null);

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
        tasks = testHelper.getTaskService().getMyTasks(substituted, batchPresentation);
        testHelper.getTaskService().completeTask(substituted, tasks.get(0).getId(), new HashMap<String, Object>(), null);
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
        tasks = testHelper.getTaskService().getMyTasks(substituted, batchPresentation);
        testHelper.getTaskService().completeTask(substitutor, tasks.get(0).getId(), new HashMap<String, Object>(), null);
        {
            checkTaskList(substituted, 0);
            checkTaskList(substitutor, 0);
            checkTaskList(substitutor2, 0);
        }
        testHelper.removeCriteriaFromSubstitution(substitution1);
        testHelper.removeCriteriaFromSubstitution(substitution2);
        testHelper.removeCriteriaFromSubstitution(substitution3);
    }

    public void testSubstitutionTrueTermination() throws Exception {
        Substitution substitution1 = testHelper.createTerminator(substituted, substitutionCriteria_requester, true);
        Substitution substitution2 = testHelper.createActorSubstitutor(substituted, "ru.runa.af.organizationfunction.ExecutorByNameFunction("
                + nameSubstitutor + ")", substitutionCriteria_always, true);
        Substitution substitution3 = testHelper.createActorSubstitutor(substituted, "ru.runa.af.organizationfunction.ExecutorByNameFunction("
                + nameSubstitutor2 + ")", substitutionCriteria_always, true);
        {
            // Will heck precondition - no tasks to all actor's
            checkTaskList(substituted, 0);
            checkTaskList(substitutor, 0);
            checkTaskList(substitutor2, 0);
        }

        testHelper.getExecutionService().startProcess(substituted, PROCESS_NAME, null, null);

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
        tasks = testHelper.getTaskService().getMyTasks(substituted, batchPresentation);
        testHelper.getTaskService().completeTask(substituted, tasks.get(0).getId(), new HashMap<String, Object>(), null);
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
        tasks = testHelper.getTaskService().getMyTasks(substituted, batchPresentation);
        testHelper.getTaskService().completeTask(substitutor, tasks.get(0).getId(), new HashMap<String, Object>(), null);
        {
            checkTaskList(substituted, 0);
            checkTaskList(substitutor, 0);
            checkTaskList(substitutor2, 0);
        }
        testHelper.removeCriteriaFromSubstitution(substitution1);
        testHelper.removeCriteriaFromSubstitution(substitution2);
        testHelper.removeCriteriaFromSubstitution(substitution3);
    }

    private void checkTaskList(User user, int expectedLength) throws Exception {
        List<WfTask> tasks = testHelper.getTaskService().getMyTasks(user, batchPresentation);
        assertEquals("getTasks() returns wrong tasks number (expected " + expectedLength + ", but was " + tasks.size() + ")", expectedLength,
                tasks.size());
        // Let's change actor status to check correct working.
        Actor actor = user.getActor();
        boolean actorStatus = actor.isActive();

        setStatus(user, !actorStatus);
        setStatus(user, actorStatus);

        tasks = testHelper.getTaskService().getMyTasks(user, batchPresentation);
        assertEquals("getTasks() returns wrong tasks number (expected " + expectedLength + ", but was " + tasks.size() + ")", expectedLength,
                tasks.size());
        actorStatus = testHelper.getExecutorService().<Actor> getExecutor(testHelper.getAdminUser(), substituted.getActor().getId()).isActive();

        setStatus(substituted, !actorStatus);

        if (!actorStatus) {
            tasks = testHelper.getTaskService().getMyTasks(substitutor, batchPresentation);
            assertEquals("getTasks() returns wrong tasks number (expected " + 0 + ", but was " + tasks.size() + ")", 0, tasks.size());
            tasks = testHelper.getTaskService().getMyTasks(substitutor2, batchPresentation);
            assertEquals("getTasks() returns wrong tasks number (expected " + 0 + ", but was " + tasks.size() + ")", 0, tasks.size());
        }

        setStatus(substituted, actorStatus);

        tasks = testHelper.getTaskService().getMyTasks(user, batchPresentation);
        assertEquals("getTasks() returns wrong tasks number (expected " + expectedLength + ", but was " + tasks.size() + ")", expectedLength,
                tasks.size());
    }

    private void setStatus(User user, boolean actorStatus) {
        testHelper.getExecutorService().setStatus(testHelper.getAdminUser(), user.getActor(), actorStatus);
        // hibernate merge workaround
        Actor actor = testHelper.createActorIfNotExist(user.getActor().getName(), PREFIX);
        user.setActor(actor);
    }
}
