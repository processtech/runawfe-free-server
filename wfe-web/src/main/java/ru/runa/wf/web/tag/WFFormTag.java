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
package ru.runa.wf.web.tag;

import org.apache.ecs.StringElement;
import org.apache.ecs.html.Form;
import org.apache.ecs.html.Input;
import org.apache.ecs.html.P;
import org.apache.ecs.html.Script;
import org.apache.ecs.html.TD;
import org.apache.struts.Globals;
import org.apache.struts.taglib.html.Constants;

import ru.runa.common.web.ActionExceptionHelper;
import ru.runa.common.web.Resources;
import ru.runa.common.web.tag.TitledFormTag;
import ru.runa.wf.web.FormSubmissionUtils;
import ru.runa.wf.web.MessagesProcesses;
import ru.runa.wfe.form.Interaction;
import ru.runa.wfe.task.TaskDoesNotExistException;

import com.google.common.base.Charsets;

public abstract class WFFormTag extends TitledFormTag {
    private static final long serialVersionUID = 1L;
    public static final String FORM_NAME = "processForm";

    private boolean formButtonVisible;

    @Override
    protected void fillFormElement(TD tdFormElement) {
        try {
            Interaction interaction = getInteraction();
            String form = buildForm(interaction);
            if (interaction.getCssData() != null) {
                StringBuffer styles = new StringBuffer("<style>");
                styles.append(new String(interaction.getCssData(), Charsets.UTF_8));
                styles.append("</style>");
                tdFormElement.addElement(new StringElement(styles.toString()));
            }
            if (interaction.isUseJSValidation()) {
                log.debug("Using javascript validation.");
                String javaScript = XWorkJavascriptValidator.getJavascript(getUser(), interaction.getValidationData());
                getForm().setOnSubmit("return validateForm_".concat(FORM_NAME).concat("();"));
                tdFormElement.addElement(new StringElement(javaScript));
            }
            if (interaction.getProcessScriptData() != null) {
                Script script = new Script();
                script.setLanguage("javascript");
                script.setType("text/javascript");
                script.addElement(new StringElement(new String(interaction.getProcessScriptData(), Charsets.UTF_8)));
                tdFormElement.addElement(script);
            }
            if (interaction.getFormScriptData() != null) {
                Script script = new Script();
                script.setLanguage("javascript");
                script.setType("text/javascript");
                script.addElement(new StringElement(new String(interaction.getFormScriptData(), Charsets.UTF_8)));
                tdFormElement.addElement(script);
            }
            tdFormElement.setClass(Resources.CLASS_BOX_BODY + " taskform " + interaction.getNodeId().replaceAll(" ", ""));
            tdFormElement.addElement(new StringElement(form));
            Input formNodeIdInput = new Input();
            formNodeIdInput.setType(Input.HIDDEN);
            formNodeIdInput.addAttribute("name", FormSubmissionUtils.FORM_NODE_ID_KEY);
            formNodeIdInput.addAttribute("value", interaction.getNodeId());
            getForm().addElement(formNodeIdInput);
            formButtonVisible = true;
        } catch (TaskDoesNotExistException e) {
            log.warn(e.getMessage());
            P p = new P();
            tdFormElement.addElement(p);
            p.setClass(Resources.CLASS_ERROR);
            String message = ActionExceptionHelper.getErrorMessage(e, pageContext);
            p.addElement(message);
        }
        getForm().setEncType(Form.ENC_UPLOAD);
        getForm().setAcceptCharset(Charsets.UTF_8.name());
        getForm().setName(FORM_NAME);
        getForm().setID(FORM_NAME);
        Input tokenInput = new Input();
        tokenInput.setType(Input.HIDDEN);
        tokenInput.addAttribute("name", Constants.TOKEN_KEY);
        tokenInput.addAttribute("value", pageContext.getSession().getAttribute(Globals.TRANSACTION_TOKEN_KEY));
        getForm().addElement(tokenInput);
    }

    @Override
    public boolean isFormButtonVisible() {
        return formButtonVisible;
    }

    @Override
    protected String getFormButtonName() {
        return MessagesProcesses.BUTTON_COMPLETE.message(pageContext);
    }

    protected abstract Long getDefinitionId();

    protected abstract Interaction getInteraction();

    protected abstract String buildForm(Interaction interaction);
}
