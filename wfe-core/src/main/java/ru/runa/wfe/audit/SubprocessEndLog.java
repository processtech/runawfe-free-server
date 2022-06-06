package ru.runa.wfe.audit;

import javax.persistence.Transient;

public interface SubprocessEndLog extends NodeLog {

    @Transient
    Long getSubprocessId();

    @Transient
    Long getParentTokenId();
}
