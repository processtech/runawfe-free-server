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
import ru.runa.wfe.definition.DefinitionArchiveFormatException;
import ru.runa.wfe.security.Permission;
import ru.runa.wfe.security.SecuredSingleton;
import ru.runa.wfe.service.DefinitionService;
import ru.runa.wfe.service.delegate.Delegates;

/**
 * Created on 20.04.2005
 * 
 * @author Gritsenko_S
 */
public class DefinitionServiceDelegateDeployProcessDefinitionTest extends ServletTestCase {
    private WfServiceTestHelper h;
    private DefinitionService definitionService;

    @Override
    protected void setUp() {
        h = new WfServiceTestHelper(getClass().getName());
        definitionService = Delegates.getDefinitionService();

        h.setPermissionsToAuthorizedActor(Lists.newArrayList(Permission.CREATE_DEFINITION), SecuredSingleton.SYSTEM);
    }

    @Override
    protected void tearDown() {
        h.releaseResources();
        definitionService = null;
    }

    public void testDeployInvalidProcessByAuthorizedUser() {
        try {
            definitionService.deployProcessDefinition(h.getAuthorizedUser(), h.getInValidProcessDefinition(), Lists.newArrayList("testProcess"));
            fail();
        } catch (DefinitionArchiveFormatException e) {
            // Expected.
        }
    }
}
