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
package ru.runa.wfe.user;

import java.util.List;

import com.google.common.collect.Lists;

import ru.runa.wfe.security.Permission;

/**
 * Created on 10.09.2004
 * 
 */
public class GroupPermission extends ExecutorPermission {
    private static final long serialVersionUID = 8101858243353378772L;

    public static final Permission LIST_GROUP = new GroupPermission((byte) 3, "permission.list_group");
    public static final Permission ADD_TO_GROUP = new GroupPermission((byte) 4, "permission.add_to_group");
    public static final Permission REMOVE_FROM_GROUP = new GroupPermission((byte) 5, "permission.remove_from_group");
    public static final Permission VIEW_TASKS = new GroupPermission((byte) 6, "permission.view_group_tasks");

    private static final List<Permission> GROUP_PERMISSIONS = fillPermissions();

    {
        DEFAULT_PERMISSIONS = "group.default.permissions";
    }

    private GroupPermission(byte maskPower, String name) {
        super(maskPower, name);
    }

    public GroupPermission() {
        super();
    }

    @Override
    public List<Permission> getAllPermissions() {
        return Lists.newArrayList(GROUP_PERMISSIONS);
    }

    private static List<Permission> fillPermissions() {
        List<Permission> superPermissions = new ExecutorPermission().getAllPermissions();
        List<Permission> result = Lists.newArrayList(superPermissions);
        result.add(LIST_GROUP);
        result.add(ADD_TO_GROUP);
        result.add(REMOVE_FROM_GROUP);
        result.add(VIEW_TASKS);
        return result;
    }
}
