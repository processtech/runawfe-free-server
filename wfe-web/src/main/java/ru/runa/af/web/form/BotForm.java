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
    public static final String TRANSACTIONAL_TIMEOUT = "transactionalTimeout";
    public static final String IS_SEQUENTIAL = "sequential";
    public static final String IS_TRANSACTIONAL = "transactional";

    private String wfeUser;
    private String wfePassword;
    private Long botId;
    private Long botStationId;
    private Long transactionalTimeout;
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

    public Long getTransactionalTimeout() {
        return transactionalTimeout;
    }

    public void setTransactionalTimeout(Long transactionalTimeout) {
        this.transactionalTimeout = transactionalTimeout;
    }

}
