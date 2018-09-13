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
package ru.runa.wfe.definition.par;

import java.util.List;

import org.dom4j.Document;
import org.dom4j.Element;

import ru.runa.wfe.commons.xml.XmlUtils;
import ru.runa.wfe.definition.IFileDataProvider;
import ru.runa.wfe.definition.InvalidDefinitionException;
import ru.runa.wfe.form.Interaction;
import ru.runa.wfe.lang.MultiTaskNode;
import ru.runa.wfe.lang.Node;
import ru.runa.wfe.lang.ParsedProcessDefinition;
import ru.runa.wfe.lang.ParsedSubprocessDefinition;
import ru.runa.wfe.var.UserType;
import ru.runa.wfe.var.VariableDefinition;
import ru.runa.wfe.var.VariableMapping;

import com.google.common.base.Objects;
import com.google.common.base.Strings;
import com.google.common.base.Throwables;

/**
 * Created on 17.11.2004
 *
 */
public class InteractionsParser implements ProcessArchiveParser {
    private final static String FORM_ELEMENT_NAME = "form";
    private final static String STATE_ATTRIBUTE_NAME = "state";
    private final static String FILE_ATTRIBUTE_NAME = "file";
    private static final String VALIDATION_FILE_ATTRIBUTE_NAME = "validationFile";
    private static final String SCRIPT_FILE_ATTRIBUTE_NAME = "scriptFile";
    private static final String JS_VALIDATION_ATTRIBUTE_NAME = "jsValidation";
    private final static String TYPE_ATTRIBUTE_NAME = "type";
    private static final String TEMPLATE_FILE_NAME = "templateFileName";

    @Override
    public boolean isApplicableToEmbeddedSubprocess() {
        return true;
    }

    @Override
    public void readFromArchive(ProcessArchive archive, ParsedProcessDefinition parsedProcessDefinition) {
        try {
            String formsFileName = IFileDataProvider.FORMS_XML_FILE_NAME;
            if (parsedProcessDefinition instanceof ParsedSubprocessDefinition) {
                formsFileName = parsedProcessDefinition.getNodeId() + "." + formsFileName;
            }
            byte[] formsXml = parsedProcessDefinition.getFileData(formsFileName);
            if (formsXml == null) {
                return;
            }
            byte[] processScriptData = parsedProcessDefinition.getFileData(IFileDataProvider.FORM_JS_FILE_NAME);
            Document document = XmlUtils.parseWithoutValidation(formsXml);
            List<Element> formElements = document.getRootElement().elements(FORM_ELEMENT_NAME);
            for (Element formElement : formElements) {
                String stateId = formElement.attributeValue(STATE_ATTRIBUTE_NAME);
                Node node = parsedProcessDefinition.getNodeNotNull(stateId);
                String fileName = formElement.attributeValue(FILE_ATTRIBUTE_NAME);
                String type = formElement.attributeValue(TYPE_ATTRIBUTE_NAME);
                if (type != null) {
                    type = type.intern();
                }
                String validationFileName = formElement.attributeValue(VALIDATION_FILE_ATTRIBUTE_NAME);
                boolean jsValidationEnabled = Boolean.parseBoolean(formElement.attributeValue(JS_VALIDATION_ATTRIBUTE_NAME));
                String scriptFileName = formElement.attributeValue(SCRIPT_FILE_ATTRIBUTE_NAME);
                String templateFileName = formElement.attributeValue(TEMPLATE_FILE_NAME);

                byte[] formCode = null;
                if (!Strings.isNullOrEmpty(fileName)) {
                    formCode = parsedProcessDefinition.getFileDataNotNull(fileName);
                }
                byte[] validationXml = null;
                if (!Strings.isNullOrEmpty(validationFileName)) {
                    validationXml = parsedProcessDefinition.getFileDataNotNull(validationFileName);
                }
                byte[] formScriptData = null;
                if (!Strings.isNullOrEmpty(scriptFileName)) {
                    formScriptData = parsedProcessDefinition.getFileDataNotNull(scriptFileName);
                }
                byte[] css = parsedProcessDefinition.getFileData(IFileDataProvider.FORM_CSS_FILE_NAME);
                byte[] template = null;
                if (!Strings.isNullOrEmpty(templateFileName)) {
                    template = parsedProcessDefinition.getFileDataNotNull(templateFileName);
                }
                Interaction interaction = new Interaction(node, type, formCode, validationXml, jsValidationEnabled, processScriptData,
                        formScriptData, css, template);
                if (validationXml != null) {
                    List<String> variableNames = ValidationXmlParser.readVariableNames(parsedProcessDefinition, validationFileName, validationXml);
                    List<String> requiredVarNames = ValidationXmlParser.readRequiredVariableNames(parsedProcessDefinition, validationXml);
                    for (String varName : requiredVarNames) {
                        interaction.getRequiredVariableNames().add(varName);
                    }
                    for (String name : variableNames) {
                        VariableDefinition variableDefinition = parsedProcessDefinition.getVariable(name, true);
                        if (variableDefinition == null && node instanceof MultiTaskNode) {
                            for (VariableMapping mapping : ((MultiTaskNode) node).getVariableMappings()) {
                                boolean strictMatch = Objects.equal(mapping.getMappedName(), name);
                                boolean userTypeMatch = name.startsWith(mapping.getMappedName() + UserType.DELIM);
                                if (strictMatch || userTypeMatch) {
                                    VariableDefinition mappedVariableDefinition = parsedProcessDefinition.getVariable(mapping.getName(), true);
                                    String format = mappedVariableDefinition.getFormatComponentClassNames()[0];
                                    if (userTypeMatch) {
                                        String attributeName = name.substring((mapping.getMappedName() + UserType.DELIM).length());
                                        UserType userType = parsedProcessDefinition.getUserTypeNotNull(format);
                                        VariableDefinition attributeDefinition = userType.getAttributeNotNull(attributeName);
                                        format = attributeDefinition.getFormat();
                                    }
                                    variableDefinition = new VariableDefinition(name, null, format, parsedProcessDefinition.getUserType(format));
                                    variableDefinition.initComponentUserTypes(parsedProcessDefinition);
                                    break;
                                }
                            }
                        }
                        if (variableDefinition == null) {
                            throw new InvalidDefinitionException(parsedProcessDefinition.getName(), "Variable '" + name + "' is defined in '"
                                    + validationFileName + "' but not defined in " + parsedProcessDefinition);
                        }
                        interaction.getVariables().put(name, variableDefinition);
                    }
                }
                parsedProcessDefinition.addInteraction(stateId, interaction);
            }
        } catch (Exception e) {
            Throwables.propagateIfInstanceOf(e, InvalidDefinitionException.class);
            throw new InvalidDefinitionException(parsedProcessDefinition.getName(), e);
        }
    }
}
