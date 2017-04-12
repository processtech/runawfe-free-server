package ru.runa.wfe.commons.bc;

public enum DurationEnum {
    seconds("label.time_scale.second"),
    minutes("label.time_scale.minute"),
    hours("label.time_scale.hour"),
    days("label.time_scale.day"),
    weeks("label.time_scale.week"),
    months("label.time_scale.month"),
    years("label.time_scale.year");

    private String messageKey;

    private DurationEnum(String messageKey) {
        this.messageKey = messageKey;
    }

    public String getMessageKey() {
        return messageKey;
    }
}
