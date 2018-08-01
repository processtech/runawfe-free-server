package ru.runa.wfe.execution.logic;

import com.google.common.base.Throwables;
import com.google.common.collect.Maps;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import lombok.extern.apachecommons.CommonsLog;
import ru.runa.wfe.commons.ApplicationContextFactory;
import ru.runa.wfe.user.Executor;
import ru.runa.wfe.var.IVariableProvider;

/**
 * Parses and evaluates swimlane initializers. Swimlane initializer can be given in 2 forms:
 *
 * 1) FQDN class name(param1, param2, ...) for example 'ru.runa.af.organizationfunction.ExecutorByNameFunction(userName)'
 * 2) @[!]relationName(parameter) for example '@!boss(processVariableName)'
 *
 * @author Dofs
 * @since 4.0.5
 */
@CommonsLog
public class SwimlaneInitializerHelper {
    private static final Map<String, SwimlaneInitializer> CACHE = Maps.newConcurrentMap();

    public static SwimlaneInitializer parse(String swimlaneConfiguration) {
        if (!CACHE.containsKey(swimlaneConfiguration)) {
            SwimlaneInitializer swimlaneInitializer;
            if (RelationSwimlaneInitializer.isValid(swimlaneConfiguration)) {
                swimlaneInitializer = ApplicationContextFactory.autowireBean(new RelationSwimlaneInitializer());
            } else if (BotSwimlaneInitializer.isValid(swimlaneConfiguration)) {
                swimlaneInitializer = ApplicationContextFactory.autowireBean(new BotSwimlaneInitializer());
            } else {
                swimlaneInitializer = ApplicationContextFactory.autowireBean(new OrgFunctionSwimlaneInitializer());
            }
            swimlaneInitializer.parse(swimlaneConfiguration);
            CACHE.put(swimlaneConfiguration, swimlaneInitializer);
        }
        return CACHE.get(swimlaneConfiguration);
    }

    public static List<? extends Executor> evaluate(String swimlaneConfiguration, IVariableProvider variableProvider) {
        SwimlaneInitializer swimlaneInitializer = parse(swimlaneConfiguration);
        try {
            List<? extends Executor> result = swimlaneInitializer.evaluate(variableProvider);
            if (result == null) {
                result = new ArrayList<>();
            }
            return result;
        } catch (Exception e) {
            log.error(swimlaneInitializer);
            throw Throwables.propagate(e);
        }
    }
}
