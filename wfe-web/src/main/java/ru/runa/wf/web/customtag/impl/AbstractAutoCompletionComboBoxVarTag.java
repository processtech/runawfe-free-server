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

package ru.runa.wf.web.customtag.impl;

import java.util.List;

import javax.servlet.jsp.PageContext;

import org.apache.commons.beanutils.BeanUtils;
import org.apache.ecs.html.Input;
import org.apache.ecs.html.Script;
import org.apache.ecs.html.Select;
import org.apache.ecs.html.TD;
import org.apache.ecs.html.TR;
import org.apache.ecs.html.Table;

import ru.runa.wfe.user.Actor;
import ru.runa.wfe.user.User;
import ru.runa.wfe.var.IVariableProvider;

import com.google.common.base.Throwables;

/**
 * Created 08.07.2005
 * 
 */
public abstract class AbstractAutoCompletionComboBoxVarTag extends AbstractActorComboBoxVarTag {
    private final static String COMBOBOX_SCRIPT_RENDERED_ATTRIBUTE_NAME = AbstractAutoCompletionComboBoxVarTag.class.getName();

    private final static String formName = "'processForm'";

    private final static String inputNameSuffix = "InputAutoCompletion";

    private final static String displayNameListName = "displayNameList";

    private final static String valueListName = "valueList";

    private String createInputAndSelectHtml(String selectName, List<Actor> actors, Actor defaultSelectedActor) throws Exception {
        Table table = new Table();
        table.setBorder(0);
        table.setCellPadding(0);
        table.setCellSpacing(0);
        TD inputTD = new TD();
        table.addElement(new TR(inputTD));
        Input input = new Input();
        inputTD.addElement(input);
        input.setName(selectName + inputNameSuffix);
        input.setBorder(0);
        input.setStyle("font-size:10pt;width:34ex;");
        input.setType("text");
        input.setOnKeyUp("handleKeyUp('" + selectName + "'," + selectName + displayNameListName + "," + selectName + valueListName + ");");
        TD selectTD = new TD();
        table.addElement(new TR(selectTD));
        Select select = createSelect(selectName, actors, defaultSelectedActor);
        selectTD.addElement(select);
        select.setOnChange("handleSelectClick('" + selectName + "');");
        return table.toString();
    }

    private String createAutoCompletionScript(String selectName, List<Actor> actors, PageContext pageContext) {
        Script script = new Script();
        script.setLanguage("javascript");
        script.setType("text/javascript");

        StringBuffer displayNames = new StringBuffer();
        StringBuffer values = new StringBuffer();
        try {
            for (int i = 0; i < actors.size(); i++) {
                if (i != 0) {
                    displayNames.append(",");
                    values.append(",");
                }
                String value = BeanUtils.getProperty(actors.get(i), getActorPropertyToUse());
                String displayName = BeanUtils.getProperty(actors.get(i), getActorPropertyToDisplay());
                values.append("'").append(value).append("'");
                displayNames.append("'").append(displayName).append("'");
            }
        } catch (Exception e) {
            throw Throwables.propagate(e);
        }
        script.addElement("var " + selectName + displayNameListName + " = Array(" + displayNames.toString() + ");\n");
        script.addElement("var " + selectName + valueListName + " = Array(" + values.toString() + ");\n");

        displayComboboxScript(script, pageContext);

        return script.toString();
    }

    private void displayComboboxScript(Script script, PageContext pageContext) {
        if (pageContext != null && pageContext.getAttribute(COMBOBOX_SCRIPT_RENDERED_ATTRIBUTE_NAME) == null) {
            script.addElement("function handleKeyUp(selectName,actorList,codeList) {\n inputElement = document.forms["
                    + formName
                    + "].elements[selectName+'"
                    + inputNameSuffix
                    + "'];\n selectElement = document.forms["
                    + formName
                    + "].elements[selectName];\n strText = '^'+inputElement.value;\n var numShown = 0;\n re = new RegExp(strText,'gi');\n selectElement.length = 0;\n for(i = 0; i < actorList.length; i++) {\n if(actorList[i].search(re) != -1) {\n selectElement[numShown] = new Option(actorList[i], codeList[i]);\n numShown++;\n }\n }\n }\n");
            script.addElement("function handleSelectClick(selectName) {\n selectElement = document.forms["
                    + formName
                    + "].elements[selectName];\n selectedValue = selectElement.options[selectElement.selectedIndex].text;\n selectedValue = selectedValue.replace(/_/g, '-') ; }\n");
            // script.addElement("function initpage() { inputElement = document.forms["
            // + formName + "]." + inputNameSuffix +
            // "; handleKeyUp(); inputElement.focus(); }\n");
            pageContext.setAttribute(COMBOBOX_SCRIPT_RENDERED_ATTRIBUTE_NAME, Boolean.TRUE);
        }
    }

    @Override
    public String getHtml(User user, String varName, Object varValue, PageContext pageContext, IVariableProvider variableProvider) throws Exception {
        StringBuffer htmlContent = new StringBuffer();
        List<Actor> actors = getActors(user, varName);
        Actor defaultActor = user.getActor();
        htmlContent.append(createAutoCompletionScript(varName, actors, pageContext));
        htmlContent.append(createInputAndSelectHtml(varName, actors, defaultActor));
        return htmlContent.toString();
    }

}
