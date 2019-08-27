package ru.runa.wfe.extension.handler.sql;

import com.google.common.base.Strings;
import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.dom4j.Document;
import org.dom4j.Element;
import ru.runa.wfe.commons.xml.XmlUtils;
import ru.runa.wfe.var.VariableProvider;

public class SqlHandlerConfigXmlParser {
    private static final String TASK_ELEMENT_NAME = "task";
    private static final String DATASOURCE_ATTRIBUTE_NAME = "datasource";
    private static final String SQL_ATTRIBUTE_NAME = "sql";
    private static final String VARIABLE_ATTRIBUTE_NAME = "var";
    private static final String QUERIES_ELEMENT_NAME = "queries";
    private static final String PROCEDURE_ELEMENT_NAME = "procedure";
    private static final String PARAMETER_ELEMENT_NAME = "param";
    private static final String SWIMLANE_PARAMETER_ELEMENT_NAME = "swimlane-param";
    private static final String RESULT_ELEMENT_NAME = "result";
    private static final String SWIMLANE_RESULT_ELEMENT_NAME = "swimlane-result";
    private static final String FIELD_PARAMETER_ELEMENT_NAME = "field";
    private static final String OUT_PARAMETER_INDEX_ELEMENT_NAME = "outIndex";
    private static final Pattern pattern = Pattern.compile("\\$\\{(.*)\\}");

    /**
     * Parses DatabaseTaskHandler configuration
     * 
     * @param configuration
     *            xml configuration
     * @param variableProvider
     *            process variables to substitute values in query string
     */
    public static SqlHandlerConfig parse(String configuration, VariableProvider variableProvider) {
        Document document = XmlUtils.parseWithXSDValidation(configuration, "database-tasks.xsd");
        // devstudio configurer provides 1 element
        Element taskElement = document.getRootElement().element(TASK_ELEMENT_NAME);
        String dataSourceValue = substituteValue(taskElement.attributeValue(DATASOURCE_ATTRIBUTE_NAME), variableProvider);
        List<SqlHandlerQuery> queries = parseQueries(taskElement, variableProvider);
        return new SqlHandlerConfig(dataSourceValue, queries);
    }

    private static String substituteValue(String value, VariableProvider variableProvider) {
        value = value.replaceAll("&#10;", "\n");
        if (!value.startsWith("$")) {
            return value;
        }
        String sql = "";
        Matcher matcher = pattern.matcher(value);
        if (matcher.matches()) {
            String variableName = matcher.group(1);
            sql = variableProvider.getValueNotNull(String.class, variableName);
        }
        return sql;
    }

    private static List<SqlHandlerQuery> parseQueries(Element taskElement, VariableProvider variableProvider) {
        Element queriesElement = taskElement.element(QUERIES_ELEMENT_NAME);
        List<Element> queryElements = queriesElement.elements();
        List<SqlHandlerQuery> queries = new ArrayList<SqlHandlerQuery>(queryElements.size());
        for (Element queryElement : queryElements) {
            String sql = substituteValue(queryElement.attributeValue(SQL_ATTRIBUTE_NAME), variableProvider);
            List<SqlHandlerQueryParameter> sqlHandlerQueryParameters = new ArrayList<SqlHandlerQueryParameter>();
            List<SqlHandlerQueryResult> sqlHandlerQueryResults = new ArrayList<SqlHandlerQueryResult>();
            parseQueryParameters(queryElement, sqlHandlerQueryParameters, sqlHandlerQueryResults);
            boolean storedProcedureQuery = PROCEDURE_ELEMENT_NAME.equals(queryElement.getName());
            queries.add(new SqlHandlerQuery(sql, sqlHandlerQueryParameters, sqlHandlerQueryResults, storedProcedureQuery));
        }
        return queries;
    }

    private static void parseQueryParameters(Element queryElement, List<SqlHandlerQueryParameter> sqlHandlerQueryParameters, List<SqlHandlerQueryResult> sqlHandlerQueryResults) {
        List<Element> parameterElements = queryElement.elements();
        for (Element parameterElement : parameterElements) {
            String elementName = parameterElement.getName();
            String variableName = parameterElement.attributeValue(VARIABLE_ATTRIBUTE_NAME);
            String fieldName = parameterElement.attributeValue(FIELD_PARAMETER_ELEMENT_NAME);
            String outParameterIndexStr = parameterElement.attributeValue(OUT_PARAMETER_INDEX_ELEMENT_NAME);
            int outParameterIndex = Strings.isNullOrEmpty(outParameterIndexStr) ? -1 : Integer.parseInt(outParameterIndexStr);
            if (PARAMETER_ELEMENT_NAME.equals(elementName)) {
                sqlHandlerQueryParameters.add(new SqlHandlerQueryParameter(variableName, fieldName, false));
            } else if (SWIMLANE_PARAMETER_ELEMENT_NAME.equals(elementName)) {
                sqlHandlerQueryParameters.add(new SqlHandlerQueryParameter(variableName, fieldName, true));
            } else if (RESULT_ELEMENT_NAME.equals(elementName)) {
                sqlHandlerQueryResults.add(new SqlHandlerQueryResult(variableName, fieldName, outParameterIndex, false));
            } else if (SWIMLANE_RESULT_ELEMENT_NAME.equals(elementName)) {
                sqlHandlerQueryResults.add(new SqlHandlerQueryResult(variableName, fieldName, outParameterIndex, true));
            }
        }
    }

}
