package ru.runa.wfe.audit;

import com.google.common.base.Charsets;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.Transient;
import ru.runa.wfe.audit.presentation.HtmlValue;

@Entity
@DiscriminatorValue(value = "8")
public class ArchivedReceiveMessageLog extends ArchivedNodeEnterLog implements ReceiveMessageLog {

    @Override
    @Transient
    public Type getType() {
        return Type.RECEIVED_MESSAGE;
    }

    @Override
    @Transient
    public Object[] getPatternArguments() {
        String message = getBytes() != null ? new String(getBytes(), Charsets.UTF_8) : getAttribute(ATTR_MESSAGE);
        return new Object[] { new HtmlValue(message) };
    }

    @Override
    public void processBy(ProcessLogVisitor visitor) {
        visitor.onReceiveMessageLog(this);
    }
}
