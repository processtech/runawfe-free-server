package ru.runa.wfe.audit;

import com.google.common.collect.Lists;
import java.util.List;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.Transient;
import ru.runa.wfe.audit.presentation.ExecutorNameValue;
import ru.runa.wfe.user.Actor;

@Entity
@DiscriminatorValue(value = "E")
public class CurrentAdminActionLog extends CurrentProcessLog implements AdminActionLog {
    private static final long serialVersionUID = 1L;

    public CurrentAdminActionLog() {
    }

    public CurrentAdminActionLog(Actor actor, String actionName, Object... data) {
        addAttribute(ATTR_ACTOR_NAME, actor.getName());
        addAttribute(ATTR_ACTION, actionName);
        if (data != null) {
            for (int i = 0; i < data.length; i++) {
                addAttribute(ATTR_PARAM + i, String.valueOf(data[i]));
            }
        }
        setSeverity(Severity.INFO);
    }

    @Override
    @Transient
    public Type getType() {
        return Type.ADMIN_ACTION;
    }

    @Override
    @Transient
    public String getPatternName() {
        return "AdminActionLog." + getAttributeNotNull(ATTR_ACTION);
    }

    @Override
    @Transient
    public Object[] getPatternArguments() {
        List<Object> result = Lists.newArrayList(new ExecutorNameValue(getAttributeNotNull(ATTR_ACTOR_NAME)));
        for (int i = 0; i < 10; i++) {
            String param = getAttribute(ATTR_PARAM + i);
            if (param != null) {
                result.add(param);
            } else {
                break;
            }
        }
        return result.toArray(new Object[result.size()]);
    }

    @Override
    public void processBy(ProcessLogVisitor visitor) {
        visitor.onAdminActionLog(this);
    }
}
