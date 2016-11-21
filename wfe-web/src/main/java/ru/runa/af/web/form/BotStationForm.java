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
 * User: stanley Date: 08.06.2008 Time: 13:48:27
 * 
 * @struts:form name = "botStationForm"
 */
public class BotStationForm extends ActionForm {
    private static final long serialVersionUID = -1273759004172153685L;

    public static final String BOT_STATION_NAME = "botStationName";
    public static final String BOT_STATION_RMI_ADDRESS = "botStationRMIAddress";
    public static final String BOT_STATION_ID = "botStationId";

    private Long botStationId;

    private String botStationName;

    private String botStationRMIAddress;

    public String getBotStationName() {
        return botStationName;
    }

    public void setBotStationName(String botStationName) {
        this.botStationName = botStationName;
    }

    public String getBotStationRMIAddress() {
        return botStationRMIAddress;
    }

    public void setBotStationRMIAddress(String botStationRMIAddress) {
        this.botStationRMIAddress = botStationRMIAddress;
    }

    public Long getBotStationId() {
        return botStationId;
    }

    public void setBotStationId(Long botStationId) {
        this.botStationId = botStationId;
    }

}
