package ru.runa.wfe.audit;

import java.util.List;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.Transient;

import ru.runa.wfe.audit.presentation.ExecutorNameValue;
import ru.runa.wfe.user.Actor;

import com.google.common.collect.Lists;

@Entity
@DiscriminatorValue(value = "E")
public class AdminActionLog extends ProcessLog {
    private static final long serialVersionUID = 1L;
    public static final String ACTION_UPDATE_VARIABLES = "update_variables";
    public static final String ACTION_UPGRADE_PROCESS_TO_NEXT_VERSION = "upgrade_to_next_version";
    public static final String ACTION_UPGRADE_CURRENT_PROCESS_VERSION = "upgrade_current_process_version";
    public static final String ACTION_UPGRADE_PROCESS_TO_VERSION = "upgrade_to_version";

    public AdminActionLog() {
    }

    public AdminActionLog(Actor actor, String actionName, Object... data) {
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
    public String getPatternName() {
        return super.getPatternName() + "." + getAttributeNotNull(ATTR_ACTION);
    }

    @Override
    @Transient
    public Object[] getPatternArguments() {
        List<Object> arguments = Lists.<Object> newArrayList(new ExecutorNameValue(getAttributeNotNull(ATTR_ACTOR_NAME)));
        for (int i = 0; i < 10; i++) {
            String param = getAttribute(ATTR_PARAM + i);
            if (param != null) {
                arguments.add(param);
            } else {
                break;
            }
        }
        return arguments.toArray(new Object[arguments.size()]);
    }

    @Override
    public void processBy(ProcessLogVisitor visitor) {
        visitor.onAdminActionLog(this);
    }
}
