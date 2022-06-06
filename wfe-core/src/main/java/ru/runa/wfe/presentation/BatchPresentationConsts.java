package ru.runa.wfe.presentation;

/**
 * Common constants for {@link BatchPresentation}.
 * 
 * @author Konstantinov Aleksey 11.02.2012
 */
public class BatchPresentationConsts {
    /**
     * Allowed sizes for paged {@link BatchPresentation}. This sizes will be available in web interface.
     */
    private static final int[] ALLOWED_VIEW_SIZES = { 20, 50, 100, 500 };

    /**
     * Identity of default presentation group.
     */
    public static final String DEFAULT_ID = "batch_presentation_default_id";

    /**
     * Struts property to display for as default presentation name.
     */
    public static final String DEFAULT_NAME = "label.batch_presentation_default_name";

    /**
     * Sort mode: ascending.
     */
    public static final boolean ASC = true;

    /**
     * Sort mode: descending.
     */
    public static final boolean DESC = false;

    /**
     * Sort mode: descending.
     */
    public static final int RANGE_SIZE_UNLIMITED = -1;

    public static final String ID_ALL_EXECUTORS = "listAllExecutorsForm";

    public static final String ID_EXECUTORS_GROUPS = "listExecutorGroupsForm";

    public static final String ID_GROUP_MEMBERS = "listGroupMembersForm";

    public static final String ID_NOT_EXECUTOR_IN_GROUPS = "listNotExecutorGroupsForm";

    public static final String ID_NOT_GROUP_MEMBERS = "listNotGroupMembersForm";

    public static final String ID_GRANT_PERMISSIONS = "grantPermissionsForm";

    public static final String ID_RELATIONS = "listRelations";

    public static final String ID_RELATION_PAIRS = "listRelationPairs";

    public static final String ID_REPORTS = "listReportsForm";

    public static final String ID_ARCHIVED_PROCESSES = "listArchivedProcessesForm";

    public static final String ID_CURRENT_PROCESSES = "listCurrentProcessesForm";

    public static final String ID_CURRENT_PROCESSES_WITH_TASKS = "listCurrentProcessesWithTasksForm";

    public static final String ID_DEFINITIONS = "listProcessesDefinitionsForm";

    public static final String ID_DEFINITIONS_HISTORY = "listProcessesDefinitionsHistoryForm";

    public static final String ID_TASKS = "listTasksForm";

    public static final String ID_OBSERVABLE_TASKS = "listObservableTasksForm";

    public static final String ID_SYSTEM_LOGS = "listSystemLogsForm";

    public static final String ID_TOKEN_ERRORS = "listTokenErrorsForm";

    public static final String ID_CHAT_ROOMS = "listChatRoomsForm";

    /**
     * Allowed sizes for paged {@link BatchPresentation}. This sizes will be available in web interface.
     * 
     * @return Allowed sizes for paged {@link BatchPresentation}.
     */
    public static int[] getAllowedViewSizes() {
        return ALLOWED_VIEW_SIZES.clone();
    }
}
