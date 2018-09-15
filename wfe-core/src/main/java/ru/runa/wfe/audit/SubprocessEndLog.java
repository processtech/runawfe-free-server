package ru.runa.wfe.audit;

import javax.persistence.Transient;

public interface SubprocessEndLog extends NodeLeaveLog {

    @Transient
    Long getSubprocessId();

    @Transient
    Long getParentTokenId();
}
