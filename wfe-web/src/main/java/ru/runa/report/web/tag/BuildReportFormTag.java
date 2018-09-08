/*
 * This file is part
 of the RUNA WFE project.
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
package ru.runa.report.web.tag;

import com.google.common.base.Function;
import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import java.util.List;
import java.util.Map;
import org.apache.ecs.html.Input;
import org.apache.ecs.html.Option;
import org.apache.ecs.html.Select;
import org.apache.ecs.html.TD;
import org.apache.ecs.html.TH;
import org.apache.ecs.html.TR;
import org.apache.ecs.html.Table;
import org.tldgen.annotations.BodyContent;
import ru.runa.common.web.HTMLUtils;
import ru.runa.common.web.MessagesCommon;
import ru.runa.common.web.Resources;
import ru.runa.common.web.StrutsMessage;
import ru.runa.common.web.form.IdForm;
import ru.runa.common.web.tag.SecuredObjectFormTag;
import ru.runa.report.web.MessagesReport;
import ru.runa.report.web.action.BuildReportAction;
import ru.runa.wfe.report.dto.WfReport;
import ru.runa.wfe.report.dto.WfReportParameter;
import ru.runa.wfe.report.impl.ReportGenerationType;
import ru.runa.wfe.report.impl.ReportGenerationType.ReportGenerationTypeVisitor;
import ru.runa.wfe.report.impl.ReportParameterModel;
import ru.runa.wfe.report.impl.ReportParameterModel.ListValuesData;
import ru.runa.wfe.security.Permission;
import ru.runa.wfe.service.delegate.Delegates;

@org.tldgen.annotations.Tag(bodyContent = BodyContent.EMPTY, name = "buildReportForm")
public class BuildReportFormTag extends SecuredObjectFormTag {
    private static final long serialVersionUID = -3361459425268889410L;

    public static final String BUILD_TYPE = "reportBuildType";

    @Override
    protected void fillFormData(TD tdFormElement) {
        tdFormElement.addElement(HTMLUtils.createInput("HIDDEN", IdForm.ID_INPUT_NAME, Long.toString(getIdentifiableId())));
        WfReport report = getSecuredObject();
        Table table = new Table();
        table.setClass(Resources.CLASS_LIST_TABLE);
        tdFormElement.addElement(table);
        TR tr = new TR();
        tr.addElement(new TH(MessagesCommon.HEADER_PARAMETER_NAME.message(pageContext)).setClass(Resources.CLASS_LIST_TABLE_TH));
        tr.addElement(new TH(MessagesCommon.HEADER_PARAMETER_VALUE.message(pageContext)).setClass(Resources.CLASS_LIST_TABLE_TH));
        table.addElement(tr);
        Map<String, String> parameterDescriptions = Maps.newHashMap();
        for (WfReportParameter parameter : report.getParameters()) {
            parameterDescriptions.put(parameter.getInternalName(), parameter.getDescription());
            ReportParameterCreateModelOperation createModelOperation = new ReportParameterCreateModelOperation(getUser());
            ReportParameterModel model = parameter.getType().processBy(createModelOperation, parameter);
            String paramHtmlName = "reportParam" + parameter.getPosition();
            String value = this.pageContext.getRequest().getParameter(paramHtmlName);
            if (model.isSimpleInputProperty()) {
                Input input = HTMLUtils.createInput(model.getHtmlInputType(), paramHtmlName,
                        !Strings.isNullOrEmpty(value) ? value : model.getValue());
                input.setClass(model.getHtmlInputClass());
                table.addElement(HTMLUtils.createRow(parameter.getUserName(), input));
            } else if (model.isFlagProperty()) {
                boolean checked = !Strings.isNullOrEmpty(value) ? "true".equals(value) : model.getValue() != null;
                table.addElement(HTMLUtils.createCheckboxRow(parameter.getUserName(), paramHtmlName, checked, true, false));
            } else if (model.isFixedListProperty()) {
                List<Option> options = Lists.transform(model.getListValues(), new Function<ListValuesData, Option>() {

                    @Override
                    public Option apply(ListValuesData input) {
                        String value = input.getValue() != null ? input.getValue().toString() : null;
                        return HTMLUtils.createOption(value, input.getValueName(), false);
                    }
                });
                table.addElement(HTMLUtils.createSelectRow(parameter.getUserName(), paramHtmlName, options.toArray(new Option[options.size()]), true,
                        model.isRequired()));
            }
        }
        table.addElement(HTMLUtils.createRow(MessagesReport.GENERATE_LABEL.message(pageContext), createBuildTypeSelect()));
    }

    private Select createBuildTypeSelect() {
        String selectedValue = pageContext.getRequest().getParameter(BUILD_TYPE);
        Option[] options = new Option[ReportGenerationType.values().length];
        for (int i = 0; i < options.length; i++) {
            ReportGenerationType value = ReportGenerationType.values()[i];
            String description = value.processBy(new ReportGenerationTypeNameVisitor()).message(pageContext);
            options[i] = HTMLUtils.createOption(value.toString(), description, value.toString().equals(selectedValue));
        }
        Select select = new Select(BUILD_TYPE, options);
        select.setID(BUILD_TYPE);
        return select;
    }

    @Override
    protected String getTitle() {
        return MessagesReport.TITLE_BUILD_REPORT.message(pageContext);
    }

    @Override
    public String getAction() {
        return BuildReportAction.ACTION_PATH;
    }

    @Override
    protected String getSubmitButtonName() {
        return MessagesReport.BUTTON_BUILD_REPORT.message(pageContext);
    }

    @Override
    protected Permission getSubmitPermission() {
        return Permission.LIST;
    }

    @Override
    protected WfReport getSecuredObject() {
        return Delegates.getReportService().getReportDefinition(getUser(), getIdentifiableId());
    }

    static final class ReportGenerationTypeNameVisitor implements ReportGenerationTypeVisitor<StrutsMessage> {

        @Override
        public StrutsMessage onHtml() {
            return MessagesReport.GENERATE_HTML;
        }

        @Override
        public StrutsMessage onExcel() {
            return MessagesReport.GENERATE_EXCEL;
        }

        @Override
        public StrutsMessage onRtf() {
            return MessagesReport.GENERATE_RTF;
        }

        @Override
        public StrutsMessage onPdf() {
            return MessagesReport.GENERATE_PDF;
        }

        @Override
        public StrutsMessage onDocx() {
            return MessagesReport.GENERATE_DOCX;
        }

    }
}
