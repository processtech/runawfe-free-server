package ru.runa.wfe.audit;

import com.google.common.base.Charsets;
import javax.persistence.MappedSuperclass;
import javax.persistence.Transient;
import ru.runa.wfe.audit.presentation.HtmlValue;

@MappedSuperclass
public abstract class ArchivedMessageNodeLog extends ArchivedNodeLog implements MessageNodeLog {

    @Override
    @Transient
    public Object[] getPatternArguments() {
        String message = getBytes() != null ? new String(getBytes(), Charsets.UTF_8) : getAttribute(ATTR_MESSAGE);
        return new Object[] { new HtmlValue(message) };
    }

}
