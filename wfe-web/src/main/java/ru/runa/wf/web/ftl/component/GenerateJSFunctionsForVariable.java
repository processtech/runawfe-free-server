package ru.runa.wf.web.ftl.component;

import ru.runa.common.WebResources;
import ru.runa.wfe.var.VariableDefinition;
import ru.runa.wfe.var.dto.WfVariable;
import ru.runa.wfe.var.format.BigDecimalFormat;
import ru.runa.wfe.var.format.BooleanFormat;
import ru.runa.wfe.var.format.DateFormat;
import ru.runa.wfe.var.format.DateTimeFormat;
import ru.runa.wfe.var.format.DoubleFormat;
import ru.runa.wfe.var.format.ExecutorFormat;
import ru.runa.wfe.var.format.FileFormat;
import ru.runa.wfe.var.format.FormatCommons;
import ru.runa.wfe.var.format.FormattedTextFormat;
import ru.runa.wfe.var.format.HiddenFormat;
import ru.runa.wfe.var.format.ListFormat;
import ru.runa.wfe.var.format.LongFormat;
import ru.runa.wfe.var.format.MapFormat;
import ru.runa.wfe.var.format.ProcessIdFormat;
import ru.runa.wfe.var.format.StringFormat;
import ru.runa.wfe.var.format.TextFormat;
import ru.runa.wfe.var.format.TimeFormat;
import ru.runa.wfe.var.format.UserTypeFormat;
import ru.runa.wfe.var.format.VariableFormat;
import ru.runa.wfe.var.format.VariableFormatVisitor;

/**
 * Component for generation javascript code, which must be called if new variable added on form (for example on list row add).
 */
public class GenerateJSFunctionsForVariable implements VariableFormatVisitor<String, WfVariable> {

    private boolean onDateCalled;
    private boolean onTimeCalled;
    private boolean onDateTimeCalled;
    private boolean onFileCalled;

    @Override
    public String onDate(DateFormat dateFormat, WfVariable variable) {
        if (!onDateCalled) {
            onDateCalled = true;
            return "$('.inputDate').filter(filterTemplatesElements).datepicker({ dateFormat: 'dd.mm.yy', buttonImage: '/wfe/images/calendar.gif' });";
        }
        return "";
    }

    @Override
    public String onTime(TimeFormat timeFormat, WfVariable variable) {
        if (!onTimeCalled) {
            onTimeCalled = true;
            return "$('.inputTime').filter(filterTemplatesElements).timepicker({ ampm: false, seconds: false });";
        }
        return "";
    }

    @Override
    public String onDateTime(DateTimeFormat dateTimeFormat, WfVariable variable) {
        if (!onDateTimeCalled) {
            onDateTimeCalled = true;
            return "$('.inputDateTime').filter(filterTemplatesElements).datetimepicker({ dateFormat: 'dd.mm.yy' });";
        }
        return "";
    }

    @Override
    public String onExecutor(ExecutorFormat executorFormat, WfVariable variable) {
        return "";
    }

    @Override
    public String onBoolean(BooleanFormat booleanFormat, WfVariable variable) {
        return "";
    }

    @Override
    public String onBigDecimal(BigDecimalFormat bigDecimalFormat, WfVariable variable) {
        return "";
    }

    @Override
    public String onDouble(DoubleFormat doubleFormat, WfVariable variable) {
        return "";
    }

    @Override
    public String onLong(LongFormat longFormat, WfVariable variable) {
        return "";
    }

    @Override
    public String onFile(FileFormat fileFormat, WfVariable variable) {
        if (!WebResources.isAjaxFileInputEnabled()) {
            return "";
        }
        if (!onFileCalled) {
            onFileCalled = true;
            return "$('.dropzone').filter(filterTemplatesElements).each(function () { initFileInput($(this)) });";
        }
        return "";
    }

    @Override
    public String onHidden(HiddenFormat hiddenFormat, WfVariable variable) {
        return "";
    }

    @Override
    public String onList(ListFormat listFormat, WfVariable variable) {
        return "init" + variable.getDefinition().getScriptingNameWithoutDots() + "(this);";
    }

    @Override
    public String onMap(MapFormat mapFormat, WfVariable variable) {
        return "init" + variable.getDefinition().getScriptingNameWithoutDots() + "(this);";
    }

    @Override
    public String onProcessId(ProcessIdFormat processIdFormat, WfVariable variable) {
        return "";
    }

    @Override
    public String onString(StringFormat stringFormat, WfVariable variable) {
        return "";
    }

    @Override
    public String onTextString(TextFormat textFormat, WfVariable variable) {
        return "";
    }

    @Override
    public String onFormattedTextString(FormattedTextFormat textFormat, WfVariable context) {
        return "";
    }

    @Override
    public String onUserType(UserTypeFormat userTypeFormat, WfVariable variable) {
        StringBuilder componentJsHandlers = new StringBuilder();
        for (VariableDefinition variableDefinition : userTypeFormat.getUserType().getAttributes()) {
            VariableFormat nestedFormat = FormatCommons.create(variableDefinition);
            WfVariable componentVariable = ViewUtil.createUserTypeComponentVariable(variable, variableDefinition, null);
            componentJsHandlers.append(nestedFormat.processBy(this, componentVariable));
        }
        return componentJsHandlers.toString();
    }

    @Override
    public String onOther(VariableFormat variableFormat, WfVariable variable) {
        return "";
    }

}
