package ru.runa.wfe.audit;

import javax.persistence.Transient;

public interface SubprocessStartLog extends NodeEnterLog {

    @Transient
    Long getSubprocessId();

    @Transient
    Long getParentTokenId();
}
