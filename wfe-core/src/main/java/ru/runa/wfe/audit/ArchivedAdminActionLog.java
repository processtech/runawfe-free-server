package ru.runa.wfe.audit;

import com.google.common.collect.Lists;
import java.util.List;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.Transient;
import ru.runa.wfe.audit.presentation.ExecutorNameValue;

@Entity
@DiscriminatorValue(value = "E")
public class ArchivedAdminActionLog extends ArchivedProcessLog implements AdminActionLog {

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
        //noinspection ToArrayCallWithZeroLengthArrayArgument
        return result.toArray(new Object[result.size()]);
    }

    @Override
    public void processBy(ProcessLogVisitor visitor) {
        visitor.onAdminActionLog(this);
    }
}
