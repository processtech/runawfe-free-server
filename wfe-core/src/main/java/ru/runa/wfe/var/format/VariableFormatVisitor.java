package ru.runa.wfe.var.format;

/**
 * Interface for operations, which may be applied to {@link VariableFormat} formats.
 *
 * @param <TResult>
 *            Type of operation result.
 * @param <TResult>
 *            Type of operation context (parameter, passed to methods).
 */
public interface VariableFormatVisitor<TResult, TContext> {

    /**
     * Called to apply operation to date format.
     *
     * @param dateFormat
     *            Format, operation applied to.
     * @param context
     *            Operation call context. Contains additional data, passed to operation.
     * @return Result of operation.
     */
    TResult onDate(DateFormat dateFormat, TContext context);

    /**
     * Called to apply operation to time format.
     *
     * @param timeFormat
     *            Format, operation applied to.
     * @param context
     *            Operation call context. Contains additional data, passed to operation.
     * @return Result of operation.
     */
    TResult onTime(TimeFormat timeFormat, TContext context);

    /**
     * Called to apply operation to date and time format.
     *
     * @param dateTimeFormat
     *            Format, operation applied to.
     * @param context
     *            Operation call context. Contains additional data, passed to operation.
     * @return Result of operation.
     */
    TResult onDateTime(DateTimeFormat dateTimeFormat, TContext context);

    /**
     * Called to apply operation to executor format.
     *
     * @param executorFormat
     *            Format, operation applied to.
     * @param context
     *            Operation call context. Contains additional data, passed to operation.
     * @return Result of operation.
     */
    TResult OnExecutor(ExecutorFormat executorFormat, TContext context);

    /**
     * Called to apply operation to boolean format.
     *
     * @param booleanFormat
     *            Format, operation applied to.
     * @param context
     *            Operation call context. Contains additional data, passed to operation.
     * @return Result of operation.
     */
    TResult onBoolean(BooleanFormat booleanFormat, TContext context);

    /**
     * Called to apply operation to big decimal format.
     *
     * @param bigDecimalFormat
     *            Format, operation applied to.
     * @param context
     *            Operation call context. Contains additional data, passed to operation.
     * @return Result of operation.
     */
    TResult onBigDecimal(BigDecimalFormat bigDecimalFormat, TContext context);

    /**
     * Called to apply operation to double format.
     *
     * @param doubleFormat
     *            Format, operation applied to.
     * @param context
     *            Operation call context. Contains additional data, passed to operation.
     * @return Result of operation.
     */
    TResult onDouble(DoubleFormat doubleFormat, TContext context);

    /**
     * Called to apply operation to long format.
     *
     * @param longFormat
     *            Format, operation applied to.
     * @param context
     *            Operation call context. Contains additional data, passed to operation.
     * @return Result of operation.
     */
    TResult onLong(LongFormat longFormat, TContext context);

    /**
     * Called to apply operation to file format.
     *
     * @param fileFormat
     *            Format, operation applied to.
     * @param context
     *            Operation call context. Contains additional data, passed to operation.
     * @return Result of operation.
     */
    TResult onFile(FileFormat fileFormat, TContext context);

    /**
     * Called to apply operation to hidden format.
     *
     * @param hiddenFormat
     *            Format, operation applied to.
     * @param context
     *            Operation call context. Contains additional data, passed to operation.
     * @return Result of operation.
     */
    TResult onHidden(HiddenFormat hiddenFormat, TContext context);

    /**
     * Called to apply operation to list format.
     *
     * @param listFormat
     *            Format, operation applied to.
     * @param context
     *            Operation call context. Contains additional data, passed to operation.
     * @return Result of operation.
     */
    TResult onList(ListFormat listFormat, TContext context);

    /**
     * Called to apply operation to map format.
     *
     * @param mapFormat
     *            Format, operation applied to.
     * @param context
     *            Operation call context. Contains additional data, passed to operation.
     * @return Result of operation.
     */
    TResult onMap(MapFormat mapFormat, TContext context);

    /**
     * Called to apply operation to process id format.
     *
     * @param processIdFormat
     *            Format, operation applied to.
     * @param context
     *            Operation call context. Contains additional data, passed to operation.
     * @return Result of operation.
     */
    TResult onProcessId(ProcessIdFormat processIdFormat, TContext context);

    /**
     * Called to apply operation to string format.
     *
     * @param stringFormat
     *            Format, operation applied to.
     * @param context
     *            Operation call context. Contains additional data, passed to operation.
     * @return Result of operation.
     */
    TResult onString(StringFormat stringFormat, TContext context);

    /**
     * Called to apply operation to text format.
     *
     * @param textFormat
     *            Format, operation applied to.
     * @param context
     *            Operation call context. Contains additional data, passed to operation.
     * @return Result of operation.
     */
    TResult onTextString(TextFormat textFormat, TContext context);

    /**
     * Called to apply operation to user type format.
     *
     * @param userTypeFormat
     *            Format, operation applied to.
     * @param context
     *            Operation call context. Contains additional data, passed to operation.
     * @return Result of operation.
     */
    TResult onUserType(UserTypeFormat userTypeFormat, TContext context);

    /**
     * Called to apply operation to custom format.
     *
     * @param userTypeFormat
     *            Format, operation applied to.
     * @param context
     *            Operation call context. Contains additional data, passed to operation.
     * @return Result of operation.
     */
    TResult onOther(VariableFormat variableFormat, TContext context);
}
