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

    public static final String ID_EXECUTORS_WITHOUT_PERMISSIONS_ON_EXECUTOR = "listExecutorsWithoutPermissionsOnExecutorForm";

    public static final String ID_GROUP_MEMBERS = "listGroupMembersForm";

    public static final String ID_NOT_EXECUTOR_IN_GROUPS = "listNotExecutorGroupsForm";

    public static final String ID_NOT_GROUP_MEMBERS = "listNotGroupMembersForm";

    public static final String ID_EXECUTORS_WITHOUT_PERMISSIONS_ON_SYSTEM = "listExecutorsWithoutPermissionsOnSystemForm";

    public static final String ID_EXECUTORS_WITHOUT_PERMISSIONS_ON_DEFINITION = "listExecutorsWithoutPermissionsOnDefinitionForm";

    public static final String ID_EXECUTORS_WITHOUT_PERMISSIONS_ON_PROCESS = "listExecutorsWithoutPermissionsOnProcessForm";

    public static final String ID_EXECUTORS_WITHOUT_PERMISSIONS_ON_RELATION = "listExecutorsWithoutPermissionsOnRelationForm";

    public static final String ID_EXECUTORS_WITHOUT_BOT_STATION_PERMISSION = "listExecutorsWithoutBotStationPermission";

    public static final String ID_EXECUTORS_WITHOUT_REPORTS_PERMISSION = "listExecutorsWithoutPermissionsOnReportsForm";

    public static final String ID_RELATIONS = "listRelations";

    public static final String ID_RELATION_PAIRS = "listRelationPairs";

    public static final String ID_REPORTS = "listReportsForm";

    public static final String ID_PROCESSES = "listProcessesForm";

    public static final String ID_PROCESSES_WITH_TASKS = "listProcessesWithTasksForm";

    public static final String ID_DEFINITIONS = "listProcessesDefinitionsForm";

    public static final String ID_DEFINITIONS_HISTORY = "listProcessesDefinitionsHistoryForm";

    public static final String ID_TASKS = "listTasksForm";

    public static final String ID_SYSTEM_LOGS = "listSystemLogsForm";

    /**
     * Allowed sizes for paged {@link BatchPresentation}. This sizes will be available in web interface.
     * 
     * @return Allowed sizes for paged {@link BatchPresentation}.
     */
    public static int[] getAllowedViewSizes() {
        return ALLOWED_VIEW_SIZES.clone();
    }

}
