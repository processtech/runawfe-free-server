package ru.runa.af.web.tag;

import com.google.common.base.Strings;
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
import org.tldgen.annotations.BodyContent;
import ru.runa.af.web.MessagesExecutor;
import ru.runa.af.web.action.UpdateSubstitutionCriteriaAction;
import ru.runa.af.web.form.SubstitutionCriteriaForm;
import ru.runa.af.web.orgfunction.FunctionDef;
import ru.runa.af.web.orgfunction.ParamDef;
import ru.runa.af.web.orgfunction.SubstitutionCriteriaDefinitions;
import ru.runa.common.web.HTMLUtils;
import ru.runa.common.web.MessagesCommon;
import ru.runa.common.web.Resources;
import ru.runa.common.web.tag.SecuredObjectFormTag;
import ru.runa.wfe.extension.orgfunction.ParamRenderer;
import ru.runa.wfe.security.Permission;
import ru.runa.wfe.security.SecuredObject;
import ru.runa.wfe.service.delegate.Delegates;
import ru.runa.wfe.ss.SubstitutionCriteria;

@org.tldgen.annotations.Tag(bodyContent = BodyContent.JSP, name = "updateSubstitutionCriteriaForm")
public class UpdateSubstitutionCriteriaFormTag extends SecuredObjectFormTag {
    private static final long serialVersionUID = 1L;

    @Override
    protected SecuredObject getSecuredObject() {
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
    protected Permission getSubmitPermission() {
        return null;
    }

    @Override
    public String getSubmitButtonName() {
        return MessagesCommon.BUTTON_SAVE.message(pageContext);
    }

    @Override
    protected String getTitle() {
        return MessagesExecutor.TITLE_SUBSTITUTION_EDIT.message(pageContext);
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
            SubstitutionCriteria substitutionCriteria = getIdentifiableId() != null ? Delegates.getSubstitutionService().getCriteria(getUser(),
                    getIdentifiableId()) : null;
            Table table = new Table();
            table.setID("paramsTable");
            table.setClass(Resources.CLASS_LIST_TABLE);
            boolean enabled = substitutionCriteria == null;
            String criteriaName = substitutionCriteria != null ? substitutionCriteria.getName() : "";
            Input nameInput = HTMLUtils.createInput(SubstitutionCriteriaForm.NAME_INPUT_NAME, criteriaName, true, true);
            table.addElement(HTMLUtils.createRow(MessagesExecutor.LABEL_SUBSTITUTION_CRITERIA_NAME.message(pageContext), nameInput));
            String criteriaType = null;
            if (substitutionCriteria != null) {
                criteriaType = substitutionCriteria.getClass().getName();
            }
            Option[] typeOptions = getTypeOptions(criteriaType);
            if (Strings.isNullOrEmpty(criteriaType) && typeOptions.length > 0) {
                criteriaType = typeOptions[0].getValue();
            }
            table.addElement(HTMLUtils.createSelectRow(MessagesExecutor.LABEL_SUBSTITUTION_CRITERIA_TYPE.message(pageContext),
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
                String value = definitions.get(i).getClassName();
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
            selectorHref.addElement(MessagesExecutor.LABEL_SUBSTITUTION_SELECT.message(pageContext));
            span.addElement(selectorHref);
        }
        return span;
    }

}
