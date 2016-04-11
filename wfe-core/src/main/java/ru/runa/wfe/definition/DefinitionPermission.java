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
package ru.runa.wfe.definition;

import java.util.List;

import ru.runa.wfe.security.Permission;

import com.google.common.collect.Lists;

/**
 * Represents Permissions on a {@link ru.runa.wfe.definition.dto.WfDefinition}
 * Created on 21.09.2004
 */
public class DefinitionPermission extends Permission {
    private static final long serialVersionUID = 4981030703856496613L;

    public static final Permission REDEPLOY_DEFINITION = new DefinitionPermission((byte) 2, "permission.redeploy_definition");
    public static final Permission UNDEPLOY_DEFINITION = new DefinitionPermission((byte) 3, "permission.undeploy_definition");
    public static final Permission START_PROCESS = new DefinitionPermission((byte) 4, "permission.start_process");

    /**
     * permission would be given to executors on created process
     */
    public static final Permission READ_STARTED_PROCESS = new DefinitionPermission((byte) 5, "permission.read_process");
    /**
     * permission would be given to executors on created process
     */
    public static final Permission CANCEL_STARTED_PROCESS = new DefinitionPermission((byte) 6, "permission.cancel_process");

    private static final List<Permission> ALL_PERMISSIONS = fillPermissions();

    protected DefinitionPermission(byte maskPower, String name) {
        super(maskPower, name);
    }

    public DefinitionPermission() {
        super();
    }

    @Override
    public List<Permission> getAllPermissions() {
        return Lists.newArrayList(ALL_PERMISSIONS);
    }

    private static List<Permission> fillPermissions() {
        List<Permission> superPermissions = new Permission().getAllPermissions();
        List<Permission> result = Lists.newArrayList(superPermissions);
        result.add(REDEPLOY_DEFINITION);
        result.add(UNDEPLOY_DEFINITION);
        result.add(START_PROCESS);
        result.add(READ_STARTED_PROCESS);
        result.add(CANCEL_STARTED_PROCESS);
        return result;
    }

}
