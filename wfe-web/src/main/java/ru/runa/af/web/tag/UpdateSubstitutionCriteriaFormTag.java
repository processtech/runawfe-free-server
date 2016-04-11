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

import java.util.List;

import javax.servlet.jsp.PageContext;

import org.apache.ecs.Element;
import org.apache.ecs.Entities;
import org.apache.ecs.html.A;
import org.apache.ecs.html.Input;
import org.apache.ecs.html.Option;
import org.apache.ecs.html.Span;
import org.apache.ecs.html.TD;
import org.apache.ecs.html.TR;
import org.apache.ecs.html.Table;

import ru.runa.af.web.action.UpdateSubstitutionCriteriaAction;
import ru.runa.af.web.form.SubstitutionCriteriaForm;
import ru.runa.af.web.orgfunction.FunctionDef;
import ru.runa.af.web.orgfunction.ParamDef;
import ru.runa.af.web.orgfunction.SubstitutionCriteriaDefinitions;
import ru.runa.common.web.HTMLUtils;
import ru.runa.common.web.Messages;
import ru.runa.common.web.Resources;
import ru.runa.common.web.tag.IdentifiableFormTag;
import ru.runa.wfe.extension.orgfunction.ParamRenderer;
import ru.runa.wfe.security.Identifiable;
import ru.runa.wfe.security.Permission;
import ru.runa.wfe.service.delegate.Delegates;
import ru.runa.wfe.ss.SubstitutionCriteria;

import com.google.common.base.Strings;

/**
 * Created on 14.08.2010
 * 
 * @jsp.tag name = "updateSubstitutionCriteriaForm" body-content = "JSP"
 */
public class UpdateSubstitutionCriteriaFormTag extends IdentifiableFormTag {
    private static final long serialVersionUID = 1L;

    @Override
    protected Identifiable getIdentifiable() {
        throw new UnsupportedOperationException();
    }

    @Override
    public void fillFormData(TD tdFormElement) {
        StringBuffer paramsDiv = new StringBuffer("<div id='rh' style='display: none;'>");
        List<FunctionDef> functions = SubstitutionCriteriaDefinitions.getAll();
        int i = 0;
        for (FunctionDef functionDef : functions) {
            paramsDiv.append("<div id='").append(functionDef.getClassName()).append("'>");
            for (ParamDef paramDef : functionDef.getParams()) {
                paramsDiv.append("<div>");
                paramsDiv.append("<span>").append(paramDef.getMessage(pageContext)).append("</span>");
                paramsDiv.append("<span>").append(createEditElement(paramDef.getRenderer(), pageContext, "", i, false)).append("</span>");
                paramsDiv.append("</div>");
            }
            paramsDiv.append("</div>");
            i++;
        }
        paramsDiv.append("</div>");
        tdFormElement.addElement(paramsDiv.toString());

        SubstitutionTableBuilder builder = new SubstitutionTableBuilder(pageContext);
        tdFormElement.addElement(builder.buildTable());
    }

    @Override
    protected Permission getPermission() {
        return null;
    }

    @Override
    public String getFormButtonName() {
        return Messages.getMessage(Messages.BUTTON_SAVE, pageContext);
    }

    @Override
    protected String getTitle() {
        return Messages.getMessage("substitutioncriteria.edit.title", pageContext);
    }

    @Override
    public String getAction() {
        return UpdateSubstitutionCriteriaAction.UPDATE_ACTION;
    }

    class SubstitutionTableBuilder {

        private final PageContext pageContext;

        public SubstitutionTableBuilder(PageContext pageContext) {
            this.pageContext = pageContext;
        }

        public Table buildTable() {
            SubstitutionCriteria substitutionCriteria = (getIdentifiableId() != null) ? Delegates.getSubstitutionService().getCriteria(getUser(),
                    getIdentifiableId()) : null;
            Table table = new Table();
            table.setID("paramsTable");
            table.setClass(Resources.CLASS_LIST_TABLE);
            boolean enabled = substitutionCriteria == null;
            String criteriaName = "";
            if (substitutionCriteria != null) {
                criteriaName = substitutionCriteria.getName();
            }
            table.addElement(HTMLUtils.createInputRow(Messages.getMessage(Messages.LABEL_SUBSTITUTION_CRITERIA_NAME, pageContext),
                    SubstitutionCriteriaForm.NAME_INPUT_NAME, criteriaName, true, true));
            String criteriaType = null;
            if (substitutionCriteria != null) {
                criteriaType = substitutionCriteria.getClass().getName();
            }
            Option[] typeOptions = getTypeOptions(criteriaType);
            if (Strings.isNullOrEmpty(criteriaType) && typeOptions.length > 0) {
                criteriaType = typeOptions[0].getValue();
            }
            table.addElement(HTMLUtils.createSelectRow(Messages.getMessage(Messages.LABEL_SUBSTITUTION_CRITERIA_TYPE, pageContext),
                    SubstitutionCriteriaForm.TYPE_INPUT_NAME, typeOptions, enabled, false));
            FunctionDef functionDef = SubstitutionCriteriaDefinitions.getByClassName(criteriaType);
            if (functionDef != null) {
                for (int i = 0; i < functionDef.getParams().size(); i++) {
                    ParamDef paramDef = functionDef.getParams().get(i);
                    String value = "";
                    if (substitutionCriteria != null) {
                        value = substitutionCriteria.getConfiguration();
                    }
                    table.addElement(createParameterTR(i, paramDef.getMessage(pageContext),
                            createEditElement(paramDef.getRenderer(), pageContext, value, i, true)));
                }
            }
            return table;
        }

        private Option[] getTypeOptions(String selectedValue) {
            List<FunctionDef> definitions = SubstitutionCriteriaDefinitions.getAll();
            Option[] options = new Option[definitions.size()];
            for (int i = 0; i < options.length; i++) {
                options[i] = new Option(definitions.get(i).getClassName());
                options[i].addElement(definitions.get(i).getLabel());
            }
            for (Option option : options) {
                if (option.getValue().equals(selectedValue)) {
                    option.setSelected(true);
                    break;
                }
            }
            return options;
        }

    }

    private TR createParameterTR(int index, String label, Element element) {
        TR tr = new TR();
        tr.addAttribute("paramIndex", index);
        tr.addElement(new TD(label).setClass(ru.runa.common.web.Resources.CLASS_LIST_TABLE_TD));
        tr.addElement(new TD(element).setClass(ru.runa.common.web.Resources.CLASS_LIST_TABLE_TD));
        return tr;
    }

    private Element createEditElement(ParamRenderer renderer, PageContext pageContext, String value, int index, boolean enabled) {
        Span span = new Span();
        Input input = new Input(Input.TEXT, SubstitutionCriteriaForm.CONF_INPUT_NAME, value);
        input.setClass(Resources.CLASS_REQUIRED);
        input.setStyle("width: 300px");
        input.setDisabled(!enabled);
        input.addAttribute("paramIndex", index);
        span.addElement(input);
        if (renderer.hasJSEditor()) {
            span.addElement(Entities.NBSP);
            String url = "javascript:editParameter('" + index + "','" + renderer.getClass().getName() + "');";
            A selectorHref = new A(url);
            selectorHref.addElement(Messages.getMessage("substitution.select", pageContext));
            span.addElement(selectorHref);
        }
        return span;
    }

}
