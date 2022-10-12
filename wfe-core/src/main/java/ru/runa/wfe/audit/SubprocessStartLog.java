package ru.runa.wfe.audit;

import javax.persistence.Transient;

public interface SubprocessStartLog extends NodeLog {

    @Transient
    Long getSubprocessId();

    @Transient
    Long getParentTokenId();
}
