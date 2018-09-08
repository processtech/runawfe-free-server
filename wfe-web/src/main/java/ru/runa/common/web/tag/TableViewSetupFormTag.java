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
package ru.runa.common.web.tag;

import com.google.common.collect.Maps;
import java.util.Map;
import javax.servlet.jsp.JspException;
import javax.servlet.jsp.JspWriter;
import org.apache.commons.logging.LogFactory;
import org.apache.ecs.Entities;
import org.apache.ecs.html.A;
import org.apache.ecs.html.Form;
import org.apache.ecs.html.IMG;
import org.apache.ecs.html.Input;
import org.apache.ecs.html.Option;
import org.apache.ecs.html.Select;
import org.apache.ecs.html.TD;
import org.apache.ecs.html.TH;
import org.apache.ecs.html.TR;
import org.apache.ecs.html.Table;
import org.tldgen.annotations.Attribute;
import org.tldgen.annotations.BodyContent;
import ru.runa.common.web.Commons;
import ru.runa.common.web.HTMLUtils;
import ru.runa.common.web.Messages;
import ru.runa.common.web.MessagesBatch;
import ru.runa.common.web.MessagesCommon;
import ru.runa.common.web.ProfileHttpSessionHelper;
import ru.runa.common.web.Resources;
import ru.runa.common.web.action.TableViewSetupFormAction;
import ru.runa.common.web.form.BatchPresentationForm;
import ru.runa.common.web.form.TableViewSetupForm;
import ru.runa.common.web.html.format.FilterFormatsFactory;
import ru.runa.common.web.html.format.FilterTDFormatter;
import ru.runa.wf.web.MessagesProcesses;
import ru.runa.wfe.commons.web.PortletUrlType;
import ru.runa.wfe.presentation.BatchPresentation;
import ru.runa.wfe.presentation.BatchPresentationConsts;
import ru.runa.wfe.presentation.ClassPresentation;
import ru.runa.wfe.presentation.FieldDescriptor;
import ru.runa.wfe.presentation.FieldFilterMode;
import ru.runa.wfe.presentation.FieldState;
import ru.runa.wfe.service.delegate.Delegates;
import ru.runa.wfe.user.Profile;
import ru.runa.wfe.user.User;

@org.tldgen.annotations.Tag(bodyContent = BodyContent.JSP, name = "tableViewSetupForm")
public class TableViewSetupFormTag extends AbstractReturningTag implements BatchedTag {
    private static final long serialVersionUID = 6534068425896008626L;
    private static boolean groupBySubprocessEnabled = ru.runa.common.WebResources.isGroupBySubprocessEnabled();

    private String batchPresentationId;
    private String excelExportAction;

    @Override
    public String getBatchPresentationId() {
        return batchPresentationId;
    }

    @Attribute(required = true, rtexprvalue = true)
    @Override
    public void setBatchPresentationId(String batchPresentationId) {
        this.batchPresentationId = batchPresentationId;
    }

    public String getExcelExportAction() {
        return excelExportAction;
    }

    @Attribute(required = false, rtexprvalue = true)
    public void setExcelExportAction(String excelExportAction) {
        this.excelExportAction = excelExportAction;
    }

    public String getApplyButtonName() {
        return MessagesCommon.BUTTON_APPLY.message(pageContext);
    }

    public String getSaveButtonName() {
        return MessagesCommon.BUTTON_SAVE.message(pageContext);
    }

    public String getCreateNewButtonName() {
        return MessagesCommon.BUTTON_SAVE_AS.message(pageContext);
    }

    public String getRemoveButtonName() {
        return MessagesCommon.BUTTON_REMOVE.message(pageContext);
    }

    @Override
    public BatchPresentation getBatchPresentation() {
        throw new UnsupportedOperationException("getBatchPresentation() is unsupported");
    }

    protected BatchPresentation getActiveBatchPresentation() {
        Profile profile = ProfileHttpSessionHelper.getProfile(pageContext.getSession());
        return profile.getActiveBatchPresentation(getBatchPresentationId());
    }

    @Override
    public int doStartTag() throws JspException {
        Form form = new Form(getAction(), getMethod());
        form.setName(TableViewSetupForm.FORM_NAME);
        form.addElement(new Input(Input.HIDDEN, TableViewSetupForm.BATCH_PRESENTATION_ID, getBatchPresentationId()));
        form.addElement(new Input(Input.HIDDEN, TableViewSetupForm.RETURN_ACTION, getReturnAction()));
        BatchPresentation activeBatchPresentation = getActiveBatchPresentation();
        Table table = buildBatchTable(activeBatchPresentation);
        form.addElement(table);

        if (activeBatchPresentation.getType().isWithPaging()) {
            TR tr = new TR();
            table.addElement(tr);
            TD td = new TD();
            td.setColSpan(6);
            tr.addElement(td);
            td.addElement(MessagesBatch.VIEW_SIZE.message(pageContext));
            td.addElement(Entities.NBSP);
            Select selectSize = new Select(TableViewSetupForm.VIEW_SIZE_NAME);
            int[] allowedSizes = BatchPresentationConsts.getAllowedViewSizes();
            for (int allowedSize : allowedSizes) {
                boolean isSelected = activeBatchPresentation.getRangeSize() == allowedSize;
                selectSize.addElement(HTMLUtils.createOption(allowedSize, String.valueOf(allowedSize), isSelected));
            }
            td.addElement(selectSize);
        }
        addAdditionalInfo(table, activeBatchPresentation);
        addSaveSection(table, activeBatchPresentation);

        JspWriter writer = pageContext.getOut();
        form.output(writer);
        return super.doStartTag();
    }

    // allows to add addition presentation elements
    protected void addAdditionalInfo(Table table, BatchPresentation activeBatchPresentation) {
    }

    private void addSaveSection(Table table, BatchPresentation activeBatchPresentation) {
        User user = Commons.getUser(pageContext.getSession());
        boolean isAdmin = Delegates.getExecutorService().isAdministrator(user);

        TR tr = new TR();
        table.addElement(tr);
        TD td = new TD();
        td.setColSpan(6);
        tr.addElement(td);

        if (isAdmin || !activeBatchPresentation.isShared()) {
            // user cannot update shared batch presentation
            if (!activeBatchPresentation.isDefault()) {
                Input saveInput = new Input(Input.SUBMIT, TableViewSetupFormAction.PARAMETER_NAME, getSaveButtonName());
                saveInput.setClass(Resources.CLASS_BUTTON);
                td.addElement(saveInput);
                td.addElement(Entities.NBSP);
            }
        }

        Input saveAsInput = new Input(Input.SUBMIT, TableViewSetupFormAction.PARAMETER_NAME, getCreateNewButtonName());
        saveAsInput.setClass(Resources.CLASS_BUTTON);
        td.addElement(saveAsInput);

        td.addElement(Entities.NBSP);
        td.addElement(new Input(Input.TEXT, TableViewSetupForm.SAVE_AS_NAME, "").setClass(Resources.CLASS_BUTTON));

        if (isAdmin || !activeBatchPresentation.isShared()) {
            // user cannot remove shared batch presentation
            if (!activeBatchPresentation.isDefault()) {
                td.addElement(Entities.NBSP);
                Input deleteInput = new Input(Input.SUBMIT, TableViewSetupFormAction.PARAMETER_NAME, getRemoveButtonName());
                deleteInput.setClass(Resources.CLASS_BUTTON);
                td.addElement(deleteInput);
            }
        }

        if (isAdmin) {
            // admin can set shared type for batch presentation
            td.addElement(Entities.NBSP);
            td.addElement(MessagesBatch.SHARED_SELECT_LABEL.message(pageContext));
            td.addElement(Entities.NBSP);
            Select selectShared = new Select(TableViewSetupForm.SHARED_TYPE_NAME);
            selectShared.addElement(HTMLUtils.createOption(TableViewSetupForm.SHARED_TYPE_NO, MessagesBatch.SHARED_OPTION_NO.message(pageContext),
                    !activeBatchPresentation.isShared()));
            selectShared.addElement(HTMLUtils.createOption(TableViewSetupForm.SHARED_TYPE_SHARED,
                    MessagesBatch.SHARED_OPTION_YES.message(pageContext), activeBatchPresentation.isShared()));
            td.addElement(selectShared);
        } else {
            // user can only "save as" shared batch presentation as private
            td.addElement(new Input(Input.HIDDEN, TableViewSetupForm.SHARED_TYPE_NAME, TableViewSetupForm.SHARED_TYPE_NO));
        }
        if (excelExportAction != null) {
            A exportLink = new A();
            Map<String, String> parameters = Maps.newHashMap();
            parameters.put(BatchPresentationForm.BATCH_PRESENTATION_ID, batchPresentationId);
            exportLink.setHref(Commons.getActionUrl(excelExportAction, parameters, pageContext, PortletUrlType.Render));
            exportLink.setClass(Resources.CLASS_LINK);
            exportLink.setStyle("display: block; float: right;");
            IMG img = new IMG(Commons.getUrl(Resources.EXCEL_ICON, pageContext, PortletUrlType.Resource), 0);
            img.setWidth(21).setHeight(21).setStyle("vertical-align: middle;");
            exportLink.addElement(img);
            exportLink.addElement(Entities.NBSP);
            exportLink.addElement(MessagesProcesses.BUTTON_EXPORT_EXCEL.message(pageContext));
            td.addElement(exportLink);
        }
    }

    private Table buildBatchTable(BatchPresentation batchPresentation) {
        Table table = new Table();
        table.setClass(Resources.CLASS_VIEW_SETUP_TABLE);
        try {
            FieldDescriptor[] displayedFields = batchPresentation.getDisplayFields();
            table.addElement(getHeaderRow());
            for (int i = 0; i < displayedFields.length; ++i) {
                if (displayedFields[i].displayName.startsWith(ClassPresentation.filterable_prefix)) {
                    continue;
                }
                if (!displayedFields[i].displayName.startsWith(ClassPresentation.editable_prefix)
                        && displayedFields[i].fieldState == FieldState.ENABLED) {
                    table.addElement(buildViewRow(batchPresentation, displayedFields[i].fieldIdx, i));
                }
            }
            for (FieldDescriptor f : batchPresentation.getHiddenFields()) {
                if (f.displayName.startsWith(ClassPresentation.filterable_prefix)) {
                    continue;
                }
                if (!f.displayName.startsWith(ClassPresentation.editable_prefix) && f.fieldState == FieldState.ENABLED) {
                    table.addElement(buildViewRow(batchPresentation, f.fieldIdx, -1));
                }
            }
            for (FieldDescriptor f : batchPresentation.getAllFields()) {
                if (f.displayName.startsWith(ClassPresentation.editable_prefix) && f.fieldState == FieldState.ENABLED
                        || f.displayName.startsWith(ClassPresentation.filterable_prefix) && groupBySubprocessEnabled) {
                    table.addElement(buildViewRow(batchPresentation, f.fieldIdx, -1));
                }
            }
        } catch (Exception e) {
            LogFactory.getLog(getClass()).warn("Unable to buildBatchTable", e);
            table.addElement(e.toString());
        }
        return table;
    }

    protected TR buildViewRow(BatchPresentation batchPresentation, int fieldIdx, int fieldDisplayPosition) {
        int noneOptionPosition = batchPresentation.getAllFields().length + 1;
        TR tr = new TR();

        FieldDescriptor field = batchPresentation.getAllFields()[fieldIdx];
        boolean isEditable = field.displayName.startsWith(ClassPresentation.editable_prefix);
        boolean isDynamic = field.displayName.startsWith(ClassPresentation.removable_prefix);
        boolean isFilterable = field.displayName.startsWith(ClassPresentation.filterable_prefix);
        tr.addAttribute("field", field.displayName);

        { // field name section
            TD td;
            if (isEditable) {
                td = new TD(Messages.getMessage(field.displayName.substring(field.displayName.lastIndexOf(':') + 1), pageContext) + ":");
                td.addElement(new Input(Input.TEXT, TableViewSetupForm.EDITABLE_FIELDS, ""));
            } else if (isDynamic) {
                int end = field.displayName.lastIndexOf(':');
                int begin = field.displayName.lastIndexOf(':', end - 1) + 1;
                td = new TD(new Input(Input.CHECKBOX, TableViewSetupForm.REMOVABLE_FIELD_IDS, fieldIdx).setChecked(true));
                td.addElement(Messages.getMessage(field.displayName.substring(begin, end), pageContext) + ":"
                        + field.displayName.substring(field.displayName.lastIndexOf(':') + 1));
            } else if (isFilterable) {
                Input groupingInput = new Input(Input.CHECKBOX, TableViewSetupForm.GROUPING_POSITIONS, fieldIdx);
                if (batchPresentation.isFieldGroupped(fieldIdx)) {
                    groupingInput.setChecked(true);
                }
                td = new TD(groupingInput);
                td.addElement(MessagesCommon.LABEL_GROUP_BY_ID.message(pageContext));
            } else {
                td = new TD(Messages.getMessage(field.displayName, pageContext));
            }
            td.addElement(new Input(Input.HIDDEN, TableViewSetupForm.IDS_INPUT_NAME, String.valueOf(fieldIdx)));
            tr.addElement(td);
        }
        if (isEditable || isFilterable) { // Editable fields havn't fields for
            // sorting/filtering e t.c.
            for (int idx = 0; idx < 5; ++idx) {
                tr.addElement(new TD());
            }
            return tr;
        }
        { // field display position section
            if (field.isShowable()) {
                Select displayFieldPositionSelect = new Select(TableViewSetupForm.DISPLAY_POSITIONS,
                        createPositionOptions(batchPresentation, fieldIdx));
                tr.addElement(new TD(displayFieldPositionSelect));
                if (fieldDisplayPosition >= 0 && !isEditable) {
                    displayFieldPositionSelect.selectOption(fieldDisplayPosition + 1);
                } else {
                    displayFieldPositionSelect.selectOption(noneOptionPosition);
                }
            } else {
                tr.addElement(new TD());
            }
        }
        {// field sorting/groupping section
            if (field.sortable) {
                Select sortingModeSelect = new Select(TableViewSetupForm.SORTING_MODE_NAMES, createSortModeOptions());
                tr.addElement(new TD(sortingModeSelect));
                Select sortingFieldPositoinSelect = new Select(TableViewSetupForm.SORTING_POSITIONS,
                        createPositionOptions(batchPresentation, fieldIdx));
                tr.addElement(new TD(sortingFieldPositoinSelect));
                selectSortingMode(batchPresentation, fieldIdx, sortingModeSelect, sortingFieldPositoinSelect);

                Input groupingInput = new Input(Input.CHECKBOX, TableViewSetupForm.GROUPING_POSITIONS, fieldIdx);
                if (batchPresentation.isFieldGroupped(fieldIdx)) {
                    groupingInput.setChecked(true);
                }
                tr.addElement(
                        new TD(groupingInput).addElement(new Input(Input.HIDDEN, TableViewSetupForm.SORTING_FIELD_IDS, String.valueOf(fieldIdx))));
            } else {
                for (int idx = 0; idx < 3; ++idx) {
                    tr.addElement(new TD());
                }
            }
        }

        // filtering
        if (field.filterMode != FieldFilterMode.NONE) {
            FilterTDFormatter formatter = FilterFormatsFactory.getFormatter(batchPresentation.getAllFields()[fieldIdx].fieldType);
            tr.addElement(formatter
                    .format(pageContext, batchPresentation.getFieldFilteredCriteria(fieldIdx), fieldIdx, batchPresentation.isFieldFiltered(fieldIdx))
                    .addElement(new Input(Input.HIDDEN, TableViewSetupForm.FILTERING_FIELD_IDS, String.valueOf(fieldIdx))));
        } else {
            tr.addElement(new TD());
        }

        return tr;
    }

    protected void selectSortingMode(BatchPresentation batchPresentation, int fieldIndex, Select sortingModeSelect,
            Select sortingFieldPositoinSelect) {
        if (batchPresentation.isSortingField(fieldIndex)) {
            int sortedFieldIndex = batchPresentation.getSortingFieldPosition(fieldIndex);
            if (batchPresentation.getFieldsToSortModes()[sortedFieldIndex]) {
                sortingModeSelect.selectOption(0);
            } else {
                sortingModeSelect.selectOption(1);
            }
            sortingFieldPositoinSelect.selectOption(sortedFieldIndex + 1);
        } else {
            sortingModeSelect.selectOption(0);
            sortingFieldPositoinSelect.selectOption(0);
        }
    }

    protected Option[] createPositionOptions(BatchPresentation batchPresentation, int fieldIdx) {
        FieldDescriptor[] fields = batchPresentation.getAllFields();
        if (fields[fieldIdx].displayName.startsWith(ClassPresentation.editable_prefix)) {
            return new Option[] { HTMLUtils.createOption("-1", MessagesBatch.OPTION_NONE.message(pageContext), false) };
        }
        int fieldsCount = fields.length;
        for (int i = fields.length - 1; i >= 0; --i) {
            if (fields[i].displayName.startsWith(ClassPresentation.editable_prefix) || fields[i].fieldState != FieldState.ENABLED) {
                --fieldsCount;
            }
        }

        Option[] positionOptions = new Option[fieldsCount + 1];
        positionOptions[0] = HTMLUtils.createOption("-1", MessagesBatch.OPTION_NONE.message(pageContext), false);
        for (int position = 1; position < positionOptions.length; position++) {
            String positionString = String.valueOf(position).intern();
            String positionIdString = String.valueOf(position - 1).intern();
            positionOptions[position] = HTMLUtils.createOption(positionIdString, positionString, false);
        }
        return positionOptions;
    }

    protected Option[] createSortModeOptions() {
        return new Option[] {
                HTMLUtils.createOption(TableViewSetupForm.ASC_SORTING_MODE, MessagesBatch.SORT_ASC.message(pageContext), false),
                HTMLUtils.createOption(TableViewSetupForm.DSC_SORTING_MODE, MessagesBatch.SORT_DESC.message(pageContext), false)
        };
    }

    private TR getHeaderRow() {
        TR tr = new TR();
        String[] headerNames = { MessagesBatch.FIELD_NAMES.message(pageContext), MessagesBatch.DISPLAY_POSITION.message(pageContext),
                MessagesBatch.SORTING_TYPE.message(pageContext), MessagesBatch.SORTING_POSITION.message(pageContext),
                MessagesBatch.GROUPING.message(pageContext), MessagesBatch.FILTER_CRITERIA.message(pageContext)
                        + " <a href='javascript:showFiltersHelp();' style='color: red; text-decoration: none;'>*</a>" };
        for (String headerName : headerNames) {
            tr.addElement(new TH(headerName));
        }
        return tr;
    }

    @Override
    public String getAction() {
        return Commons.getActionUrl(TableViewSetupFormAction.ACTION_PATH, pageContext, PortletUrlType.Action);
    }
}
