package ru.runa.wfe.audit;

import com.google.common.base.Charsets;
import javax.persistence.MappedSuperclass;
import javax.persistence.Transient;
import ru.runa.wfe.audit.presentation.HtmlValue;
import ru.runa.wfe.lang.BaseMessageNode;

@MappedSuperclass
public abstract class CurrentMessageNodeLog extends CurrentNodeLog implements MessageNodeLog {
    private static final long serialVersionUID = 1L;

    public CurrentMessageNodeLog() {

    }

    public CurrentMessageNodeLog(BaseMessageNode node, String message) {
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
