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

import java.util.List;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;

import ru.runa.common.web.CategoriesSelectUtils;
import ru.runa.common.web.action.ActionBase;
import ru.runa.common.web.form.FileForm;
import ru.runa.wf.web.tag.RedeployDefinitionFormTag;
import ru.runa.wfe.user.User;

/**
 * Created on 14.10.2004
 *
 */
public abstract class BaseDeployProcessDefinitionAction extends ActionBase {

    protected abstract void doAction(User user, FileForm fileForm, List<String> categories, boolean isUpdateCurrentVersion,
            boolean isUpdateAllIncompleteProcesses) throws Exception;

    @Override
    public ActionForward execute(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response) {
        boolean isUpdateCurrentVersion = request.getParameter(RedeployDefinitionFormTag.TYPE_UPDATE_CURRENT_VERSION) != null;
        boolean isUpdateAllIncompleteProcesses = request.getParameter(RedeployDefinitionFormTag.TYPE_UPDATE_ALL_INCOMLETE_PROCESSES) != null;

        FileForm fileForm = (FileForm) form;
        prepare(fileForm);
        try {
            doAction(getLoggedUser(request), fileForm, CategoriesSelectUtils.extract(request), isUpdateCurrentVersion, isUpdateAllIncompleteProcesses);
        } catch (Exception e) {
            addError(request, e);
            return getErrorForward(mapping);
        }
        return getSuccessAction(mapping);
    }

    protected abstract ActionForward getSuccessAction(ActionMapping mapping);

    protected abstract ActionForward getErrorForward(ActionMapping mapping);

    protected abstract void prepare(FileForm fileForm);
}
