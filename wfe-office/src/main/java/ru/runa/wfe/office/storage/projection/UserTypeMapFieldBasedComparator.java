package ru.runa.wfe.office.storage.projection;

import java.math.BigDecimal;
import java.util.Comparator;
import java.util.Date;
import lombok.Value;
import ru.runa.wfe.commons.TypeConversionUtil;
import ru.runa.wfe.var.UserTypeMap;
import ru.runa.wfe.var.dto.WfVariable;
import ru.runa.wfe.var.format.BigDecimalFormat;
import ru.runa.wfe.var.format.BooleanFormat;
import ru.runa.wfe.var.format.DateFormat;
import ru.runa.wfe.var.format.DateTimeFormat;
import ru.runa.wfe.var.format.DoubleFormat;
import ru.runa.wfe.var.format.ExecutorFormat;
import ru.runa.wfe.var.format.FileFormat;
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
 * @author Alekseev Mikhail
 * @since #1394
 */
public class UserTypeMapFieldBasedComparator implements Comparator<UserTypeMap> {
    private final String fieldName;
    private final Sort sort;

    public UserTypeMapFieldBasedComparator(String fieldName, Sort sort) {
        this.fieldName = fieldName;
        this.sort = sort;
    }

    @Override
    public int compare(UserTypeMap o1, UserTypeMap o2) {
        final WfVariable o1AttributeValue = o1.getAttributeValue(fieldName);
        final WfVariable o2AttributeValue = o2.getAttributeValue(fieldName);

        final VariableFormat o1Format = o1AttributeValue.getDefinition().getFormatNotNull();
        final VariableFormat o2Format = o2AttributeValue.getDefinition().getFormatNotNull();
        if (!o1Format.getJavaClass().equals(o2Format.getJavaClass())) {
            throw new IllegalStateException(String.format(
                    "Incompatible formats of field %s o1Format: %s o2Format: %s",
                    fieldName,
                    o1Format.toString(),
                    o2Format.toString())
            );
        }

        if (o1AttributeValue.getValue() == null && o2AttributeValue.getValue() == null) {
            return 0;
        }

        if (o1AttributeValue.getValue() == null) {
            return ascOrDesc(-1);
        }

        if (o2AttributeValue.getValue() == null) {
            return ascOrDesc(1);
        }

        final CompareResult compareResult = o1Format.processBy(
                new ComparingVisitor(),
                new CompareContext(o1AttributeValue.getValue(), o2AttributeValue.getValue())
        );
        return ascOrDesc(compareResult.getResult());
    }

    private int ascOrDesc(int result) {
        return sort == Sort.ASC ? result : -result;
    }

    @Value
    private static class CompareResult {
        int result;
    }

    @Value
    private static class CompareContext {
        Object o1;
        Object o2;
    }

    private static class ComparingVisitor implements VariableFormatVisitor<CompareResult, CompareContext> {
        @Override
        public CompareResult onDate(DateFormat dateFormat, CompareContext compareContext) {
            final Date o1 = TypeConversionUtil.convertTo(dateFormat.getJavaClass(), compareContext.getO1());
            final Date o2 = TypeConversionUtil.convertTo(dateFormat.getJavaClass(), compareContext.getO2());
            return new CompareResult(o1.compareTo(o2));
        }

        @Override
        public CompareResult onTime(TimeFormat timeFormat, CompareContext compareContext) {
            final Date o1 = TypeConversionUtil.convertTo(timeFormat.getJavaClass(), compareContext.getO1());
            final Date o2 = TypeConversionUtil.convertTo(timeFormat.getJavaClass(), compareContext.getO2());
            return new CompareResult(o1.compareTo(o2));
        }

        @Override
        public CompareResult onDateTime(DateTimeFormat dateTimeFormat, CompareContext compareContext) {
            final Date o1 = TypeConversionUtil.convertTo(dateTimeFormat.getJavaClass(), compareContext.getO1());
            final Date o2 = TypeConversionUtil.convertTo(dateTimeFormat.getJavaClass(), compareContext.getO2());
            return new CompareResult(o1.compareTo(o2));
        }

        @Override
        public CompareResult onExecutor(ExecutorFormat executorFormat, CompareContext compareContext) {
            final String o1 = executorFormat.format(compareContext.getO1());
            final String o2 = executorFormat.format(compareContext.getO2());
            return new CompareResult(o1.compareTo(o2));
        }

        @Override
        public CompareResult onBoolean(BooleanFormat booleanFormat, CompareContext compareContext) {
            final Boolean o1 = TypeConversionUtil.convertTo(booleanFormat.getJavaClass(), compareContext.getO1());
            final Boolean o2 = TypeConversionUtil.convertTo(booleanFormat.getJavaClass(), compareContext.getO2());
            return new CompareResult(o1.compareTo(o2));
        }

        @Override
        public CompareResult onBigDecimal(BigDecimalFormat bigDecimalFormat, CompareContext compareContext) {
            final BigDecimal o1 = TypeConversionUtil.convertTo(bigDecimalFormat.getJavaClass(), compareContext.getO1());
            final BigDecimal o2 = TypeConversionUtil.convertTo(bigDecimalFormat.getJavaClass(), compareContext.getO2());
            return new CompareResult(o1.compareTo(o2));
        }

        @Override
        public CompareResult onDouble(DoubleFormat doubleFormat, CompareContext compareContext) {
            final Double o1 = TypeConversionUtil.convertTo(doubleFormat.getJavaClass(), compareContext.getO1());
            final Double o2 = TypeConversionUtil.convertTo(doubleFormat.getJavaClass(), compareContext.getO2());
            return new CompareResult(o1.compareTo(o2));
        }

        @Override
        public CompareResult onLong(LongFormat longFormat, CompareContext compareContext) {
            final Number o1 = TypeConversionUtil.convertTo(longFormat.getJavaClass(), compareContext.getO1());
            final Number o2 = TypeConversionUtil.convertTo(longFormat.getJavaClass(), compareContext.getO2());
            return new CompareResult(Long.compare(o1.longValue(), o2.longValue()));
        }

        @Override
        public CompareResult onFile(FileFormat fileFormat, CompareContext compareContext) {
            final String o1 = fileFormat.convertToStringValue(compareContext.getO1());
            final String o2 = fileFormat.convertToStringValue(compareContext.getO2());
            return new CompareResult(o1.compareTo(o2));
        }

        @Override
        public CompareResult onHidden(HiddenFormat hiddenFormat, CompareContext compareContext) {
            return new CompareResult(0);
        }

        @Override
        public CompareResult onList(ListFormat listFormat, CompareContext compareContext) {
            throw new UnsupportedOperationException();
        }

        @Override
        public CompareResult onMap(MapFormat mapFormat, CompareContext compareContext) {
            throw new UnsupportedOperationException();
        }

        @Override
        public CompareResult onProcessId(ProcessIdFormat processIdFormat, CompareContext compareContext) {
            final Number o1 = TypeConversionUtil.convertTo(processIdFormat.getJavaClass(), compareContext.getO1());
            final Number o2 = TypeConversionUtil.convertTo(processIdFormat.getJavaClass(), compareContext.getO2());
            return new CompareResult(Long.compare(o1.longValue(), o2.longValue()));
        }

        @Override
        public CompareResult onString(StringFormat stringFormat, CompareContext compareContext) {
            final String o1 = stringFormat.format(compareContext.getO1());
            final String o2 = stringFormat.format(compareContext.getO2());
            return new CompareResult(o1.compareTo(o2));
        }

        @Override
        public CompareResult onTextString(TextFormat textFormat, CompareContext compareContext) {
            final String o1 = textFormat.format(compareContext.getO1());
            final String o2 = textFormat.format(compareContext.getO2());
            return new CompareResult(o1.compareTo(o2));
        }

        @Override
        public CompareResult onFormattedTextString(FormattedTextFormat textFormat, CompareContext compareContext) {
            final String o1 = textFormat.format(compareContext.getO1());
            final String o2 = textFormat.format(compareContext.getO2());
            return new CompareResult(o1.compareTo(o2));
        }

        @Override
        public CompareResult onUserType(UserTypeFormat userTypeFormat, CompareContext compareContext) {
            throw new UnsupportedOperationException();
        }

        @Override
        public CompareResult onOther(VariableFormat variableFormat, CompareContext compareContext) {
            throw new UnsupportedOperationException();
        }
    }
}
