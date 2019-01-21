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
 */
public class DefinitionServiceDelegateUndeployProcessDefinitionTest extends ServletTestCase {

    private DefinitionService definitionService;

    private WfServiceTestHelper helper = null;

    @Override
    protected void setUp() throws Exception {
        helper = new WfServiceTestHelper(getClass().getName());
        definitionService = Delegates.getDefinitionService();

        helper.deployValidProcessDefinition();

        Collection<Permission> undeployPermissions = Lists.newArrayList(Permission.ALL);
        helper.setPermissionsToAuthorizedPerformerOnExecutors(undeployPermissions);
        
        super.setUp();
    }

    @Override
    protected void tearDown() throws Exception {
        helper.releaseResources();
        definitionService = null;
        super.tearDown();
    }

    public void testUndeployProcessByAuthorizedPerformer() throws Exception {
        definitionService.undeployProcessDefinition(helper.getAuthorizedPerformerUser(), WfServiceTestHelper.VALID_PROCESS_NAME, null);
        List<WfDefinition> deployedProcesses = definitionService.getProcessDefinitions(helper.getAuthorizedPerformerUser(),
                helper.getProcessDefinitionBatchPresentation(), false);

        if (deployedProcesses.size() != 0) {
            fail("testUndeployProcessByAuthorizedPerformer wrongNumberOfProcessDefinitions after undeployment");
        }

        try {
            definitionService.undeployProcessDefinition(helper.getAuthorizedPerformerUser(), WfServiceTestHelper.VALID_PROCESS_NAME, null);
            fail("testUndeployProcessByAuthorizedPerformer allows undeploy process definition after undeployment");
        } catch (DefinitionDoesNotExistException e) {
        }
    }

    public void testUndeployProcessByAuthorizedPerformerWithoutUNDEPLOYPermission() throws Exception {
        try {
            Collection<Permission> undeployPermissions = Lists.newArrayList();
            helper.setPermissionsToAuthorizedPerformerOnDefinitionByName(undeployPermissions, WfServiceTestHelper.VALID_PROCESS_NAME);

            try {
                definitionService.undeployProcessDefinition(helper.getAuthorizedPerformerUser(), WfServiceTestHelper.VALID_PROCESS_NAME, null);
                fail("testUndeployProcessByAuthorizedPerformerWithoutUNDEPLOYPermission, no AuthorizationException");
            } catch (AuthorizationException e1) {
            }
        } finally {
            helper.undeployValidProcessDefinition();
        }
    }

    public void testUndeployProcessByUnauthorizedPerformer() throws Exception {
        try {
            definitionService.undeployProcessDefinition(helper.getUnauthorizedPerformerUser(), WfServiceTestHelper.VALID_PROCESS_NAME, null);
            fail("testUndeployProcessByUnauthorizedPerformer, no AuthorizationException");
        } catch (AuthorizationException e1) {
        } finally {
            helper.undeployValidProcessDefinition();
        }
    }

    public void testUndeployProcessByFakePerformer() throws Exception {
        try {
            definitionService.undeployProcessDefinition(helper.getFakeUser(), WfServiceTestHelper.VALID_PROCESS_NAME, null);
            fail("testUndeployProcessByFakePerformer, no AuthenticationException");
        } catch (AuthenticationException e1) {
        } finally {
            helper.undeployValidProcessDefinition();
        }
    }

    public void testUndeployProcessByNullPerformer() throws Exception {
        try {
            definitionService.undeployProcessDefinition(null, WfServiceTestHelper.VALID_PROCESS_NAME, null);
            fail("testUndeployProcessByNullPerformer, no IllegalArgumentException");
        } catch (IllegalArgumentException e1) {
        } finally {
            helper.undeployValidProcessDefinition();
        }

    }

    public void testUndeployProcessWithNullProcessName() throws Exception {
        try {
            definitionService.undeployProcessDefinition(helper.getUnauthorizedPerformerUser(), null, null);
            fail("testUndeployProcessWithNullProcessName allows undeploy process definition with NULL name");
        } catch (IllegalArgumentException e) {
        } finally {
            helper.undeployValidProcessDefinition();
        }
    }

    public void testUndeployProcessWithUnexistentProcessName() throws Exception {
        try {
            definitionService.undeployProcessDefinition(helper.getUnauthorizedPerformerUser(), "Unexistent_Process_definition_Name_000", null);
            fail("testUndeployProcessWithNullProcessName allows undeploy process definition with unexistent name");
        } catch (DefinitionDoesNotExistException e) {
        } finally {
            helper.undeployValidProcessDefinition();
        }

    }
}
