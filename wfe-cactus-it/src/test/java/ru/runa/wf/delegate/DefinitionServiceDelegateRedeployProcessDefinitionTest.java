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
import java.util.Collection;
import java.util.List;
import org.apache.cactus.ServletTestCase;
import ru.runa.wf.service.WfServiceTestHelper;
import ru.runa.wfe.definition.DefinitionArchiveFormatException;
import ru.runa.wfe.definition.DefinitionDoesNotExistException;
import ru.runa.wfe.definition.dto.WfDefinition;
import ru.runa.wfe.security.AuthenticationException;
import ru.runa.wfe.security.AuthorizationException;
import ru.runa.wfe.security.Permission;
import ru.runa.wfe.service.DefinitionService;
import ru.runa.wfe.service.delegate.Delegates;
import ru.runa.wfe.user.User;

/**
 * Created on 20.04.2005
 * 
 * @author Gritsenko_S
 */
public class DefinitionServiceDelegateRedeployProcessDefinitionTest extends ServletTestCase {
    private WfServiceTestHelper h;
    private DefinitionService definitionService;

    private long processDefinitionId;

    @Override
    protected void setUp() {
        h = new WfServiceTestHelper(getClass().getName());
        definitionService = Delegates.getDefinitionService();

        h.deployValidProcessDefinition();
        processDefinitionId = definitionService.getLatestProcessDefinition(h.getAdminUser(), WfServiceTestHelper.VALID_PROCESS_NAME).getId();
        h.setPermissionsToAuthorizedActorOnDefinitionByName(Lists.newArrayList(Permission.UPDATE), WfServiceTestHelper.VALID_PROCESS_NAME);
    }

    @Override
    protected void tearDown() {
        h.undeployValidProcessDefinition();
        h.releaseResources();
        definitionService = null;
    }

    public void testRedeployProcessByAuthorizedUser() {
        definitionService.redeployProcessDefinition(h.getAuthorizedUser(), processDefinitionId, h.getValidProcessDefinition(),
                Lists.newArrayList("testProcess"));
        List<WfDefinition> deployedProcesses = definitionService.getProcessDefinitions(h.getAuthorizedUser(),
                h.getProcessDefinitionBatchPresentation(), false);
        if (deployedProcesses.size() != 1) {
            fail("testRedeployProcessByAuthorizedUser() wrongNumberOfProcessDefinitions");
        }
        if (!deployedProcesses.get(0).getName().equals(WfServiceTestHelper.VALID_PROCESS_NAME)) {
            fail("testRedeployProcessByAuthorizedUser() wrongNameOfDeployedProcessDefinitions");
        }
    }

    public void testRedeployProcessByAuthorizedUserWithoutREDEPLOYPermission() {
        Collection<Permission> nullPermissions = Lists.newArrayList();
        h.setPermissionsToAuthorizedActorOnDefinitionByName(nullPermissions, WfServiceTestHelper.VALID_PROCESS_NAME);

        try {
            definitionService.redeployProcessDefinition(h.getAuthorizedUser(), processDefinitionId, h.getValidProcessDefinition(),
                    Lists.newArrayList("testProcess"));
            fail("testRedeployProcessByAuthorizedUserWithoutREDEPLOYPermission() no AuthorizationException");
        } catch (AuthorizationException e) {
            // Expected.
        }
    }

    public void testRedeployProcessByUnauthorizedUser() {
        try {
            definitionService.redeployProcessDefinition(h.getUnauthorizedUser(), processDefinitionId,
                    h.getValidProcessDefinition(), Lists.newArrayList("testProcess"));
            fail("testRedeployProcessByUnauthorizedUser() no AuthorizationException");
        } catch (AuthorizationException e) {
            // Expected.
        }
    }

    public void testRedeployProcessWithFakeUser() {
        try {
            User fakeUser = h.getFakeUser();
            definitionService.redeployProcessDefinition(fakeUser, processDefinitionId, h.getValidProcessDefinition(),
                    Lists.newArrayList("testProcess"));
            fail();
        } catch (AuthenticationException e) {
            // Expected.
        }
    }

    public void testRedeployInvalidProcessByAuthorizedUser() {
        try {
            definitionService.redeployProcessDefinition(h.getAuthorizedUser(), processDefinitionId,
                    h.getInValidProcessDefinition(), Lists.newArrayList("testProcess"));
            fail("testRedeployInvalidProcessByAuthorizedUser() no DefinitionParsingException");
        } catch (DefinitionArchiveFormatException e) {
            // Expected.
        }
    }

    public void testRedeployWithInvalidProcessId() {
        try {
            definitionService.redeployProcessDefinition(h.getAuthorizedUser(), -1L, h.getValidProcessDefinition(),
                    Lists.newArrayList("testProcess"));
            fail("testRedeployWithInvalidProcessId() no Exception");
        } catch (DefinitionDoesNotExistException e) {
            // Expected.
        }
    }

    public void testRedeployInvalidProcess() {
        try {
            definitionService.redeployProcessDefinition(h.getAuthorizedUser(), processDefinitionId,
                    h.getInValidProcessDefinition(), Lists.newArrayList("testProcess"));
            fail("testRedeployInvalidProcess() no Exception");
        } catch (DefinitionArchiveFormatException e) {
            // Expected.
        }
    }
}
