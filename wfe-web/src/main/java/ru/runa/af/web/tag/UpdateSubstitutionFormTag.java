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
import org.tldgen.annotations.Attribute;
import org.tldgen.annotations.BodyContent;

import ru.runa.af.web.MessagesExecutor;
import ru.runa.af.web.action.UpdateSubstitutionAction;
import ru.runa.af.web.form.SubstitutionForm;
import ru.runa.af.web.orgfunction.FunctionDef;
import ru.runa.af.web.orgfunction.ParamDef;
import ru.runa.af.web.orgfunction.SubstitutionDefinitions;
import ru.runa.common.web.HTMLUtils;
import ru.runa.common.web.MessagesCommon;
import ru.runa.common.web.Resources;
import ru.runa.common.web.tag.IdentifiableFormTag;
import ru.runa.wfe.execution.logic.OrgFunctionSwimlaneInitializer;
import ru.runa.wfe.execution.logic.SwimlaneInitializerHelper;
import ru.runa.wfe.extension.orgfunction.ParamRenderer;
import ru.runa.wfe.security.Identifiable;
import ru.runa.wfe.security.Permission;
import ru.runa.wfe.service.SubstitutionService;
import ru.runa.wfe.service.delegate.Delegates;
import ru.runa.wfe.ss.Substitution;
import ru.runa.wfe.ss.SubstitutionCriteria;
import ru.runa.wfe.ss.TerminatorSubstitution;

@org.tldgen.annotations.Tag(bodyContent = BodyContent.JSP, name = "updateSubstitutionForm")
public class UpdateSubstitutionFormTag extends IdentifiableFormTag {
    private static final long serialVersionUID = 9096797376521541558L;
    private Substitution substitution;
    private boolean terminator;
    private Long actorId;

    public boolean isTerminator() {
        return terminator;
    }

    @Attribute(required = false, rtexprvalue = true)
    public void setTerminator(boolean terminator) {
        this.terminator = terminator;
    }

    public Long getActorId() {
        return actorId;
    }

    @Attribute(required = false, rtexprvalue = true)
    public void setActorId(Long actorId) {
        this.actorId = actorId;
    }

    @Override
    protected Identifiable getIdentifiable() {
        throw new UnsupportedOperationException();
    }

    @Override
    public void fillFormData(TD tdFormElement) {
        StringBuffer paramsDiv = new StringBuffer("<div id='rh' style='display: none;'>");
        List<FunctionDef> functions = SubstitutionDefinitions.getAll();
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
        return (substitution != null ? MessagesCommon.BUTTON_SAVE : MessagesCommon.BUTTON_ADD).message(pageContext);
    }

    @Override
    protected String getTitle() {
        SubstitutionService substitutionService = Delegates.getSubstitutionService();
        if (getIdentifiableId() != null) {
            substitution = substitutionService.getSubstitution(getUser(), getIdentifiableId());
            terminator = substitution instanceof TerminatorSubstitution;
        }
        return (terminator ? MessagesExecutor.TITLE_TERMINATOR_EDIT : MessagesExecutor.TITLE_SUBSTITUTION_EDIT).message(pageContext);
    }

    @Override
    public String getAction() {
        return UpdateSubstitutionAction.UPDATE_ACTION;
    }

    class SubstitutionTableBuilder {

        private final PageContext pageContext;

        public SubstitutionTableBuilder(PageContext pageContext) {
            this.pageContext = pageContext;
        }

        public Table buildTable() {
            Table table = new Table();
            table.setID("paramsTable");
            table.setClass(Resources.CLASS_LIST_TABLE);
            String criteriaId = null;
            if (substitution != null && substitution.getCriteria() != null) {
                criteriaId = substitution.getCriteria().getId().toString();
            }
            table.addElement(HTMLUtils.createSelectRow(MessagesExecutor.LABEL_SUBSTITUTORS_CRITERIA.message(pageContext),
                    SubstitutionForm.CRITERIA_ID_INPUT_NAME, getCriteriaOptions(criteriaId), true, false));
            table.addElement(HTMLUtils.createCheckboxRow(MessagesExecutor.LABEL_SUBSTITUTORS_ENABLED.message(pageContext),
                    SubstitutionForm.ENABLED_INPUT_NAME, substitution == null || substitution.isEnabled(), true, false));
            if (!terminator) {
                String function = "";
                OrgFunctionSwimlaneInitializer swimlaneInitializer = null;
                if (substitution != null) {
                    try {
                        swimlaneInitializer = (OrgFunctionSwimlaneInitializer) SwimlaneInitializerHelper.parse(substitution.getOrgFunction());
                        function = swimlaneInitializer.getOrgFunctionClassName();
                    } catch (Exception e) {
                        log.warn(e);
                    }
                }
                Option[] functionOptions = getFunctionOptions(function);
                if (function.length() == 0 && functionOptions.length > 0) {
                    function = functionOptions[0].getValue();
                }
                table.addElement(HTMLUtils.createSelectRow(MessagesExecutor.LABEL_SWIMLANE_ORGFUNCTION.message(pageContext),
                        SubstitutionForm.FUNCTION_INPUT_NAME, functionOptions, true, true));
                if (function.length() > 0) {
                    FunctionDef functionDef = SubstitutionDefinitions.getByClassNameNotNull(function);
                    if (functionDef != null) {
                        for (int i = 0; i < functionDef.getParams().size(); i++) {
                            String value = "";
                            if (swimlaneInitializer != null) {
                                value = swimlaneInitializer.getParameterNames()[i];
                            }
                            ParamDef paramDef = functionDef.getParams().get(i);
                            table.addElement(createParameterTR(i, paramDef.getMessage(pageContext),
                                    createEditElement(paramDef.getRenderer(), pageContext, value, i, true)));
                        }
                    }
                }
            }
            table.addElement(new Input(Input.HIDDEN, SubstitutionForm.TERMINATOR_INPUT_NAME, String.valueOf(terminator)));
            table.addElement(new Input(Input.HIDDEN, SubstitutionForm.ACTOR_ID_INPUT_NAME, String.valueOf(actorId)));
            return table;
        }

        private Option[] getCriteriaOptions(String selectedValue) {
            List<SubstitutionCriteria> criterias = Delegates.getSubstitutionService().getAllCriterias(getUser());
            Option[] options = new Option[criterias.size() + 1];
            options[0] = HTMLUtils.createOption("", MessagesExecutor.SUBSTITUTION_ALWAYS.message(pageContext), "".equals(selectedValue));
            for (int i = 1; i < options.length; i++) {
                String value = String.valueOf(criterias.get(i - 1).getId());
                options[i] = HTMLUtils.createOption(value, criterias.get(i - 1).getName(), value.equals(selectedValue));
            }
            return options;
        }

        private Option[] getFunctionOptions(String selectedValue) {
            List<FunctionDef> definitions = SubstitutionDefinitions.getAll();
            Option[] options = new Option[definitions.size()];
            for (int i = 0; i < options.length; i++) {
                String value = String.valueOf(definitions.get(i).getClassName());
                options[i] = HTMLUtils.createOption(value, definitions.get(i).getLabel(), value.equals(selectedValue));
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
        Input input = new Input(Input.TEXT, SubstitutionForm.PARAMS_INPUT_NAME, value);
        input.setClass(Resources.CLASS_REQUIRED);
        input.setStyle("width: 300px");
        input.setDisabled(!enabled);
        input.addAttribute("paramIndex", index);
        span.addElement(input);
        if (renderer.hasJSEditor()) {
            span.addElement(Entities.NBSP);
            String url = "javascript:editParameter('" + index + "','" + renderer.getClass().getName() + "');";
            A selectorHref = new A(url);
            selectorHref.addElement(MessagesExecutor.LABEL_SUBSTITUTION_SELECT.message(pageContext));
            span.addElement(selectorHref);
        }
        return span;
    }

}
