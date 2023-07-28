package ru.runa.wfe.audit;

import java.util.Date;

public interface CreateTimerLog extends NodeLog {

    public Date getDueDate();

}
