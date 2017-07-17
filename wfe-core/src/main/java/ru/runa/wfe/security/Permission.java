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

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;

import com.google.common.base.Objects;
import com.google.common.collect.Lists;

import ru.runa.wfe.commons.SystemProperties;
import ru.runa.wfe.user.Executor;

/**
 * Class represents permissions on any {@link SecuredObject}. Every type of {@link SecuredObject} can own subclass of this class which represent set
 * of allowed permissions.
 */
@XmlAccessorType(XmlAccessType.FIELD)
public class Permission implements Serializable {
    private static final long serialVersionUID = -3672653529467591904L;
    public String DEFAULT_PERMISSIONS;
    /**
     * Read permission. Read permission usually allows read/get object.
     */
    public static final Permission READ = new Permission(0, "permission.read");
    /**
     * Update permission. Update permission usually allows change object state.
     */
    public static final Permission UPDATE_PERMISSIONS = new Permission(1, "permission.update_permissions");

    /**
     * Default executor permissions.
     * 
     * @return
     */
    public static final <T extends Executor> List<Permission> getDefaultPermissions(T executor) {
        List<Permission> defPermList = new ArrayList<>();
        Permission permission = executor.getSecuredObjectType().getNoPermission();

        List<String> defProperties = SystemProperties.getDefaultPermissions(permission.DEFAULT_PERMISSIONS);
        if (!defProperties.isEmpty()) {
            for (String prop : defProperties) {
                for (Permission p : permission.getAllPermissions()) {
                    if (p.getName().equals(prop)) {
                        defPermList.add(p);
                    }
                }
            }
        }

        return defPermList;
    }

    /**
     * All defined permissions.
     */
    private static final Permission[] ALL_PERMISSIONS = { READ, UPDATE_PERMISSIONS };

    /**
     * Permission name.
     */
    private final String name;

    /**
     * Permission mask. Every specific permission (Read, Update) has it's own bit position in this mask. If permission is granted, then permission bit
     * is set to 1; otherwise permission bit is set to 0.
     */
    private final long mask;

    /**
     * Create permission with specified name and permission bit position.
     * 
     * @param maskPower
     *            Bit position, where permission is stored in {@link #mask}.
     * @param name
     *            Permission name.
     */
    protected Permission(int maskPower, String name) {
        mask = getMask(maskPower);
        this.name = name;
    }

    /**
     * Creates permission with mask==0 i.e means NO_PERMISSION
     */
    public Permission() {
        mask = 0;
        name = null;
    }

    /**
     * Return permission mask. Every specific permission (Read, Update) has it's own bit position in this mask. If permission is granted, then
     * permission bit is set to 1; otherwise permission bit is set to 0.
     * 
     * @return Permission mask.
     */
    public long getMask() {
        return mask;
    }

    /**
     * Return permission name.
     * 
     * @return Permission name.
     */
    public String getName() {
        return name;
    }

    /**
     * Return permission with specified mask if found, throws {@link PermissionNotFoundException} otherwise.
     * 
     * @param mask
     *            Permission bit mask.
     * @return Permission with specified mask.
     * @throws PermissionNotFoundException
     *             Permission with specified mask not found.
     */
    public Permission getPermission(long mask) throws PermissionNotFoundException {
        List<Permission> permissions = getAllPermissions();
        for (Permission permission : permissions) {
            if (mask == permission.getMask()) {
                return permission;
            }
        }
        throw new PermissionNotFoundException("mask = " + mask);
    }

    /**
     * Returns an array of all permissions that executor may have on type of secured object this class represents. This method must be overridden in
     * subclass.
     */
    public List<Permission> getAllPermissions() {
        return Lists.newArrayList(ALL_PERMISSIONS);
    }

    /**
     * Returns permission array, which represents no permission on secured object.
     * 
     * @return Permission array.
     */
    public static List<Permission> getNoPermissions() {
        return Lists.newArrayList();
    }

    /**
     * Return permission with specified name or throws {@link PermissionNotFoundException} otherwise.
     * 
     * @param name
     *            Permission name
     * @return Permission with specified name.
     * @throws PermissionNotFoundException
     *             Permission with specified name was not found.
     */
    public Permission getPermission(String name) throws PermissionNotFoundException {
        for (Permission permission : getAllPermissions()) {
            if (permission.getName().equals(name)) {
                return permission;
            }
        }
        throw new PermissionNotFoundException(name);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof Permission) {
            Permission p = (Permission) obj;
            return Objects.equal(name, p.name) && mask == p.mask;
        }
        return super.equals(obj);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(name, mask);
    }

    @Override
    public String toString() {
        return Objects.toStringHelper(this).add("name", name).toString();
    }

    /**
     * Merge two permission arrays together into single permission array.
     * 
     * @param p1
     *            First permission array.
     * @param p2
     *            Second permission array.
     * @return Merged permission array.
     */
    public static Set<Permission> mergePermissions(Collection<Permission> p1, Collection<Permission> p2) {
        Set<Permission> set = new HashSet<Permission>();
        set.addAll(p1);
        set.addAll(p2);
        return set;
    }

    /**
     * Remove permissions from permission array.
     * 
     * @param p1
     *            Source permission array, from which permissions removed.
     * @param p2
     *            Permission array with permissions to remove.
     * @return Permission array
     */
    public static Set<Permission> subtractPermissions(Collection<Permission> p1, Collection<Permission> p2) {
        Set<Permission> set = new HashSet<Permission>();
        set.addAll(p1);
        set.removeAll(p2);
        return set;
    }

    /**
     * Create mask for specified bit position.
     * 
     * @param maskPower
     *            Bit position.
     * @return Bit-mask, contains 1 in bit position maskPower and 0 in other bits.
     */
    private static final long getMask(long maskPower) {
        return (long) Math.pow(2, maskPower);
    }
}
