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
import java.util.List;

import org.apache.cactus.ServletTestCase;

import ru.runa.wf.service.WfServiceTestHelper;
import ru.runa.wfe.definition.DefinitionArchiveFormatException;
import ru.runa.wfe.definition.DefinitionDoesNotExistException;
import ru.runa.wfe.definition.DefinitionPermission;
import ru.runa.wfe.definition.dto.WfDefinition;
import ru.runa.wfe.security.AuthenticationException;
import ru.runa.wfe.security.AuthorizationException;
import ru.runa.wfe.security.Permission;
import ru.runa.wfe.service.DefinitionService;
import ru.runa.wfe.service.delegate.Delegates;
import ru.runa.wfe.user.User;

import com.google.common.collect.Lists;

/**
 * Created on 20.04.2005
 * 
 * @author Gritsenko_S
 */
public class DefinitionServiceDelegateRedeployProcessDefinitionTest extends ServletTestCase {

    private DefinitionService definitionService;

    private WfServiceTestHelper helper = null;

    private long processDefinitionId;

    @Override
    protected void setUp() throws Exception {
        helper = new WfServiceTestHelper(getClass().getName());
        definitionService = Delegates.getDefinitionService();

        helper.deployValidProcessDefinition();

        processDefinitionId = definitionService.getLatestProcessDefinition(helper.getAdminUser(), WfServiceTestHelper.VALID_PROCESS_NAME).getId();

        Collection<Permission> redeployPermissions = Lists.newArrayList(DefinitionPermission.READ, DefinitionPermission.REDEPLOY_DEFINITION);
        helper.setPermissionsToAuthorizedPerformerOnDefinitionByName(redeployPermissions, WfServiceTestHelper.VALID_PROCESS_NAME);

        super.setUp();
    }

    @Override
    protected void tearDown() throws Exception {
        helper.undeployValidProcessDefinition();

        helper.releaseResources();
        definitionService = null;
        super.tearDown();
    }

    public void testRedeployProcessByAuthorizedPerformer() throws Exception {
        definitionService.redeployProcessDefinition(helper.getAuthorizedPerformerUser(), processDefinitionId, helper.getValidProcessDefinition(),
                Lists.newArrayList("testProcess"));
        List<WfDefinition> deployedProcesses = definitionService.getProcessDefinitions(helper.getAuthorizedPerformerUser(),
                helper.getProcessDefinitionBatchPresentation(), false);
        if (deployedProcesses.size() != 1) {
            assertTrue("testRedeployProcessByAuthorizedPerformer wrongNumberOfProcessDefinitions", false);
        }
        if (!deployedProcesses.get(0).getName().equals(WfServiceTestHelper.VALID_PROCESS_NAME)) {
            assertTrue("testRedeployProcessByAuthorizedPerformer wrongNameOfDeployedProcessDefinitions", false);
        }
    }

    public void testRedeployProcessByAuthorizedPerformerWithoutREDEPLOYPermission() throws Exception {
        Collection<Permission> nullPermissions = Lists.newArrayList();
        helper.setPermissionsToAuthorizedPerformerOnDefinitionByName(nullPermissions, WfServiceTestHelper.VALID_PROCESS_NAME);

        try {
            definitionService.redeployProcessDefinition(helper.getAuthorizedPerformerUser(), processDefinitionId, helper.getValidProcessDefinition(),
                    Lists.newArrayList("testProcess"));
            assertTrue("definitionDelegate.redeployProcessByAuthorizedPerformer() no AuthorizationException", false);
        } catch (AuthorizationException e) {
        }
    }

    public void testRedeployProcessByUnauthorizedPerformer() throws Exception {
        try {
            definitionService.redeployProcessDefinition(helper.getUnauthorizedPerformerUser(), processDefinitionId,
                    helper.getValidProcessDefinition(), Lists.newArrayList("testProcess"));
            assertTrue("definitionDelegate.redeployProcessByUnauthorizedPerformer() no AuthorizationException", false);
        } catch (AuthorizationException e) {
        }
    }

    public void testRedeployProcessWithNullSubject() throws Exception {
        try {
            definitionService.redeployProcessDefinition(null, processDefinitionId, helper.getValidProcessDefinition(),
                    Lists.newArrayList("testProcess"));
            assertTrue("testRedeployProcessWithNullSubject no IllegalArgumentException", false);
        } catch (IllegalArgumentException e) {
            // That's what we expect
        }
    }

    public void testRedeployProcessWithFakeSubject() throws Exception {
        try {
            User fakeUser = helper.getFakeUser();
            definitionService.redeployProcessDefinition(fakeUser, processDefinitionId, helper.getValidProcessDefinition(),
                    Lists.newArrayList("testProcess"));
            assertTrue("testRedeployProcessWithFakeSubject no AuthenticationException", false);
        } catch (AuthenticationException e) {
        }
    }

    public void testRedeployInvalidProcessByAuthorizedPerformer() throws Exception {
        try {
            definitionService.redeployProcessDefinition(helper.getAuthorizedPerformerUser(), processDefinitionId,
                    helper.getInValidProcessDefinition(), Lists.newArrayList("testProcess"));
            assertTrue("definitionDelegate.deployProcessByAuthorizedPerformer() no DefinitionParsingException", false);
        } catch (DefinitionArchiveFormatException e) {
        }
    }

    public void testRedeployWithInvalidProcessId() throws Exception {
        try {
            definitionService.redeployProcessDefinition(helper.getAuthorizedPerformerUser(), -1l, helper.getValidProcessDefinition(),
                    Lists.newArrayList("testProcess"));
            fail("testRedeployWithInvalidProcessId() no Exception");
        } catch (DefinitionDoesNotExistException e) {
        }
    }

    public void testRedeployInvalidProcess() throws Exception {
        try {
            definitionService.redeployProcessDefinition(helper.getAuthorizedPerformerUser(), processDefinitionId,
                    helper.getInValidProcessDefinition(), Lists.newArrayList("testProcess"));
            fail("testRedeployInvalidProcess() no Exception");
        } catch (DefinitionArchiveFormatException e) {
        }
    }

}
