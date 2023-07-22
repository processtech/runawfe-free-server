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
import org.apache.cactus.ServletTestCase;
import ru.runa.wf.service.WfServiceTestHelper;
import ru.runa.wfe.definition.DefinitionDoesNotExistException;
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
 * 
 */
public class DefinitionServiceDelegateGetLatestDefinitionStubTest extends ServletTestCase {
    private WfServiceTestHelper h;
    private DefinitionService definitionService;

    @Override
    protected void setUp() {
        h = new WfServiceTestHelper(getClass().getName());
        definitionService = Delegates.getDefinitionService();

        h.deployValidProcessDefinition();
        h.setPermissionsToAuthorizedActorOnDefinitionByName(Lists.newArrayList(Permission.READ), WfServiceTestHelper.VALID_PROCESS_NAME);
    }

    @Override
    protected void tearDown() {
        h.undeployValidProcessDefinition();
        h.releaseResources();
        definitionService = null;
    }

    public void testGetLatestDefinitionStubByAuthorizedUser() {
        WfDefinition process = definitionService.getLatestProcessDefinition(h.getAuthorizedUser(),
                WfServiceTestHelper.VALID_PROCESS_NAME);
        assertEquals("definitionDelegate.getLatestDefinitionStub() returned process with different name", process.getName(),
                WfServiceTestHelper.VALID_PROCESS_NAME);
    }

    public void testGetLatestDefinitionStubByUnauthorizedUser() {
        try {
            definitionService.getLatestProcessDefinition(h.getUnauthorizedUser(), WfServiceTestHelper.VALID_PROCESS_NAME);
            fail();
        } catch (AuthorizationException e) {
            // Expected.
        }
    }

    public void testGetLatestDefinitionStubByFakeUser() {
        try {
            definitionService.getLatestProcessDefinition(h.getFakeUser(), WfServiceTestHelper.VALID_PROCESS_NAME);
            fail();
        } catch (AuthenticationException e) {
            // Expected.
        }
    }

    public void testGetLatestDefinitionStubByAuthorizedUserWithInvalidProcessName() {
        try {
            definitionService.getLatestProcessDefinition(h.getAuthorizedUser(), "0_Invalid_Process_Name");
            fail();
        } catch (DefinitionDoesNotExistException e) {
            // Expected.
        }
    }
}
