package ru.runa.wfe.report;

/**
 * Тип конфигурации отчета.
 * 
 * На данный момент поддерживается только один тип отчета: Отчет строится через SQL запросы.
 */
public enum ReportConfigurationType {
    RAW_SQL_REPORT("JasperReport с SQL внутри отчёта", ".jasper") {

        @Override
        public <Result> Result processBy(ReportConfigurationTypeVisitor<Result> visitor) {
            return visitor.onRawSqlReport();
        }
    };

    /**
     * Описание типа отчета.
     */
    private final String description;

    /**
     * Расширение файла с отчётом.
     */
    private final String fileExtension;

    private ReportConfigurationType(String description, String fileExtension) {
        this.description = description;
        this.fileExtension = fileExtension;
    }

    /**
     * Возвращает текстовое описание способа построения отчёта.
     * 
     * @return Возвращает текстовое описание способа построения отчёта.
     */
    public final String getDescription() {
        return description;
    }

    /**
     * Возвращает фильтр для файла с отчётом (Может использоваться для ограничения множества файлов, доступных для выбора).
     * 
     * @return Возвращает фильтр для файла с отчётом.
     */
    public String getFileExtension() {
        return fileExtension;
    }

    /**
     * Применяет операцию в зависимости от типа конфигурирования отчета.
     * 
     * @param visitor
     *            Применяемая операция.
     * @return Возвращает результат применения операции.
     * 
     * @throws Exception
     *             Может выбрасывать любые исключения
     */
    public abstract <Result> Result processBy(ReportConfigurationTypeVisitor<Result> visitor);

    /**
     * Интерфейс операции, которая может быть применена в зависимости от типа конфигурирования отчета.
     * 
     * @param <Result>
     *            Тип результата, возвращаемого операцией.
     */
    public static interface ReportConfigurationTypeVisitor<Result> {
        /**
         * Вызывается для обработки отчета, который строится с использованием {@link ReportParametersBuilder}.
         * 
         * @return Возвращает результат применения операции.
         * 
         * @throws Exception
         *             Может выбрасывать любые исключения
         */
        public Result onParameterBuilder();

        /**
         * Вызывается для обработки отчета, который строится с использованием SQL запросов. Дополнительных действия для построения отчёта не
         * требуется.
         * 
         * @return Возвращает результат применения операции.
         * 
         * @throws Exception
         *             Может выбрасывать любые исключения
         */
        public Result onRawSqlReport();
    }
}
