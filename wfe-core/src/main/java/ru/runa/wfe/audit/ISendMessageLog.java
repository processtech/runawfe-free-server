package ru.runa.wfe.audit;

import com.google.common.base.Charsets;
import javax.persistence.Transient;
import ru.runa.wfe.audit.presentation.HtmlValue;

public interface ISendMessageLog extends INodeEnterLog {

    @Override
    @Transient
    default Object[] getPatternArguments() {
        String message = getBytes() != null ? new String(getBytes(), Charsets.UTF_8) : getAttribute(ATTR_MESSAGE);
        return new Object[] { new HtmlValue(message) };
    }

    @Override
    default void processBy(ProcessLogVisitor visitor) {
        visitor.onSendMessageLog(this);
    }
}
