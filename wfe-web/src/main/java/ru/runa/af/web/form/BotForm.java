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

import org.apache.struts.action.ActionForm;

/**
 * @author: petrmikheev
 * 
 * @struts:form name = "botForm"
 */
public class BotForm extends ActionForm {
    private static final long serialVersionUID = -5742270733607674118L;

    public static final String USER_NAME = "wfeUser";
    public static final String PASSWORD = "wfePassword";
    public static final String BOT_STATION_ID = "botStationId";
    public static final String BOT_ID = "botId";
    public static final String BOT_TIMEOUT = "botTimeout";
    public static final String IS_SEQUENTIAL = "sequential";
    public static final String IS_TRANSACTIONAL = "transactional";

    private String wfeUser;
    private String wfePassword;
    private Long botId;
    private Long botStationId;
    private Long botTimeout;
    private boolean sequential;
    private boolean transactional;

    public String getWfeUser() {
        return wfeUser;
    }

    public void setWfeUser(String botName) {
        wfeUser = botName;
    }

    public String getWfePassword() {
        return wfePassword;
    }

    public void setWfePassword(String botPassword) {
        wfePassword = botPassword;
    }

    public Long getBotStationId() {
        return botStationId;
    }

    public void setBotStationId(Long botStationId) {
        this.botStationId = botStationId;
    }

    public Long getBotId() {
        return botId;
    }

    public void setBotId(Long botId) {
        this.botId = botId;
    }

    public boolean isSequential() {
        return sequential;
    }

    public void setSequential(boolean sequential) {
        this.sequential = sequential;
    }

    public boolean isTransactional() {
        return transactional;
    }

    public void setTransactional(boolean transactional) {
        this.transactional = transactional;
    }

    public Long getBotTimeout() {
        return botTimeout;
    }

    public void setBotTimeout(Long botTimeout) {
        this.botTimeout = botTimeout;
    }

}
