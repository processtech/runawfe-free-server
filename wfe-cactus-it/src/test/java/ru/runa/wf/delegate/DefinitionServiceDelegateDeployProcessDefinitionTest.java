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
import junit.framework.Test;
import junit.framework.TestSuite;
import org.apache.cactus.ServletTestCase;
import ru.runa.wf.service.WfServiceTestHelper;
import ru.runa.wfe.definition.DefinitionArchiveFormatException;
import ru.runa.wfe.security.Permission;
import ru.runa.wfe.service.DefinitionService;
import ru.runa.wfe.service.delegate.Delegates;

import java.util.Collection;

/**
 * Created on 20.04.2005
 * 
 * @author Gritsenko_S
 */
public class DefinitionServiceDelegateDeployProcessDefinitionTest extends ServletTestCase {

    private DefinitionService definitionService = null;

    private WfServiceTestHelper helper = null;

    @Override
    protected void setUp() throws Exception {
        helper = new WfServiceTestHelper(getClass().getName());
        definitionService = Delegates.getDefinitionService();

        Collection<Permission> deployPermissions = Lists.newArrayList(Permission.CREATE);
        helper.setPermissionsToAuthorizedPerformerOnDefinitions(deployPermissions);

        super.setUp();
    }

    @Override
    protected void tearDown() throws Exception {
        helper.releaseResources();
        definitionService = null;
        super.tearDown();
    }

    public void testDeployInvalidProcessByAuthorizedPerformer() throws Exception {
        try {
            definitionService.deployProcessDefinition(helper.getAuthorizedPerformerUser(), helper.getInValidProcessDefinition(),
                    Lists.newArrayList("testProcess"));
            assertTrue("definitionDelegate.deployProcessByAuthorizedPerformer() no DefinitionArchiveFormatException", false);
        } catch (DefinitionArchiveFormatException e) {
            // That's what we expect
        }
    }
}
