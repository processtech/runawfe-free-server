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

import java.util.Date;

import ru.runa.wfe.InternalApplicationException;

/**
 * Signals that process definition locked.
 */
public class DefinitionLockedException extends InternalApplicationException {
    private static final long serialVersionUID = 1L;
    private final String name;
    private final String userName;
    private final Date date;

    public DefinitionLockedException(String name, String userName, Date date) {
        super("Definition " + name + " locked by " + userName + " at " + date.toString() + ".");
        this.name = name;
        this.userName = userName;
        this.date = date;
    }

    public String getName() {
        return name;
    }

    public String getUserName() {
        return userName;
    }

    public Date getDate() {
        return date;
    }
}
