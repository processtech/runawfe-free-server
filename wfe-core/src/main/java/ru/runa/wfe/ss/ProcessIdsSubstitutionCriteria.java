package ru.runa.wfe.ss;

import java.util.List;

import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;

import ru.runa.wfe.execution.ExecutionContext;
import ru.runa.wfe.task.Task;
import ru.runa.wfe.user.Actor;

import com.google.common.base.Function;
import com.google.common.base.Splitter;
import com.google.common.collect.Lists;

@Entity
@DiscriminatorValue(value = "processIds")
public class ProcessIdsSubstitutionCriteria extends SubstitutionCriteria {
    private static final long serialVersionUID = 1L;
    private List<Long> processIds;

    @Override
    public boolean isSatisfied(ExecutionContext executionContext, Task task, Actor asActor, Actor substitutorActor) {
        validate();
        return processIds.contains(executionContext.getProcess().getId());
    }

    @Override
    public void validate() {
        if (processIds == null) {
            processIds = Lists.transform(Splitter.on(",").trimResults().splitToList(getConfiguration()), new Function<String, Long>() {

                @Override
                public Long apply(String input) {
                    return Long.valueOf(input);
                }
            });
        }
    }

}
