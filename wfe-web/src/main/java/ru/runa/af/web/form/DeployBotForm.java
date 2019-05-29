package ru.runa.af.web.form;

import ru.runa.common.web.form.FileForm;

/**
 * @author: petrmikheev
 * 
 * @struts:form name = "deployBotForm"
 */
public class DeployBotForm extends FileForm {
    private static final long serialVersionUID = -6778295273263121816L;
    public static final String BOT_STATION_ID = "botStationId";
    public static final String REPLACE_OPTION_NAME = "replace";
    private boolean replace;
    private Long botStationId;

    public boolean isReplace() {
        return replace;
    }

    public void setReplace(boolean replace) {
        this.replace = replace;
    }

    public Long getBotStationId() {
        return botStationId;
    }

    public void setBotStationId(Long botStationId) {
        this.botStationId = botStationId;
    }
}
