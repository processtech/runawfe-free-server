package ru.runa.wfe.audit;

import com.google.common.base.Charsets;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.Transient;
import ru.runa.wfe.audit.presentation.HtmlValue;
import ru.runa.wfe.lang.BaseMessageNode;

/**
 * Base class for message nodes.
 *
 * @author Sergey Inyakin
 */
@Entity
@DiscriminatorValue(value = "0")
public abstract class MessageNodeLog extends NodeLog {

    private static final long serialVersionUID = -3692025459960979115L;

    public MessageNodeLog() {
    }

    public MessageNodeLog(BaseMessageNode node, String message) {
        super(node);
        if (message.length() < 1000) {
            addAttribute(ATTR_MESSAGE, message);
        } else {
            setBytes(message.getBytes(Charsets.UTF_8));
        }
    }

    @Override
    @Transient
    public Object[] getPatternArguments() {
        String message = getBytes() != null ? new String(getBytes(), Charsets.UTF_8) : getAttribute(ATTR_MESSAGE);
        return new Object[] { new HtmlValue(message) };
    }

}
