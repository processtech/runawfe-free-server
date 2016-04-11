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

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.struts.action.ActionForm;
import org.apache.struts.action.ActionForward;
import org.apache.struts.action.ActionMapping;

import ru.runa.common.web.action.ActionBase;
import ru.runa.common.web.form.FileForm;
import ru.runa.wf.web.ProcessTypesIterator;
import ru.runa.wfe.user.User;

import com.google.common.base.Strings;
import com.google.common.collect.Lists;

/**
 * Created on 14.10.2004
 * 
 */
public abstract class BaseDeployProcessDefinitionAction extends ActionBase {
    
    public static final String TYPE_TYPE = "type";
    public static final String TYPE_SEL = "typeSel";
    public static final String TYPE_ATTRIBUTES = "TypeAttributes";
    public static final String TYPE_DEFAULT = "_default_type_";
    public static final String TYPE_UPDATE_CURRENT_VERSION = "updateCurrentVersion";

    protected abstract void doAction(User user, FileForm fileForm, List<String> processType, boolean isUpdateCurrentVersion) throws Exception;

    @Override
    public ActionForward execute(ActionMapping mapping, ActionForm form, HttpServletRequest request, HttpServletResponse response) {
        String paramType = request.getParameter(TYPE_TYPE);
        String paramTypeSelected = request.getParameter(TYPE_SEL);
        boolean isUpdateCurrentVersion = (request.getParameter(TYPE_UPDATE_CURRENT_VERSION) != null);
        
        Map<String, String> typeParamsHolder = new HashMap<String, String>();
        typeParamsHolder.put(TYPE_TYPE, paramType);
        typeParamsHolder.put(TYPE_SEL, paramTypeSelected);
        request.setAttribute(TYPE_ATTRIBUTES, typeParamsHolder);

        List<String> fullType;

        FileForm fileForm = (FileForm) form;
        prepare(fileForm);
        try {
            ProcessTypesIterator iter = new ProcessTypesIterator(getLoggedUser(request));
            if (paramTypeSelected == null || paramTypeSelected.equals(TYPE_DEFAULT)) {
                if (Strings.isNullOrEmpty(paramType)) {
                    throw new ProcessDefinitionTypeNotPresentException();
                }
                fullType = Lists.newArrayList(paramType);
            } else {
                String[] selectedType = iter.getItem(Integer.parseInt(paramTypeSelected));
                fullType = Lists.newArrayList(selectedType);
                if (!Strings.isNullOrEmpty(paramType)) {
                    fullType.add(paramType);
                }
            }
            doAction(getLoggedUser(request), fileForm, fullType, isUpdateCurrentVersion);
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
