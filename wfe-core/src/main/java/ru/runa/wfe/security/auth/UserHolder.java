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
package ru.runa.wfe.security.auth;

import ru.runa.wfe.user.User;

/**
 * This class holds user from EJB call and should be used only in action
 * handlers in extravagant cases.
 * 
 * @author dofs
 * @since 4.0
 */
public class UserHolder {
    private static ThreadLocal<User> users = new ThreadLocal<User>();

    public static void set(User user) {
        users.set(user);
    }

    public static User get() {
        return users.get();
    }

    public static void reset() {
        users.remove();
    }
}
