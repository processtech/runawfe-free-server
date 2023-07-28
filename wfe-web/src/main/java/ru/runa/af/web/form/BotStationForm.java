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
