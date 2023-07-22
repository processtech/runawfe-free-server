package ru.runa.wfe.office.doc;

import com.google.common.base.Preconditions;
import java.util.List;
import org.dom4j.Element;
import ru.runa.wfe.commons.BackCompatibilityClassNames;
import ru.runa.wfe.office.doc.DocxConfig.TableConfig;
import ru.runa.wfe.office.shared.FilesSupplierConfigParser;
import ru.runa.wfe.var.VariableDefinition;
import ru.runa.wfe.var.format.FormatCommons;
import ru.runa.wfe.var.format.VariableFormat;

public class DocxConfigParser extends FilesSupplierConfigParser<DocxConfig> {

    @Override
    protected DocxConfig instantiate() {
        return new DocxConfig();
    }

    @SuppressWarnings("unchecked")
    @Override
    protected void parseCustom(Element root, DocxConfig config) throws Exception {
        config.setStrictMode(Boolean.parseBoolean(root.attributeValue("strict", "true")));
        List<Element> tableElements = root.elements("table");
        for (Element tableElement : tableElements) {
            TableConfig tableConfig = new TableConfig();
            tableConfig.setAddBreak(Boolean.parseBoolean(tableElement.attributeValue("addBreak", "false")));
            tableConfig.setStyleName(tableElement.attributeValue("styleName"));
            String markerName = tableElement.attributeValue("name");
            Preconditions.checkNotNull(markerName, "Missed 'name' attribute in table element");
            List<Element> columnElements = tableElement.elements("column");
            for (Element columnElement : columnElements) {
                String variableName = columnElement.attributeValue("variable");
                Preconditions.checkNotNull(markerName, "Missed 'variable' attribute in table element");
                tableConfig.getColumns().add(variableName);
            }
            config.getTables().put(markerName, tableConfig);
        }
        List<Element> hintElements = root.elements("hint");
        for (Element hintElement : hintElements) {
            String variableName = hintElement.attributeValue("name");
            Preconditions.checkNotNull(variableName, "Missed 'name' attribute in table element");
            String formatClassName = hintElement.attributeValue("type");
            Preconditions.checkNotNull(formatClassName, "Missed 'type' attribute in table element");
            formatClassName = BackCompatibilityClassNames.getClassName(formatClassName);
            VariableDefinition variableDefinition = new VariableDefinition(variableName, null, formatClassName, null);
            VariableFormat variableFormat = FormatCommons.create(variableDefinition);
            config.getTypeHints().put(variableName, variableFormat);
        }
    }

}
