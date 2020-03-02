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

/*
 * Created on 29.11.2005
 */
package ru.runa.af.organizationfunction.impl;

import com.google.common.collect.Lists;
import java.util.List;
import org.apache.cactus.ServletTestCase;
import ru.runa.wf.service.WfServiceTestHelper;
import ru.runa.wfe.security.Permission;
import ru.runa.wfe.task.dto.WfTask;

/**
 * @author Gritsenko_S
 */
public class OrganizationFunctionParserTest extends ServletTestCase {
    private static final String PROCESS_PATH = "organizationProcess.par";
    private static final String PROCESS_NAME = "organizationProcess";

    private WfServiceTestHelper h;

    @Override
    protected void setUp() {
        h = new WfServiceTestHelper(getClass().getName());

        h.addExecutorToGroup(h.getHrOperator(), h.getBossGroup());
        h.deployValidProcessDefinition(PROCESS_PATH);
        h.setPermissionsToAuthorizedActorOnDefinitionByName(Lists.newArrayList(Permission.START_PROCESS), PROCESS_NAME);
    }

    @Override
    protected void tearDown() {
        h.undeployValidProcessDefinition(PROCESS_NAME);
        h.releaseResources();
        h = null;
    }

    public void testOrganizationFunction() {
        h.getExecutionService().startProcess(h.getAuthorizedUser(), PROCESS_NAME, null);

        List<WfTask> tasks = h.getTaskService().getMyTasks(h.getAdminUser(), h.getTaskBatchPresentation());
        assertEquals("tasks count differs from expected", 0, tasks.size());
    }
}
