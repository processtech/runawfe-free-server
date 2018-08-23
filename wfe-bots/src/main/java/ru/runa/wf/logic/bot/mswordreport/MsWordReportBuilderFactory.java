/*
 * This file is part of the RUNA WFE project.
 * 
 * This program is free software; you can redistribute it and/or 
 * modify it under the terms of the GNU Lesser General Public License 
 * as published by the Free Software Foundation; version 2.1 
 * of the License. 
 * 
 * This program is distributed in the hope that it will be useful, 
 * but WITHOUT ANY WARRANTY; without even the implied warranty of 
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the 
 * GNU Lesser General Public License for more details. 
 * 
 * You should have received a copy of the GNU Lesser General Public License 
 * along with this program; if not, write to the Free Software 
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA.
 */
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
