package ru.runa.wfe.audit;

public interface TaskEndLog extends TaskLog {

    String getActorName();

    String getTransitionName();

}
