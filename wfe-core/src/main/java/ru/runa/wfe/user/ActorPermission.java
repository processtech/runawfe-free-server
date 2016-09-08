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
public class ActorPermission extends ExecutorPermission {

	private static final long serialVersionUID = 7900437485850107134L;

	public static final Permission UPDATE_STATUS = new ActorPermission((byte) 3, "permission.update_actor_status");
	public static final Permission READ_USER_TASKS = new ExecutorPermission((byte) 4, "permission.read_user_tasks");
	private static final List<Permission> PERMISSIONS = fillPermissions();

	public ActorPermission() {
		super();
	}

	protected ActorPermission(byte maskPower, String name) {
		super(maskPower, name);
	}

	@Override
	public List<Permission> getAllPermissions() {
		return Lists.newArrayList(PERMISSIONS);
	}

	private static List<Permission> fillPermissions() {
		List<Permission> superPermissions = new ExecutorPermission().getAllPermissions();
		List<Permission> result = Lists.newArrayList(superPermissions);
		result.add(UPDATE_STATUS);
		result.add(READ_USER_TASKS);
		return result;
	}

}
