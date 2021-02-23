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
import org.apache.cactus.ServletTestCase;
import ru.runa.wf.service.WfServiceTestHelper;
import ru.runa.wfe.definition.dto.WfDefinition;
import ru.runa.wfe.security.AuthenticationException;
import ru.runa.wfe.security.AuthorizationException;
import ru.runa.wfe.security.Permission;
import ru.runa.wfe.service.DefinitionService;
import ru.runa.wfe.service.delegate.Delegates;

/**
 * Created on 20.04.2005
 * 
 * @author Gritsenko_S
 */
public class DefinitionServiceDelegateGetProcessDefinitionStubTest extends ServletTestCase {
    private WfServiceTestHelper h = null;
    private DefinitionService definitionService;

    @Override
    protected void setUp() {
        h = new WfServiceTestHelper(getClass().getName());
        definitionService = Delegates.getDefinitionService();

        h.deployValidProcessDefinition();
    }

    @Override
    protected void tearDown() {
        h.undeployValidProcessDefinition();
        h.releaseResources();
        definitionService = null;
    }

    public void testGetProcessDefinitionStubByAuthorizedUser() {
        Collection<Permission> permissions = Lists.newArrayList(Permission.READ);
        h.setPermissionsToAuthorizedActorOnDefinitionByName(permissions, WfServiceTestHelper.VALID_PROCESS_NAME);

        WfDefinition process = definitionService.getLatestProcessDefinition(h.getAuthorizedUser(),
                WfServiceTestHelper.VALID_PROCESS_NAME);
        long processId = process.getId();
        WfDefinition actualProcess = definitionService.getProcessDefinition(h.getAuthorizedUser(), processId);
        assertEquals("definitionDelegate.getLatestDefinitionStub returns different process by the same processId", process.getName(),
                actualProcess.getName());
        assertEquals("definitionDelegate.getLatestDefinitionStub returns different process by the same processId", process.getId(),
                actualProcess.getId());
        assertEquals("definitionDelegate.getLatestDefinitionStub returns different process by the same processId", process.getId(),
                actualProcess.getId());
    }

    public void testGetProcessDefinitionStubByAuthorizedUserWithoutREADPermission() {
        Collection<Permission> permissions = Lists.newArrayList();
        h.setPermissionsToAuthorizedActorOnDefinitionByName(permissions, WfServiceTestHelper.VALID_PROCESS_NAME);
        try {
            definitionService.getLatestProcessDefinition(h.getAuthorizedUser(), WfServiceTestHelper.VALID_PROCESS_NAME);
            fail();
        } catch (AuthorizationException e) {
            // Expected.
        }
    }

    public void testGetProcessDefinitionStubByUnauthorizedUser() {
        try {
            definitionService.getLatestProcessDefinition(h.getUnauthorizedUser(), WfServiceTestHelper.VALID_PROCESS_NAME);
            fail();
        } catch (AuthorizationException e) {
            // Expected.
        }
    }

    public void testGetProcessDefinitionStubByFakeUser() {
        try {
            definitionService.getLatestProcessDefinition(h.getFakeUser(), WfServiceTestHelper.VALID_PROCESS_NAME);
            fail();
        } catch (AuthenticationException e) {
            // Expected.
        }
    }
}
