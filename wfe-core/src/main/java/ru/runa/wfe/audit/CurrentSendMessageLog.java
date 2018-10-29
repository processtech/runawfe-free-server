package ru.runa.wfe.audit;

import com.google.common.base.Charsets;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.Transient;
import ru.runa.wfe.audit.presentation.HtmlValue;
import ru.runa.wfe.lang.Node;

/**
 * Logging message nodes execution.
 * 
 * @author Dofs
 */
@Entity
@DiscriminatorValue(value = "7")
public class CurrentSendMessageLog extends CurrentNodeEnterLog implements SendMessageLog {
    private static final long serialVersionUID = 1L;

    public CurrentSendMessageLog() {
    }

    public CurrentSendMessageLog(Node node, String message) {
        super(node);
        if (message.length() < 1000) {
            addAttribute(ATTR_MESSAGE, message);
        } else {
            setBytes(message.getBytes(Charsets.UTF_8));
        }
    }

    @Override
    @Transient
    public Type getType() {
        return Type.SEND_MESSAGE;
    }

    @Override
    @Transient
    public Object[] getPatternArguments() {
        String message = getBytes() != null ? new String(getBytes(), Charsets.UTF_8) : getAttribute(ATTR_MESSAGE);
        return new Object[] { new HtmlValue(message) };
    }

    @Override
    public void processBy(ProcessLogVisitor visitor) {
        visitor.onSendMessageLog(this);
    }
}
