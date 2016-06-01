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
package ru.runa.wf.web.action;

import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.struts.Globals;
import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;
import org.apache.struts.action.ActionMessage;

import ru.runa.common.WebResources;
import ru.runa.common.web.MessagesException;
import ru.runa.common.web.ProfileHttpSessionHelper;
import ru.runa.common.web.Resources;
import ru.runa.common.web.action.ActionBase;
import ru.runa.wf.web.FormSubmissionUtils;
import ru.runa.wfe.form.Interaction;
import ru.runa.wfe.task.TaskDoesNotExistException;
import ru.runa.wfe.user.Profile;
import ru.runa.wfe.user.User;
import ru.runa.wfe.validation.ValidationException;

import com.google.common.base.Strings;

/**
 * Created on 15.12.2005
 *
 */
public abstract class BaseProcessFormAction extends ActionBase {

    @Override
    public ActionForward execute(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response) {
        Map<String, String> userInputErrors = null;
        ActionForward forward = null;
        try {
            User user = getLoggedUser(request);
            Profile profile = ProfileHttpSessionHelper.getProfile(request.getSession());
            // TODO fix bug when working from 2 browser tabs (token saved in
            // user session!)
            if (request.getSession().getAttribute(Globals.TRANSACTION_TOKEN_KEY) == null || isTokenValid(request, true)) {
                saveToken(request);
                Long processId = executeProcessFromAction(request, form, mapping, profile);
                if (WebResources.isAutoShowForm()) {
                    forward = AutoShowFormHelper.getNextActionForward(user, mapping, profile, processId);
                }
                if (forward == null) {
                    forward = mapping.findForward(Resources.FORWARD_SUCCESS);
                }
                addMessage(request, getMessage(processId));
            } else {
                forward = new ActionForward("/manage_tasks.do", true);
                log.warn(getLoggedUser(request) + " will be forwarded to tasklist due invalid token");
            }
        } catch (TaskDoesNotExistException e) {
            // In this case we must go to success forwarding, because of this
            // task is absent and form can't be displayed
            addError(request, e);
            forward = mapping.findForward(Resources.FORWARD_SUCCESS);
        } catch (ValidationException e) {
            userInputErrors = e.getConcatenatedFieldErrors("<br>");
            if (e.getGlobalErrors().size() > 0) {
                for (String message : e.getGlobalErrors()) {
                    if (Strings.isNullOrEmpty(message)) {
                        addError(request, new ActionMessage(MessagesException.MESSAGE_VALIDATION_ERROR.getKey()));
                    } else {
                        // we are working with localized string
                        addError(request, new ActionMessage(message, false));
                    }
                }
            } else {
                addError(request, new ActionMessage(MessagesException.MESSAGE_VALIDATION_ERROR.getKey()));
            }
            forward = getErrorForward(mapping, form);
        } catch (Exception e) {
            addError(request, e);
            forward = getErrorForward(mapping, form);
        }
        FormSubmissionUtils.saveUserFormInput(request, form, userInputErrors);
        return forward;
    }

    protected Map<String, Object> getFormVariables(Long definitionId, HttpServletRequest request, ActionForm actionForm, Interaction interaction) {
        return FormSubmissionUtils.extractVariables(definitionId, request, actionForm, interaction);
    }

    protected abstract ActionMessage getMessage(Long processId);

    protected abstract Long executeProcessFromAction(HttpServletRequest request, ActionForm form, ActionMapping mapping, Profile profile);

    protected abstract ActionForward getErrorForward(ActionMapping mapping, ActionForm actionForm);
}
