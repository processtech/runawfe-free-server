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

import ru.runa.wfe.security.Permission;

import com.google.common.collect.Lists;

/**
 * Created on 10.09.2004
 */
public class ExecutorPermission extends Permission {
    private static final long serialVersionUID = -8626848851132307465L;

    public static final Permission UPDATE = new ExecutorPermission((byte) 2, "permission.update_executor");
    private static final List<Permission> EXECUTOR_PERMISSIONS = fillPermissions();

    protected ExecutorPermission(byte maskPower, String name) {
        super(maskPower, name);
    }

    public ExecutorPermission() {
        super();
    }

    @Override
    public List<Permission> getAllPermissions() {
        return Lists.newArrayList(EXECUTOR_PERMISSIONS);
    }

    private static List<Permission> fillPermissions() {
        List<Permission> superPermissions = new Permission().getAllPermissions();
        List<Permission> result = Lists.newArrayList(superPermissions);
        result.add(UPDATE);
        return result;
    }
}
