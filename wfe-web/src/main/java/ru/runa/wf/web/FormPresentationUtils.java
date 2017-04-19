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

package ru.runa.wf.web;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.servlet.jsp.PageContext;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.w3c.dom.html.HTMLOptionElement;

import ru.runa.common.WebResources;
import ru.runa.common.web.Commons;
import ru.runa.common.web.HTMLUtils;
import ru.runa.common.web.MessagesException;
import ru.runa.common.web.Resources;
import ru.runa.common.web.form.IdForm;
import ru.runa.wf.web.action.LoadProcessDefinitionHtmlFileAction;
import ru.runa.wf.web.form.DefinitionFileForm;
import ru.runa.wfe.commons.SystemProperties;
import ru.runa.wfe.commons.web.PortletUrlType;
import ru.runa.wfe.var.IVariableProvider;
import ru.runa.wfe.var.dto.WfVariable;
import ru.runa.wfe.var.format.VariableFormatContainer;

import com.google.common.base.Charsets;
import com.google.common.base.Objects;
import com.google.common.base.Strings;
import com.google.common.base.Throwables;
import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

/**
 * 
 * Created on 21.02.2007
 * 
 */
public class FormPresentationUtils {
    private static final Log log = LogFactory.getLog(FormPresentationUtils.class);

    private static final String PROTOCOL_SEPARATOR = "//";
    /**
     * This map contains Tag name -> Href attribute name
     */
    private static final Map<String, String> TAG_NAME_ATTRIBUTE_MAP = new HashMap<String, String>();
    static {
        TAG_NAME_ATTRIBUTE_MAP.put("a", "href");
        TAG_NAME_ATTRIBUTE_MAP.put("img", "src");
        TAG_NAME_ATTRIBUTE_MAP.put("frame", "src");
        TAG_NAME_ATTRIBUTE_MAP.put("link", "href");
        TAG_NAME_ATTRIBUTE_MAP.put("script", "src");
    }
    private static final String VALUE_ATTR = "value";
    private static final String NAME_ATTR = "name";
    private static final String TYPE_ATTR = "type";
    private static final String CHECKED_ATTR = "checked";
    private static final String SELECTED_ATTR = "selected";
    private static final String CSS_CLASS_ATTR = "class";
    private static final String ERROR_CONTAINER = "div";

    public static String adjustUrls(PageContext pageContext, Long definitionId, String htmlHref, byte[] originalBytes) {
        Document document = HTMLUtils.readHtml(originalBytes);
        adjustUrls(pageContext, document, definitionId, htmlHref);
        return HTMLUtils.writeHtml(document);
    }

    private static void adjustUrls(PageContext pageContext, Document document, Long definitionId, String htmlHref) {
        if (pageContext != null) {
            for (Map.Entry<String, String> tagNameTagAttributeEntry : TAG_NAME_ATTRIBUTE_MAP.entrySet()) {
                String tagName = tagNameTagAttributeEntry.getKey();
                NodeList htmlTagElements = document.getElementsByTagName(tagName);
                if (htmlTagElements.getLength() > 0) {
                    String attributeName = tagNameTagAttributeEntry.getValue();
                    handleElements(htmlTagElements, attributeName, htmlHref, pageContext, definitionId);
                }
            }
            if (WebResources.getBooleanProperty("form.tr.title.clean", true)) {
                NodeList trElements = document.getElementsByTagName("tr");
                for (int i = 0; i < trElements.getLength(); i++) {
                    Node node = trElements.item(i);
                    if (node.getAttributes().getNamedItem("title") != null) {
                        node.getAttributes().removeNamedItem("title");
                    }
                }
            }
        }
    }

    private static void handleElements(NodeList nodeList, String hrefAttributeName, String htmlHref, PageContext pageContext, Long id) {
        String[] fileNameStructureElements = htmlHref.split("/");
        Map<String, Object> params = Maps.newHashMap();
        params.put(IdForm.ID_INPUT_NAME, id);

        for (int i = 0; i < nodeList.getLength(); i++) {
            Node node = nodeList.item(i);
            Node hrefNode = node.getAttributes().getNamedItem(hrefAttributeName);
            if (hrefNode == null) {
                continue;
            }
            String href = hrefNode.getNodeValue();
            if (href == null || href.length() == 0 || href.startsWith("#") || href.startsWith("javascript")) {
                continue;
            }
            if (isHrefRelative(href)) {
                String url = getNormalizedUrlForElement(htmlHref, fileNameStructureElements, href);
                int anchorIndex = url.indexOf("#");
                String anchor = null;
                if (anchorIndex >= 0) {
                    anchor = url.substring(anchorIndex + 1);
                    url = url.substring(0, anchorIndex);
                }
                params.put(DefinitionFileForm.URL_INPUT_NAME, url);
                String newHref = Commons.getActionUrl(LoadProcessDefinitionHtmlFileAction.ACTION_PATH, params, anchor, pageContext,
                        PortletUrlType.Resource);
                hrefNode.setNodeValue(newHref);
            }
        }
    }

    private static String getNormalizedUrlForElement(String htmlHref, String[] fileNameStructureElements, String originalHref) {
        StringBuilder normalizedUrl = new StringBuilder();

        if (originalHref.startsWith("./")) {
            originalHref = originalHref.substring(2);
        }

        if (originalHref.startsWith("../")) {
            int prefixLength = "../".length();
            int counter = 0;
            while (originalHref.startsWith("../", counter * prefixLength)) {
                counter++;
            }
            if (counter < fileNameStructureElements.length) {
                for (int j = 0; j < fileNameStructureElements.length - counter - 1; j++) {
                    normalizedUrl.append(fileNameStructureElements[j]);
                    normalizedUrl.append("/");
                }
            } else {
                normalizedUrl.append("#");
            }
        } else if (originalHref.startsWith("/")) {
            // DO NOTHING
        } else if (originalHref.startsWith("#")) {
            normalizedUrl.append(htmlHref);
        } else {
            int lastDirIndex = htmlHref.lastIndexOf("/");
            if (lastDirIndex > 0) {
                String additionalPath = htmlHref.substring(0, lastDirIndex + 1);
                normalizedUrl.append(additionalPath);
            }
        }
        return normalizedUrl.append(originalHref).toString();
    }

    private static boolean isHrefRelative(String href) {
        if (href.contains(PROTOCOL_SEPARATOR) || href.startsWith("/")) {
            return false;
        } else {
            return true;
        }
    }

    public static String adjustForm(PageContext pageContext, Long definitionId, String formHtml, IVariableProvider variableProvider,
            List<String> requiredVarNames) {
        try {
            Map<String, String[]> userInput = null;
            if (SystemProperties.isV3CompatibilityMode()) {
                userInput = FormSubmissionUtils.getUserFormInput(pageContext.getRequest());
            }
            Map<String, String> userErrors = FormSubmissionUtils.getUserFormValidationErrors(pageContext.getRequest());

            Document document = HTMLUtils.readHtml(formHtml.getBytes(Charsets.UTF_8));
            adjustUrls(pageContext, document, definitionId, "form.ftl");
            NodeList htmlTagElements = document.getElementsByTagName("input");
            for (int i = 0; i < htmlTagElements.getLength(); i++) {
                Element node = (Element) htmlTagElements.item(i);
                String typeName = node.getAttribute(TYPE_ATTR);
                String inputName = node.getAttribute(NAME_ATTR);
                if (WebResources.isHighlightRequiredFields() && requiredVarNames.contains(inputName)) {
                    Element requiredNode = node;
                    if ("file".equalsIgnoreCase(typeName) && WebResources.isAjaxFileInputEnabled()) {
                        requiredNode = (Element) node.getParentNode();
                    }
                    addRequiredClassAttribute(requiredNode);
                }
                handleErrors(userErrors, inputName, pageContext, document, node);
                String stringValue = getStringValue(inputName, variableProvider, userInput);
                if (stringValue == null) {
                    continue;
                }
                // handle input (type='text, password')
                if (Strings.isNullOrEmpty(typeName) || "text".equalsIgnoreCase(typeName) || "password".equalsIgnoreCase(typeName)
                        || "hidden".equalsIgnoreCase(typeName)) {
                    log.debug("Setting " + inputName + "[value]=" + stringValue);
                    node.setAttribute(VALUE_ATTR, stringValue);
                } else if ("checkbox".equalsIgnoreCase(typeName)) {
                    String checkBoxValue = node.getAttribute(VALUE_ATTR);
                    if (Objects.equal(checkBoxValue, stringValue) || "true".equals(stringValue)) {
                        log.debug("Adding " + inputName + "[checked]");
                        node.setAttribute(CHECKED_ATTR, CHECKED_ATTR);
                    } else {
                        log.debug("Removing " + inputName + "[checked]");
                        node.removeAttribute(CHECKED_ATTR);
                    }
                } else if ("radio".equalsIgnoreCase(typeName)) {
                    String radioValue = node.getAttribute(VALUE_ATTR);
                    if (Objects.equal(stringValue, radioValue)) {
                        log.debug("Adding " + inputName + "[checked]");
                        node.setAttribute(CHECKED_ATTR, CHECKED_ATTR);
                    } else {
                        log.debug("Removing " + inputName + "[checked]");
                        node.removeAttribute(CHECKED_ATTR);
                    }
                } else if ("file".equalsIgnoreCase(typeName) || "button".equalsIgnoreCase(typeName)) {
                } else {
                    log.error("Strange input " + inputName + "[type='" + typeName + "']");
                }
            }
            NodeList textareaElements = document.getElementsByTagName("textarea");
            for (int i = 0; i < textareaElements.getLength(); i++) {
                Element node = (Element) textareaElements.item(i);
                String inputName = node.getAttribute(NAME_ATTR);
                if (WebResources.isHighlightRequiredFields() && requiredVarNames.contains(inputName)) {
                    addRequiredClassAttribute(node);
                }
                handleErrors(userErrors, inputName, pageContext, document, node);
                String stringValue = getStringValue(inputName, variableProvider, userInput);
                if (stringValue == null || Strings.isNullOrEmpty(inputName)) {
                    continue;
                }
                if (node.getFirstChild() != null) {
                    log.debug("Setting " + inputName + " text");
                    node.getFirstChild().setNodeValue(stringValue);
                } else {
                    log.debug("Adding " + inputName + " text");
                    node.appendChild(document.createTextNode(stringValue));
                }
            }
            NodeList selectElements = document.getElementsByTagName("select");
            for (int i = 0; i < selectElements.getLength(); i++) {
                Element node = (Element) selectElements.item(i);
                String inputName = node.getAttribute(NAME_ATTR);
                if (WebResources.isHighlightRequiredFields() && requiredVarNames.contains(inputName)) {
                    wrapSelectToErrorContainer(document, node, inputName, true);
                }
                handleErrors(userErrors, inputName, pageContext, document, node);
                String stringValue = getStringValue(inputName, variableProvider, userInput);
                if (stringValue == null) {
                    continue;
                }
                NodeList options = node.getChildNodes();
                for (int j = 0; j < options.getLength(); j++) {
                    if (options.item(j) instanceof HTMLOptionElement) {
                        HTMLOptionElement option = (HTMLOptionElement) options.item(j);
                        if (Objects.equal(option.getValue(), stringValue)) {
                            if (!option.hasAttribute(SELECTED_ATTR)) {
                                log.debug("Setting selected option " + option.getValue() + " in select[name='" + inputName + "']");
                                option.setAttribute(SELECTED_ATTR, SELECTED_ATTR);
                            }
                        } else {
                            if (option.hasAttribute(SELECTED_ATTR)) {
                                log.debug("Removing selected option " + option.getValue() + " in select[name='" + inputName + "']");
                                option.removeAttribute(SELECTED_ATTR);
                            }
                        }
                    }
                }
            }
            if (!userErrors.isEmpty()) {
                Set<String> messages = Sets.newHashSet();
                for (String variableName : userErrors.keySet()) {
                    messages.add(variableName + ": " + getErrorText(pageContext, userErrors, variableName));
                }
                log.debug("Not for all errors inputs found, appending errors to the end of the form: " + messages);
                document.getLastChild().appendChild(document.createElement("hr"));
                Element font = document.createElement("font");
                for (String message : messages) {
                    font.appendChild(document.createElement("br"));
                    font.setAttribute(CSS_CLASS_ATTR, "error");
                    Element b = document.createElement("b");
                    b.appendChild(document.createTextNode(message));
                    font.appendChild(b);
                }
                document.getLastChild().appendChild(font);
            }
            return HTMLUtils.writeHtml(document);
        } catch (Exception e) {
            throw Throwables.propagate(e);
        }
    }

    /**
     * Rules:
     * 
     * 1) don't handling multiple input (we cannot do this properly; they are handled in according tags.
     * 
     * 2) Don't fill long strings due to java.lang.ArrayIndexOutOfBoundsException at java.lang.String.getChars(String.java:854) at
     * org.apache.xml.serializer.WriterToUTF8Buffered .write(WriterToUTF8Buffered.java:347)
     * 
     * 3) User input has precedence on variables
     * 
     * @param valueArray
     *            http values
     * @return <code>null</code> or replacement value
     */
    private static String getStringValue(String name, IVariableProvider variableProvider, Map<String, String[]> userInput) {
        if (userInput != null && userInput.get(name) != null && userInput.get(name).length == 1 && userInput.get(name)[0].length() < 1000) {
            return userInput.get(name)[0];
        }
        if (name.endsWith(VariableFormatContainer.SIZE_SUFFIX) || name.contains(".")) {
            return null;
        }
        WfVariable variable = variableProvider.getVariable(name);
        if (variable != null) {
            return variable.getDefinition().getFormatNotNull().format(variable.getValue());
        }
        return null;
    }

    private static String getErrorText(PageContext pageContext, Map<String, String> errors, String inputName) {
        String errorText = errors.get(inputName);
        if (errorText == null) {
            errorText = Commons.getMessage(MessagesException.MESSAGE_VARIABLE_FORMAT_ERROR.getKey(), pageContext, new Object[] { inputName });
        }
        if (errorText.trim().length() == 0) {
            errorText = Commons.getMessage(MessagesException.MESSAGE_VALIDATION_ERROR.getKey(), pageContext);
        }
        return errorText;
    }

    private static void handleErrors(Map<String, String> errors, String inputName, PageContext pageContext, Document document, Element node) {
        if (errors.containsKey(inputName)) {
            String errorText = getErrorText(pageContext, errors, inputName);
            if ("file".equalsIgnoreCase(node.getAttribute(TYPE_ATTR)) && WebResources.isAjaxFileInputEnabled()) {
                try {
                    node = (Element) ((Element) ((Element) node.getParentNode()).getParentNode()).getParentNode();
                } catch (Exception e) {
                    log.error("Unexpected file input format", e);
                }
            }
            if (WebResources.useImagesForValidationErrors()) {
                Element errorImg = document.createElement("img");
                errorImg.setAttribute("title", errorText);
                errorImg.setAttribute("src", Commons.getUrl("/images/error.gif", pageContext, PortletUrlType.Resource));
                Node parent = node.getParentNode();
                parent.insertBefore(errorImg, node.getNextSibling());
            } else {
                node.setAttribute("title", errorText);
                if ("select".equalsIgnoreCase(node.getTagName())) {
                    node = wrapSelectToErrorContainer(document, node, inputName, false);
                }
                addClassAttribute(node, Resources.CLASS_INVALID);
            }
            // avoiding multiple error labels
            errors.remove(inputName);
        }
    }

    private static void addRequiredClassAttribute(Element element) {
        addClassAttribute(element, Resources.CLASS_REQUIRED);
    }

    private static void addClassAttribute(Element element, String cssClass) {
        String cssClasses = element.getAttribute(CSS_CLASS_ATTR) + " " + cssClass;
        element.setAttribute(CSS_CLASS_ATTR, cssClasses);
    }

    private static Element wrapSelectToErrorContainer(Document document, Node selectNode, String selectName, boolean required) {
        Node parentNode = selectNode.getParentNode();
        if (parentNode instanceof Element && ERROR_CONTAINER.equalsIgnoreCase(parentNode.getNodeName())) {
            return (Element) parentNode;
        }
        log.debug("Wrapping select[name='" + selectName + "'] to " + ERROR_CONTAINER);
        parentNode.removeChild(selectNode);
        Element div = document.createElement(ERROR_CONTAINER);
        if (required) {
            addClassAttribute(div, "requiredWrapper");
        }
        div.appendChild(selectNode);
        parentNode.appendChild(div);
        return div;
    }
}
