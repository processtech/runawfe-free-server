package ru.runa.wfe.audit;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.Transient;

@Entity
@DiscriminatorValue(value = "N")
public class ArchivedNodeEnterLog extends ArchivedNodeLog implements NodeEnterLog {

    @Override
    @Transient
    public Type getType() {
        return Type.NODE_ENTER;
    }

    @Override
    public void processBy(ProcessLogVisitor visitor) {
        visitor.onNodeEnterLog(this);
    }
}
