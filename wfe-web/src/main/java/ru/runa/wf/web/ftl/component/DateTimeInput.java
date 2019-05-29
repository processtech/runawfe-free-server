package ru.runa.wf.web.ftl.component;

import java.util.Date;

import ru.runa.wfe.commons.CalendarUtil;
import ru.runa.wfe.commons.ftl.FormComponent;
import ru.runa.wfe.var.dto.WfVariable;

/**
 * @deprecated code moved to {@link InputVariable}.
 * 
 * @author dofs
 * @since 4.0
 */
@Deprecated
public class DateTimeInput extends FormComponent {
    private static final long serialVersionUID = 1L;

    @Override
    protected Object renderRequest() {
        String variableName = getParameterAsString(0);
        String view = getParameterAsString(1);
        WfVariable variable = variableProvider.getVariableNotNull(variableName);
        Date date = (Date) variable.getValue();
        String html = "";
        if ("date".equals(view)) {
            html += "<input type=\"text\" class=\"inputDate\" name=\"" + variableName + "\" style=\"width: 100px;\" ";
            if (date != null) {
                html += "value=\"" + CalendarUtil.formatDate(date) + "\" ";
            }
            html += "/>";
        }
        if ("time".equals(view)) {
            html += "<input type=\"text\" class=\"inputTime\" name=\"" + variableName + "\" style=\"width: 50px;\" ";
            if (date != null) {
                html += "value=\"" + CalendarUtil.formatTime(date) + "\" ";
            }
            html += "/>";
        }
        if ("datetime".equals(view)) {
            html += "<input type=\"text\" class=\"inputDateTime\" name=\"" + variableName + "\" style=\"width: 150px;\" ";
            if (date != null) {
                html += "value=\"" + CalendarUtil.formatDateTime(date) + "\" ";
            }
            html += "/>";
        }
        if (html.length() == 0) {
            log.warn("No HTML built (" + variableName + ") for variable " + variable);
        }
        return html;
    }

}
