package ru.runa.wf.web.ftl.component;

import ru.runa.common.WebResources;
import ru.runa.wfe.var.VariableDefinition;
import ru.runa.wfe.var.format.BigDecimalFormat;
import ru.runa.wfe.var.format.BooleanFormat;
import ru.runa.wfe.var.format.DateFormat;
import ru.runa.wfe.var.format.DateTimeFormat;
import ru.runa.wfe.var.format.DoubleFormat;
import ru.runa.wfe.var.format.ExecutorFormat;
import ru.runa.wfe.var.format.FileFormat;
import ru.runa.wfe.var.format.FormatCommons;
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

public class GenerateJSFunctionsForVariable implements VariableFormatVisitor<String, Object> {

    private boolean onDateCalled;
    private boolean onTimeCalled;
    private boolean onDateTimeCalled;
    private boolean onFileCalled;

    @Override
    public String onDate(DateFormat dateFormat, Object variable) {
        if (!onDateCalled) {
            onDateCalled = true;
            return "$('.inputDate').datepicker({ dateFormat: 'dd.mm.yy', buttonImage: '/wfe/images/calendar.gif' });";
        }
        return "";
    }

    @Override
    public String onTime(TimeFormat timeFormat, Object variable) {
        if (!onTimeCalled) {
            onTimeCalled = true;
            return "$('.inputTime').timepicker({ ampm: false, seconds: false });";
        }
        return "";
    }

    @Override
    public String onDateTime(DateTimeFormat dateTimeFormat, Object variable) {
        if (!onDateTimeCalled) {
            onDateTimeCalled = true;
            return "$('.inputDateTime').datetimepicker({ dateFormat: 'dd.mm.yy' });";
        }
        return "";
    }

    @Override
    public String OnExecutor(ExecutorFormat executorFormat, Object variable) {
        return "";
    }

    @Override
    public String onBoolean(BooleanFormat booleanFormat, Object variable) {
        return "";
    }

    @Override
    public String onBigDecimal(BigDecimalFormat bigDecimalFormat, Object variable) {
        return "";
    }

    @Override
    public String onDouble(DoubleFormat doubleFormat, Object variable) {
        return "";
    }

    @Override
    public String onLong(LongFormat longFormat, Object variable) {
        return "";
    }

    @Override
    public String onFile(FileFormat fileFormat, Object variable) {
        if (!WebResources.isAjaxFileInputEnabled()) {
            return "";
        }
        if (!onFileCalled) {
            onFileCalled = true;
            return "$('.dropzone').each(function () { initFileInput($(this)) });";
        }
        return "";
    }

    @Override
    public String onHidden(HiddenFormat hiddenFormat, Object variable) {
        return "";
    }

    @Override
    public String onList(ListFormat listFormat, Object variable) {
        VariableFormat componentFormat = FormatCommons.createComponent(listFormat, 0);
        return componentFormat.processBy(this, variable);
    }

    @Override
    public String onMap(MapFormat mapFormat, Object variable) {
        return "";
    }

    @Override
    public String onProcessId(ProcessIdFormat processIdFormat, Object variable) {
        return "";
    }

    @Override
    public String onString(StringFormat stringFormat, Object variable) {
        return "";
    }

    @Override
    public String onTextString(TextFormat textFormat, Object variable) {
        return "";
    }

    @Override
    public String onUserType(UserTypeFormat userTypeFormat, Object variable) {
        String componentJsHandlers = "";
        for (VariableDefinition variableDefinition : userTypeFormat.getUserType().getAttributes()) {
            VariableFormat nestedFormat = FormatCommons.create(variableDefinition);
            componentJsHandlers += nestedFormat.processBy(this, variable);
        }
        return componentJsHandlers;
    }

    @Override
    public String onOther(VariableFormat variableFormat, Object variable) {
        return "";
    }

}
