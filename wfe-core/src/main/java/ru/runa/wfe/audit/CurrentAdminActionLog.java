package ru.runa.wfe.audit;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
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
}
