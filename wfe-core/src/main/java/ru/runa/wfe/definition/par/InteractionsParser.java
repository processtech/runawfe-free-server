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

import com.google.common.base.Objects;
import com.google.common.base.Strings;
import com.google.common.base.Throwables;
import java.util.List;
import org.dom4j.Document;
import org.dom4j.Element;
import ru.runa.wfe.commons.xml.XmlUtils;
import ru.runa.wfe.definition.FileDataProvider;
import ru.runa.wfe.definition.InvalidDefinitionException;
import ru.runa.wfe.form.Interaction;
import ru.runa.wfe.lang.MultiTaskNode;
import ru.runa.wfe.lang.Node;
import ru.runa.wfe.lang.ProcessDefinition;
import ru.runa.wfe.lang.SubprocessDefinition;
import ru.runa.wfe.var.UserType;
import ru.runa.wfe.var.VariableDefinition;
import ru.runa.wfe.var.VariableMapping;

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
    public void readFromArchive(ProcessArchive archive, ProcessDefinition processDefinition) {
        try {
            String formsFileName = FileDataProvider.FORMS_XML_FILE_NAME;
            if (processDefinition instanceof SubprocessDefinition) {
                formsFileName = processDefinition.getNodeId() + "." + formsFileName;
            }
            byte[] formsXml = processDefinition.getFileData(formsFileName);
            if (formsXml == null) {
                return;
            }
            byte[] processScriptData = processDefinition.getFileData(FileDataProvider.FORM_JS_FILE_NAME);
            Document document = XmlUtils.parseWithoutValidation(formsXml);
            List<Element> formElements = document.getRootElement().elements(FORM_ELEMENT_NAME);
            for (Element formElement : formElements) {
                String stateId = formElement.attributeValue(STATE_ATTRIBUTE_NAME);
                Node node = processDefinition.getNodeNotNull(stateId);
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
                    formCode = processDefinition.getFileDataNotNull(fileName);
                }
                byte[] validationXml = null;
                if (!Strings.isNullOrEmpty(validationFileName)) {
                    validationXml = processDefinition.getFileDataNotNull(validationFileName);
                }
                byte[] formScriptData = null;
                if (!Strings.isNullOrEmpty(scriptFileName)) {
                    formScriptData = processDefinition.getFileDataNotNull(scriptFileName);
                }
                byte[] css = processDefinition.getFileData(FileDataProvider.FORM_CSS_FILE_NAME);
                byte[] template = null;
                if (!Strings.isNullOrEmpty(templateFileName)) {
                    template = processDefinition.getFileDataNotNull(templateFileName);
                }
                Interaction interaction = new Interaction(node, type, formCode, validationXml, jsValidationEnabled, processScriptData,
                        formScriptData, css, template);
                if (validationXml != null) {
                    List<String> variableNames = ValidationXmlParser.readVariableNames(processDefinition, validationFileName, validationXml);
                    List<String> requiredVarNames = ValidationXmlParser.readRequiredVariableNames(processDefinition, validationXml);
                    for (String varName : requiredVarNames) {
                        interaction.getRequiredVariableNames().add(varName);
                    }
                    for (String name : variableNames) {
                        VariableDefinition variableDefinition = processDefinition.getVariable(name, true);
                        if (variableDefinition == null && node instanceof MultiTaskNode) {
                            for (VariableMapping mapping : ((MultiTaskNode) node).getVariableMappings()) {
                                boolean strictMatch = Objects.equal(mapping.getMappedName(), name);
                                boolean userTypeMatch = name.startsWith(mapping.getMappedName() + UserType.DELIM);
                                if (strictMatch || userTypeMatch) {
                                    VariableDefinition mappedVariableDefinition = processDefinition.getVariable(mapping.getName(), true);
                                    String format = mappedVariableDefinition.getFormatComponentClassNames()[0];
                                    if (userTypeMatch) {
                                        String attributeName = name.substring((mapping.getMappedName() + UserType.DELIM).length());
                                        UserType userType = processDefinition.getUserTypeNotNull(format);
                                        VariableDefinition attributeDefinition = userType.getAttributeNotNull(attributeName);
                                        format = attributeDefinition.getFormat();
                                    }
                                    variableDefinition = new VariableDefinition(name, null, format, processDefinition.getUserType(format));
                                    variableDefinition.initComponentUserTypes(processDefinition);
                                    break;
                                }
                            }
                        }
                        if (variableDefinition == null) {
                            throw new InvalidDefinitionException(processDefinition.getName(), "Variable '" + name + "' is defined in '"
                                    + validationFileName + "' but not defined in " + processDefinition);
                        }
                        interaction.getVariables().put(name, variableDefinition);
                    }
                }
                processDefinition.addInteraction(stateId, interaction);
            }
        } catch (Exception e) {
            Throwables.propagateIfInstanceOf(e, InvalidDefinitionException.class);
            throw new InvalidDefinitionException(processDefinition.getName(), e);
        }
    }
}
