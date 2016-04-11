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
package ru.runa.wfe.bot;

import ru.runa.wfe.InternalApplicationException;

/**
 * @author petr_mikheev
 */
public class BotStationAlreadyExistsException extends InternalApplicationException {
    private static final long serialVersionUID = -9186710256485510506L;
    private final String botstationName;

    public BotStationAlreadyExistsException(String botstationName) {
        super("BotStation '" + botstationName + "' already exists.");
        this.botstationName = botstationName;
    }

    public String getBotStationName() {
        return botstationName;
    }
}
