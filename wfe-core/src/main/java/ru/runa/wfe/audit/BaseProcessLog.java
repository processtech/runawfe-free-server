package ru.runa.wfe.audit;

import javax.persistence.MappedSuperclass;
import ru.runa.wfe.execution.BaseProcess;

@MappedSuperclass
public abstract class BaseProcessLog<P extends BaseProcess> {
}
