package ru.runa.wfe.audit;

import com.google.common.collect.Lists;
import java.util.List;
import javax.persistence.Transient;
import ru.runa.wfe.audit.presentation.ExecutorNameValue;

public interface IAdminActionLog extends IProcessLog {
    String ACTION_UPDATE_VARIABLES = "update_variables";
    String ACTION_UPGRADE_PROCESS_TO_NEXT_VERSION = "upgrade_to_next_version";
    String ACTION_UPGRADE_CURRENT_PROCESS_VERSION = "upgrade_current_process_version";
    String ACTION_UPGRADE_PROCESS_TO_VERSION = "upgrade_to_version";

    @Override
    @Transient
    default String getPatternName() {
        return getClass().getSimpleName() + "." + getAttributeNotNull(ATTR_ACTION);
    }

    @Override
    default Object[] getPatternArguments() {
        List<Object> arguments = Lists.newArrayList(new ExecutorNameValue(getAttributeNotNull(ATTR_ACTOR_NAME)));
        for (int i = 0; i < 10; i++) {
            String param = getAttribute(ATTR_PARAM + i);
            if (param != null) {
                arguments.add(param);
            } else {
                break;
            }
        }
        //noinspection ToArrayCallWithZeroLengthArrayArgument
        return arguments.toArray(new Object[arguments.size()]);
    }

    @Override
    default void processBy(ProcessLogVisitor visitor) {
        visitor.onAdminActionLog(this);
    }
}
