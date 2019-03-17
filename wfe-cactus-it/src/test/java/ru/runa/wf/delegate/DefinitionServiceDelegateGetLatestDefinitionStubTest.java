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

import org.apache.cactus.ServletTestCase;

import ru.runa.wf.service.WfServiceTestHelper;
import ru.runa.wfe.definition.DefinitionDoesNotExistException;
import ru.runa.wfe.definition.dto.WfDefinition;
import ru.runa.wfe.security.AuthenticationException;
import ru.runa.wfe.security.AuthorizationException;
import ru.runa.wfe.security.Permission;
import ru.runa.wfe.service.DefinitionService;
import ru.runa.wfe.service.delegate.Delegates;

import com.google.common.collect.Lists;

/**
 * Created on 20.04.2005
 * 
 * @author Gritsenko_S
 * 
 */
public class DefinitionServiceDelegateGetLatestDefinitionStubTest extends ServletTestCase {
    private DefinitionService definitionService;
    private WfServiceTestHelper helper = null;

    @Override
    protected void setUp() throws Exception {
        helper = new WfServiceTestHelper(getClass().getName());
        definitionService = Delegates.getDefinitionService();

        helper.deployValidProcessDefinition();

        Collection<Permission> permissions = Lists.newArrayList(Permission.READ);
        helper.setPermissionsToAuthorizedPerformerOnDefinitionByName(permissions, WfServiceTestHelper.VALID_PROCESS_NAME);

        super.setUp();
    }

    @Override
    protected void tearDown() throws Exception {
        helper.undeployValidProcessDefinition();

        helper.releaseResources();
        definitionService = null;
        super.tearDown();
    }

    public void testGetLatestDefinitionStubByAuthorizedUser() throws Exception {
        WfDefinition process = definitionService.getLatestProcessDefinition(helper.getAuthorizedPerformerUser(),
                WfServiceTestHelper.VALID_PROCESS_NAME);
        assertEquals("definitionDelegate.getLatestDefinitionStub() returned process with different name", process.getName(),
                WfServiceTestHelper.VALID_PROCESS_NAME);
    }

    public void testGetLatestDefinitionStubByUnauthorizedUser() throws Exception {
        try {
            definitionService.getLatestProcessDefinition(helper.getUnauthorizedPerformerUser(), WfServiceTestHelper.VALID_PROCESS_NAME);
            fail("testGetLatestDefinitionStubByUnauthorizedSubject, no AuthorizationException");
        } catch (AuthorizationException e) {
        }
    }

    public void testGetLatestDefinitionStubByFakeUser() throws Exception {
        try {
            definitionService.getLatestProcessDefinition(helper.getFakeUser(), WfServiceTestHelper.VALID_PROCESS_NAME);
            fail("testGetLatestDefinitionStubByUnauthorizedSubject, no AuthenticationException");
        } catch (AuthenticationException e) {
        }
    }

    public void testGetLatestDefinitionStubByAuthorizedSubjectWithInvalidProcessName() throws Exception {
        try {
            definitionService.getLatestProcessDefinition(helper.getAuthorizedPerformerUser(), "0_Invalid_Process_Name");
            fail("testGetLatestDefinitionStubByAuthorizedSubjectWithInvalidProcessName, no Exception");
        } catch (DefinitionDoesNotExistException e) {
        }
    }
}
