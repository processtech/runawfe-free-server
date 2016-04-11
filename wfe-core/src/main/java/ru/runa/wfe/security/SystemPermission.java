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
package ru.runa.wfe.security;

import java.util.List;

import com.google.common.collect.Lists;

public class SystemPermission extends Permission {
    private static final long serialVersionUID = 1L;

    public static final Permission LOGIN_TO_SYSTEM = new SystemPermission((byte) 2, "permission.login_to_system");
    public static final Permission CREATE_EXECUTOR = new SystemPermission((byte) 3, "permission.create_executor");
    public static final Permission CHANGE_SELF_PASSWORD = new SystemPermission((byte) 5, "permission.change_self_password");
    public static final Permission VIEW_LOGS = new SystemPermission((byte) 7, "permission.view_logs");
    private static final List<Permission> SYSTEM_PERMISSIONS = fillPermissions();

    public SystemPermission(byte maskPower, String name) {
        super(maskPower, name);
    }

    public SystemPermission() {
        super();
    }

    @Override
    public List<Permission> getAllPermissions() {
        return Lists.newArrayList(SYSTEM_PERMISSIONS);
    }

    private static List<Permission> fillPermissions() {
        List<Permission> superPermissions = new Permission().getAllPermissions();
        List<Permission> result = Lists.newArrayList(superPermissions);
        result.add(LOGIN_TO_SYSTEM);
        result.add(CREATE_EXECUTOR);
        result.add(CHANGE_SELF_PASSWORD);
        result.add(VIEW_LOGS);
        return result;
    }

}
