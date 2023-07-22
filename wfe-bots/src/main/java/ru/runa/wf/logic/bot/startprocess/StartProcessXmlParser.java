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
package ru.runa.wf.logic.bot.startprocess;

import java.util.List;

import org.dom4j.Document;
import org.dom4j.Element;

import ru.runa.wfe.commons.xml.XmlUtils;

import com.google.common.collect.Lists;

public class StartProcessXmlParser {
    private static final String PROCESS_ELEMENT_NAME = "process";
    private static final String TITLE_ATTRIBUTE_NAME = "name";
    private static final String VARIABLE_ELEMENT_NAME = "variable";
    private static final String VARFROM_ATTRIBUTE_NAME = "from";
    private static final String VARTO_ATTRIBUTE_NAME = "to";
    private static final String STARTED_PROCESS_ID = "started-process-id";
    private static final String VARIABLE_NAME = "variable-name";

    /**
     * Parses StartProcessXmlHandler configuration
     */
    public static List<StartProcessTask> parse(String configuration) {
        List<StartProcessTask> startProcessTasks = Lists.newArrayList();
        Document document = XmlUtils.parseWithXSDValidation(configuration, "process-start.xsd");
        List<Element> processElements = document.getRootElement().elements(PROCESS_ELEMENT_NAME);
        for (Element processElement : processElements) {
            String title = processElement.attributeValue(TITLE_ATTRIBUTE_NAME);
            StartProcessVariableMapping[] variables = parseProcessStartProcessVariableMappings(processElement);
            String startedProcessId = getStartedProcessIdName(processElement);
            startProcessTasks.add(new StartProcessTask(title, variables, startedProcessId));
        }
        return startProcessTasks;
    }

    private static String getStartedProcessIdName(Element processElement) {
        Element spIdNode = processElement.element(STARTED_PROCESS_ID);
        String startedProcessId = null;
        if (spIdNode != null) {
            startedProcessId = spIdNode.attributeValue(VARIABLE_NAME);
        }
        return startedProcessId;
    }

    private static StartProcessVariableMapping[] parseProcessStartProcessVariableMappings(Element processElement) {
        List<StartProcessVariableMapping> variables = Lists.newArrayList();
        List<Element> variableElements = processElement.elements(VARIABLE_ELEMENT_NAME);
        for (Element variableElement : variableElements) {
            String varFrom = variableElement.attributeValue(VARFROM_ATTRIBUTE_NAME);
            String varTo = variableElement.attributeValue(VARTO_ATTRIBUTE_NAME);
            variables.add(new StartProcessVariableMapping(varFrom, varTo));
        }
        return variables.toArray(new StartProcessVariableMapping[variables.size()]);
    }
}
