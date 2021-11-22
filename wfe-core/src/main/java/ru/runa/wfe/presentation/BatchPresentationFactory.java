package ru.runa.wfe.presentation;

/**
 * Default batch presentation factory.
 * 
 * @author Dofs
 * @since 4.0
 */
public class BatchPresentationFactory {
    public static final BatchPresentationFactory EXECUTORS = new BatchPresentationFactory(ClassPresentationType.EXECUTOR, 100);
    public static final BatchPresentationFactory ACTORS = new BatchPresentationFactory(ClassPresentationType.ACTOR, 100);
    public static final BatchPresentationFactory GROUPS = new BatchPresentationFactory(ClassPresentationType.GROUP, 100);
    public static final BatchPresentationFactory RELATIONS = new BatchPresentationFactory(ClassPresentationType.RELATION);
    public static final BatchPresentationFactory RELATION_PAIRS = new BatchPresentationFactory(ClassPresentationType.RELATIONPAIR);
    public static final BatchPresentationFactory SYSTEM_LOGS = new BatchPresentationFactory(ClassPresentationType.SYSTEM_LOG);
    public static final BatchPresentationFactory ARCHIVED_PROCESSES = new BatchPresentationFactory(ClassPresentationType.ARCHIVED_PROCESS);
    public static final BatchPresentationFactory CURRENT_PROCESSES = new BatchPresentationFactory(ClassPresentationType.CURRENT_PROCESS);
    public static final BatchPresentationFactory CURRENT_PROCESSES_WITH_TASKS = new BatchPresentationFactory(ClassPresentationType.CURRENT_PROCESS_WITH_TASKS);
    public static final BatchPresentationFactory DEFINITIONS = new BatchPresentationFactory(ClassPresentationType.DEFINITION, 100);
    public static final BatchPresentationFactory DEFINITIONS_HISTORY = new BatchPresentationFactory(ClassPresentationType.DEFINITION_HISTORY);
    public static final BatchPresentationFactory TASKS = new BatchPresentationFactory(ClassPresentationType.TASK);
    public static final BatchPresentationFactory OBSERVABLE_TASKS = new BatchPresentationFactory(ClassPresentationType.TASK_OBSERVABLE);
    public static final BatchPresentationFactory REPORTS = new BatchPresentationFactory(ClassPresentationType.REPORTS);
    public static final BatchPresentationFactory TOKEN_ERRORS = new BatchPresentationFactory(ClassPresentationType.TOKEN_ERRORS);

    private final ClassPresentationType type;
    private final int defaultPageRangeSize;

    public BatchPresentationFactory(ClassPresentationType type) {
        this(type, BatchPresentationConsts.getAllowedViewSizes()[0]);
    }

    public BatchPresentationFactory(ClassPresentationType type, int defaultPageRangeSize) {
        this.type = type;
        this.defaultPageRangeSize = defaultPageRangeSize;
    }

    public BatchPresentation createDefault() {
        return createDefault(BatchPresentationConsts.DEFAULT_ID);
    }

    public BatchPresentation createDefault(String batchPresentationId) {
        BatchPresentation result = new BatchPresentation(type, BatchPresentationConsts.DEFAULT_NAME, batchPresentationId);
        result.setRangeSize(defaultPageRangeSize);
        return result;
    }

    public BatchPresentation createNonPaged() {
        BatchPresentation batchPresentation = createDefault(BatchPresentationConsts.DEFAULT_ID);
        batchPresentation.setRangeSize(BatchPresentationConsts.RANGE_SIZE_UNLIMITED);
        return batchPresentation;
    }
}
