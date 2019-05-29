package ru.runa.common.web.html.format;

import javax.servlet.jsp.PageContext;

import org.apache.ecs.Entities;
import org.apache.ecs.html.Input;
import org.apache.ecs.html.TD;

import ru.runa.common.web.form.TableViewSetupForm;
import ru.runa.wfe.presentation.filter.FilterCriteria;

import com.google.common.base.Strings;

/**
 * Only dates supported now.
 * 
 * Created on 14.09.2005
 */
public class DateFilterTDFormatter extends FilterTDFormatter {

    @Override
    public void formatTd(TD filterInputTd, PageContext pageContext, FilterCriteria filterCriteria, int fieldIndex) {
        int inputsCount = 2;
        String[] stringConditions = filterCriteria.getFilterTemplates();
        for (int j = 0; j < inputsCount; j++) {
            if (j != 0) {
                filterInputTd.addElement(Entities.NBSP);
            }
            String html = "<input class=\"inputDateTime\" name=\"" + TableViewSetupForm.FILTER_CRITERIA + "\" style=\"width: 100px;\" ";
            if (!Strings.isNullOrEmpty(stringConditions[j])) {
                html += "value=\"" + stringConditions[j] + "\" ";
            }
            html += "/>";
            filterInputTd.addElement(html);
            filterInputTd.addElement(new Input(Input.HIDDEN, TableViewSetupForm.FILTER_POSITIONS, String.valueOf(fieldIndex)));
        }
    }
}
