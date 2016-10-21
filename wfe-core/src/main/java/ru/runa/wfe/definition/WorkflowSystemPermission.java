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
import ru.runa.wfe.security.SystemPermission;

import com.google.common.collect.Lists;

/**
 * Created on 04.11.2004
 * 
 */
public class WorkflowSystemPermission extends SystemPermission {
    private static final long serialVersionUID = 9013104689547707534L;

    public static final Permission DEPLOY_DEFINITION = new WorkflowSystemPermission((byte) 4, "permission.deploy_definition");

    private static final List<Permission> SYSTEM_PERMISSIONS = fillPermissions();

    protected WorkflowSystemPermission(byte maskPower, String name) {
        super(maskPower, name);
    }

    public WorkflowSystemPermission() {
        super();
    }

    @Override
    public List<Permission> getAllPermissions() {
        return Lists.newArrayList(SYSTEM_PERMISSIONS);
    }

    private static List<Permission> fillPermissions() {
        List<Permission> superPermissions = new SystemPermission().getAllPermissions();
        List<Permission> result = Lists.newArrayList(superPermissions);
        result.add(DEPLOY_DEFINITION);
        return result;
    }
}
