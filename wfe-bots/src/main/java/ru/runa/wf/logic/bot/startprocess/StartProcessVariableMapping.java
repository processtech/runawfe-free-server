package ru.runa.wf.logic.bot.startprocess;

/**
 * by gavrusev_sergei
 * 
 * @since 2.0
 */
public class StartProcessVariableMapping {
    private final String from;
    private final String to;

    public StartProcessVariableMapping(String from, String to) {
        this.from = from;
        this.to = to;
    }

    public String getFromName() {
        return from;
    }

    public String getToName() {
        return to;
    }
}
