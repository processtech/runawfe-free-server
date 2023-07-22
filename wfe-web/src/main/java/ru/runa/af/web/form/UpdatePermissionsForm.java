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
package ru.runa.af.web.form;

import com.google.common.collect.Maps;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import javax.servlet.http.HttpServletRequest;
import org.apache.struts.action.ActionMapping;

/**
 * @struts:form name = "updatePermissionsForm"
 */
public class UpdatePermissionsForm extends GrantPermissionsForm {
    private static final long serialVersionUID = 1L;

    private final Map<Long, Permissions> executorPermissions = Maps.newHashMap();

    public Permissions getPermissions(Long executorId) {
        if (!executorPermissions.containsKey(executorId)) {
            return new Permissions();
        }
        return executorPermissions.get(executorId);
    }

    @Override
    public void reset(ActionMapping mapping, HttpServletRequest request) {
        super.reset(mapping, request);
        executorPermissions.clear();
    }

    /**
     * this method is used by Struts map backed forms
     */
    public Permissions getExecutor(String executorId) {
        Long id = Long.valueOf(executorId);
        Permissions permissions = executorPermissions.get(id);
        if (permissions == null) {
            permissions = new Permissions();
            executorPermissions.put(id, permissions);
        }
        return permissions;
    }

    public static class Permissions {
        private final Set<String> permissionNames = new HashSet<>();

        public Set<String> getPermissionNames() {
            return permissionNames;
        }

        /**
         * this method is used by Struts map backed forms
         */
        public void setPermission(String permissionName, Object value) {
            if (Objects.equals(value, "on")) {
                permissionNames.add(permissionName);
            }
        }
    }
}
