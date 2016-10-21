package ru.runa.wfe.report;

/**
 * Report configuration type.
 * 
 * Currently only one report type is supported: Report is built via SQL requests.
 */
public enum ReportConfigurationType {
    RAW_SQL_REPORT("JasperReport with SQL inside the report", ".jasper") {

        @Override
        public <Result> Result processBy(ReportConfigurationTypeVisitor<Result> visitor) {
            return visitor.onRawSqlReport();
        }
    };

    /**
     * Report type description.
     */
    private final String description;

    /**
     * Report file extension.
     */
    private final String fileExtension;

    private ReportConfigurationType(String description, String fileExtension) {
        this.description = description;
        this.fileExtension = fileExtension;
    }

    /**
     * Returns text description of the report construction details.
     * 
     * @return text description of the report construction details.
     */
    public final String getDescription() {
        return description;
    }

    /**
     * Returns filter for report file (Can be used to filter files that are shown in select).
     * 
     * @return filter for report file.
     */
    public String getFileExtension() {
        return fileExtension;
    }

    /**
     * Applies operation on the basis of report configuration type.
     * 
     * @param visitor
     *            Applied operation.
     * @return Result of operation being applied.
     * 
     * @throws Exception
     *             any exception can be thrown
     */
    public abstract <Result> Result processBy(ReportConfigurationTypeVisitor<Result> visitor);

    /**
     * Opertion interface. Operation is applied on the basis of report configuration type.
     * 
     * @param <Result>
     *            the type of result that is return by the operation.
     */
    public static interface ReportConfigurationTypeVisitor<Result> {
        /**
         * Used to process report that is constructed with {@link ReportParametersBuilder}.
         * 
         * @return result of operation being applied.
         * 
         * @throws Exception
         *             Any exception can be thrown
         */
        public Result onParameterBuilder();

        /**
         * Used to process report that is constructed with the use of SQL requests. No additional work to build report is required.
         * 
         * @return result of operation being applied.
         * 
         * @throws Exception
         *             Any exception can be thrown
         */
        public Result onRawSqlReport();
    }
}
