package ru.runa.wf.web;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider;
import org.springframework.core.type.filter.AssignableTypeFilter;
import ru.runa.wfe.var.format.FileFormat;
import ru.runa.wfe.var.format.ListFormat;
import ru.runa.wfe.var.format.MapFormat;
import ru.runa.wfe.var.format.UserTypeFormat;
import ru.runa.wfe.var.format.VariableFormat;

/**
 * @author Denis B. Kulikov<br/>
 *         date: 17.04.2019:15:54<br/>
 */
public class SignalUtils {

    /**
     * Map of supported format
     */
    private static final Map<String, VariableFormat> formatMap = new HashMap<>();
    /**
     * Available options for select type of format
     */
    private static final List<String> options = new ArrayList<>();

    static {

        for (VariableFormat variableFormat : getSupportedFormats()) {
            formatMap.put(variableFormat.getName(), variableFormat);
            options.add(variableFormat.getName());
        }
    }

    public static Map<String, VariableFormat> getFormatMap() {
        return formatMap;
    }

    public static List<String> getOptions() {
        return options;
    }

    private static List<VariableFormat> getSupportedFormats() {

        final Set<Class<? extends VariableFormat>> excludeFormats = new HashSet<>();

        excludeFormats.add(FileFormat.class);
        excludeFormats.add(ListFormat.class);
        excludeFormats.add(MapFormat.class);
        excludeFormats.add(UserTypeFormat.class);

        ClassPathScanningCandidateComponentProvider provider = new ClassPathScanningCandidateComponentProvider(false);
        provider.addIncludeFilter(new AssignableTypeFilter(VariableFormat.class));
        List<VariableFormat> formats = new ArrayList<>();
        try {
            for (BeanDefinition bean : provider.findCandidateComponents("ru.runa.wfe.var.format")) {
                Class<? extends VariableFormat> type = (Class<? extends VariableFormat>) Class.forName(bean.getBeanClassName());
                if (!bean.isAbstract() && !excludeFormats.contains(type)) {
                    VariableFormat variableFormat = type.newInstance();
                    formats.add(variableFormat);
                }
            }
        } catch (ClassNotFoundException | IllegalAccessException | InstantiationException e) {
            throw new IllegalStateException("Impossible state, class not found", e);
        }

        return formats;
    }
}
