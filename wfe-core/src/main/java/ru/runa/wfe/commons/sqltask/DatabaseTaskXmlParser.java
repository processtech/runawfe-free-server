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
package ru.runa.wfe.commons.sqltask;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.dom4j.Document;
import org.dom4j.Element;

import ru.runa.wfe.commons.xml.XmlUtils;
import ru.runa.wfe.var.VariableProvider;

import com.google.common.base.Strings;

public class DatabaseTaskXmlParser {
    private static final String TASK_ELEMENT_NAME = "task";
    private static final String DATASOURCE_ATTRIBUTE_NAME = "datasource";
    private static final String SQL_ATTRIBUTE_NAME = "sql";
    private static final String VARIABLE_ATTRIBUTE_NAME = "var";
    private static final String QUERY_ELEMENT_NAME = "query";
    private static final String QUERIES_ELEMENT_NAME = "queries";
    private static final String PROCEDURE_ELEMENT_NAME = "procedure";
    private static final String PARAMETER_ELEMENT_NAME = "param";
    private static final String SWIMLANE_PARAMETER_ELEMENT_NAME = "swimlane-param";
    private static final String RESULT_ELEMENT_NAME = "result";
    private static final String SWIMLANE_RESULT_ELEMENT_NAME = "swimlane-result";
    private static final String FIELD_PARAMETER_ELEMENT_NAME = "field";
    private static final String OUT_PARAMETER_INDEX_ELEMENT_NAME = "outIndex";

    /**
     * Parses DatabaseTaskHandler configuration
     * 
     * @param configuration
     *            xml configuration
     * @param variableProvider
     *            process variables to substitute values in query string
     */
    public static DatabaseTask[] parse(String configuration, VariableProvider variableProvider) {
        Document document = XmlUtils.parseWithXSDValidation(configuration, "database-tasks.xsd");
        List<Element> taskElementList = document.getRootElement().elements(TASK_ELEMENT_NAME);
        DatabaseTask[] databaseTasks = new DatabaseTask[taskElementList.size()];
        for (int i = 0; i < databaseTasks.length; i++) {
            Element taskElement = taskElementList.get(i);
            String datasourceName = parseSQLQueryElement(taskElement.attributeValue(DATASOURCE_ATTRIBUTE_NAME), variableProvider);
            AbstractQuery[] abstractQueries = parseTaskQueries(taskElement, variableProvider);
            databaseTasks[i] = new DatabaseTask(datasourceName, abstractQueries);
        }
        return databaseTasks;
    }

    private static AbstractQuery[] parseTaskQueries(Element taskElement, VariableProvider variableProvider) {
        Element queriesElement = taskElement.element(QUERIES_ELEMENT_NAME);

        List<Element> queryElementList = queriesElement.elements();
        List<AbstractQuery> queryList = new ArrayList<AbstractQuery>(queryElementList.size());
        for (int i = 0; i < queryElementList.size(); i++) {
            Element node = queryElementList.get(i);
            if (!(QUERY_ELEMENT_NAME.equals(node.getName()) || PROCEDURE_ELEMENT_NAME.equals(node.getName()))) {
                continue;
            }
            Element queryElement = node;
            String sql = parseSQLQueryElement(queryElement.attributeValue(SQL_ATTRIBUTE_NAME), variableProvider);
            List<Parameter> parameterList = new ArrayList<Parameter>();
            List<Result> resultList = new ArrayList<Result>();
            parseQueryParameters(queryElement, parameterList, resultList);
            Parameter[] parameters = parameterList.toArray(new Parameter[parameterList.size()]);
            Result[] results = resultList.toArray(new Result[resultList.size()]);
            if (QUERY_ELEMENT_NAME.equals(queryElement.getName())) {
                queryList.add(new Query(sql, parameters, results));
            } else if (PROCEDURE_ELEMENT_NAME.equals(queryElement.getName())) {
                queryList.add(new StoredProcedureQuery(sql, parameters, results));
            }
        }
        return queryList.toArray(new AbstractQuery[queryList.size()]);
    }

    private static void parseQueryParameters(Element queryElement, List<Parameter> parameterList, List<Result> resultList) {
        List<Element> queryNodes = queryElement.elements();
        for (int k = 0; k < queryNodes.size(); k++) {
            Element queryNode = queryNodes.get(k);
            Element parameterElement = queryNode;
            String elementName = parameterElement.getName();
            String variableName = parameterElement.attributeValue(VARIABLE_ATTRIBUTE_NAME);
            String fieldName = parameterElement.attributeValue(FIELD_PARAMETER_ELEMENT_NAME);
            String outParameterIndexStr = parameterElement.attributeValue(OUT_PARAMETER_INDEX_ELEMENT_NAME);
            int outParameterIndex = Strings.isNullOrEmpty(outParameterIndexStr) ? -1 : Integer.parseInt(outParameterIndexStr);
            if (PARAMETER_ELEMENT_NAME.equals(elementName)) {
                parameterList.add(new Parameter(variableName, fieldName));
            } else if (SWIMLANE_PARAMETER_ELEMENT_NAME.equals(elementName)) {
                parameterList.add(new SwimlaneParameter(variableName, fieldName));
            } else if (RESULT_ELEMENT_NAME.equals(elementName)) {
                resultList.add(new Result(variableName, fieldName, outParameterIndex));
            } else if (SWIMLANE_RESULT_ELEMENT_NAME.equals(elementName)) {
                resultList.add(new SwimlaneResult(variableName, fieldName));
            }
        }
    }

    private static final Pattern pattern = Pattern.compile("\\$\\{(.*)\\}");

    public static String parseSQLQueryElement(String sqlText, VariableProvider variableProvider) {
        sqlText = sqlText.replaceAll("&#10;", "\n");
        if (!sqlText.startsWith("$")) {
            return sqlText;
        }
        String sql = "";
        Matcher matcher = pattern.matcher(sqlText);
        if (matcher.matches()) {
            String variableName = matcher.group(1);
            sql = variableProvider.getValueNotNull(String.class, variableName);
        }
        return sql;
    }

}
