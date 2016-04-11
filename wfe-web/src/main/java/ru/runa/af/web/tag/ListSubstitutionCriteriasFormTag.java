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
package ru.runa.af.web.tag;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.jsp.PageContext;

import org.apache.ecs.html.A;
import org.apache.ecs.html.Input;
import org.apache.ecs.html.TD;
import org.apache.ecs.html.TH;
import org.apache.ecs.html.TR;
import org.apache.ecs.html.Table;

import ru.runa.af.web.action.DeleteSubstitutionCriteriasAction;
import ru.runa.af.web.action.UpdateSubstitutionCriteriaAction;
import ru.runa.common.web.Commons;
import ru.runa.common.web.ConfirmationPopupHelper;
import ru.runa.common.web.Messages;
import ru.runa.common.web.Resources;
import ru.runa.common.web.form.IdForm;
import ru.runa.common.web.form.SubstitutionCriteriasForm;
import ru.runa.wfe.commons.web.PortletUrlType;
import ru.runa.wfe.commons.web.WebUtils;
import ru.runa.wfe.security.Permission;
import ru.runa.wfe.service.ExecutorService;
import ru.runa.wfe.service.SubstitutionService;
import ru.runa.wfe.service.delegate.Delegates;
import ru.runa.wfe.ss.Substitution;
import ru.runa.wfe.ss.SubstitutionCriteria;
import ru.runa.wfe.user.Actor;

/**
 * Created on 24.04.2012
 * 
 * @jsp.tag name = "listSubstitutionCriteriasForm" body-content = "JSP"
 */
public class ListSubstitutionCriteriasFormTag extends UpdateSystemBaseFormTag {
    private static final long serialVersionUID = 1L;
    private String substitutionCriteriaIds;

    /**
     * @jsp.attribute required = "false" rtexprvalue = "true"
     */
    public String getSubstitutionCriteriaIds() {
        return substitutionCriteriaIds;
    }

    public void setSubstitutionCriteriaIds(String substitutionCriteriaIds) {
        this.substitutionCriteriaIds = substitutionCriteriaIds;
    }

    private static ArrayList<Long> arrayFromString(String string) {
        ArrayList<Long> result = new ArrayList<Long>();
        if (string == null || string.isEmpty()) {
            return result;
        }
        String[] strings = string.replace("[", "").replace("]", "").split(",");
        for (int i = 0; i < strings.length; i++) {
            result.add(Long.valueOf(strings[i].trim()));
        }
        return result;
    }

    @Override
    protected void fillFormData(TD tdFormElement) {
        SubstitutionCriteriaTableBuilder tableBuilder = new SubstitutionCriteriaTableBuilder(pageContext);
        tdFormElement.addElement(tableBuilder.buildTable());
        tdFormElement.addElement(new Input(Input.HIDDEN, SubstitutionCriteriasForm.REMOVE_METHOD_INPUT_NAME,
                SubstitutionCriteriasForm.REMOVE_METHOD_CONFIRM));
        if (substitutionCriteriaIds != null && !substitutionCriteriaIds.isEmpty()) {
            String message = Messages.getMessage(Messages.LABEL_SUBSTITUTION_CRITERIA_USED_BY, pageContext) + ":<ul>";
            ArrayList<Long> ids = arrayFromString(substitutionCriteriaIds);
            ArrayList<Substitution> substitutions = new ArrayList<Substitution>();
            SubstitutionService substitutionService = Delegates.getSubstitutionService();
            for (Long id : ids) {
                SubstitutionCriteria substitutionCriteria = substitutionService.getCriteria(getUser(), id);
                substitutions.addAll(substitutionService.getSubstitutionsByCriteria(getUser(), substitutionCriteria));
            }
            for (Substitution substitution : substitutions) {
                ExecutorService executorService = Delegates.getExecutorService();
                Actor actor = executorService.getExecutor(getUser(), substitution.getActorId());
                message += "<li>" + actor.getFullName() + " (" + actor.getName() + ")</li>";
            }
            message += "</ul>" + Messages.getMessage(Messages.CONF_POPUP_REMOVE_SUBSTITUTION_CRITERIA, pageContext);
            getForm().addAttribute("id", "substitutionCriteriasForm");
            String javascript = "onload = function() {" + "openSubstitutionCriteriasConfirmPopup('" + message + "', '"
                    + SubstitutionCriteriasForm.REMOVE_METHOD_ALL + "', '"
                    + Messages.getMessage(Messages.CONF_POPUP_SUBSTITUTION_CRITERIA_BUTTON_ALL, pageContext) + "', '"
                    + SubstitutionCriteriasForm.REMOVE_METHOD_ONLY + "', '"
                    + Messages.getMessage(Messages.CONF_POPUP_SUBSTITUTION_CRITERIA_BUTTON_ONLY, pageContext) + "', '"
                    + Messages.getMessage(Messages.CONF_POPUP_BUTTON_CANCEL, pageContext) + "');" + "}";
            tdFormElement.addElement(WebUtils.getScript(javascript));
        }
    }

    @Override
    protected Permission getPermission() {
        return Permission.UPDATE_PERMISSIONS;
    }

    @Override
    public String getFormButtonName() {
        return Messages.getMessage(Messages.BUTTON_REMOVE, pageContext);
    }

    @Override
    protected String getTitle() {
        return Messages.getMessage(Messages.TITLE_SUBSTITUTION_CRITERIA, pageContext);
    }

    @Override
    public String getAction() {
        return DeleteSubstitutionCriteriasAction.ACTION_PATH;
    }

    @Override
    public String getConfirmationPopupParameter() {
        return ConfirmationPopupHelper.REMOVE_SUBSTITUTION_CRITERIA_PARAMETER;
    }

    private class SubstitutionCriteriaTableBuilder {
        private final PageContext pageContext;

        public SubstitutionCriteriaTableBuilder(PageContext pageContext) {
            this.pageContext = pageContext;
        }

        public Table buildTable() {
            Table table = new Table();
            table.setClass(Resources.CLASS_PERMISSION_TABLE);
            table.addElement(createTableHeaderTR());
            SubstitutionService substitutionService = Delegates.getSubstitutionService();
            List<SubstitutionCriteria> substitutionCriterias = substitutionService.getAllCriterias(getUser());
            ArrayList<Long> ids = arrayFromString(substitutionCriteriaIds);
            for (SubstitutionCriteria substitutionCriteria : substitutionCriterias) {
                table.addElement(createTR(substitutionCriteria, ids.contains(substitutionCriteria.getId())));
            }
            return table;
        }

        private TR createTableHeaderTR() {
            TR tr = new TR();
            tr.addElement(new TH().setClass(Resources.CLASS_LIST_TABLE_TH));
            tr.addElement(new TH(Messages.getMessage(Messages.LABEL_SUBSTITUTION_CRITERIA_NAME, pageContext)).setClass(Resources.CLASS_LIST_TABLE_TH));
            tr.addElement(new TH(Messages.getMessage(Messages.LABEL_SUBSTITUTION_CRITERIA_TYPE, pageContext)).setClass(Resources.CLASS_LIST_TABLE_TH));
            tr.addElement(new TH(Messages.getMessage(Messages.LABEL_SUBSTITUTION_CRITERIA_CONF, pageContext)).setClass(Resources.CLASS_LIST_TABLE_TH));
            return tr;
        }

        private TR createTR(SubstitutionCriteria substitutionCriteria, boolean enabled) {
            TR tr = new TR();
            Input input = new Input(Input.CHECKBOX, SubstitutionCriteriasForm.IDS_INPUT_NAME, String.valueOf(substitutionCriteria.getId()));
            input.setChecked(enabled);
            tr.addElement(new TD(input).setClass(Resources.CLASS_LIST_TABLE_TD));
            {
                Map<String, Object> params = new HashMap<String, Object>();
                params.put(IdForm.ID_INPUT_NAME, substitutionCriteria.getId());
                A editHref = new A(Commons.getActionUrl(UpdateSubstitutionCriteriaAction.EDIT_ACTION, params, pageContext, PortletUrlType.Action));
                editHref.addElement(substitutionCriteria.getName());
                tr.addElement(new TD(editHref).setClass(Resources.CLASS_LIST_TABLE_TD));
            }
            String label = Delegates.getSystemService().getLocalized(getUser(), substitutionCriteria.getClass().getName());
            tr.addElement(new TD(label).setClass(Resources.CLASS_LIST_TABLE_TD));
            tr.addElement(new TD(substitutionCriteria.getConfiguration()).setClass(Resources.CLASS_LIST_TABLE_TD));
            return tr;
        }
    }
}
