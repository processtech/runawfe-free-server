package ru.runa.wf.logic.bot.mswordreport;

import ru.runa.wfe.commons.ClassLoaderUtil;
import ru.runa.wfe.commons.PropertyResources;
import ru.runa.wfe.var.VariableProvider;

/**
 * 
 * Created on 23.11.2006
 * 
 */
public class MsWordReportBuilderFactory {
    private static final PropertyResources RESOURCES = new PropertyResources("msword.report.properties");
    private static final String BUILDER_PROPERTY = "word.report.builder.class";

    public static MsWordReportBuilder createBuilder(MsWordReportTaskSettings settings, VariableProvider variableProvider) {
        String builderClassName = RESOURCES.getStringPropertyNotNull(BUILDER_PROPERTY);
        return (MsWordReportBuilder) ClassLoaderUtil.instantiate(builderClassName, settings, variableProvider);
    }

}
