package ru.runa.wfe.presentation;

import com.google.common.collect.Maps;
import java.util.Map;

public class DefaultBatchPresentations {
    private static Map<String, BatchPresentation> map = Maps.newHashMap();
    static {
        create(BatchPresentationConsts.ID_ALL_EXECUTORS, BatchPresentationFactory.EXECUTORS);
        create(BatchPresentationConsts.ID_EXECUTORS_GROUPS, BatchPresentationFactory.GROUPS);
        create(BatchPresentationConsts.ID_GROUP_MEMBERS, BatchPresentationFactory.EXECUTORS);
        create(BatchPresentationConsts.ID_NOT_EXECUTOR_IN_GROUPS, BatchPresentationFactory.GROUPS);
        create(BatchPresentationConsts.ID_NOT_GROUP_MEMBERS, BatchPresentationFactory.EXECUTORS);
        create(BatchPresentationConsts.ID_GRANT_PERMISSIONS, BatchPresentationFactory.EXECUTORS);
        create(BatchPresentationConsts.ID_RELATIONS, BatchPresentationFactory.RELATIONS);
        create(BatchPresentationConsts.ID_RELATION_PAIRS, BatchPresentationFactory.RELATION_PAIRS);
        create(BatchPresentationConsts.ID_REPORTS, BatchPresentationFactory.REPORTS);
        create(BatchPresentationConsts.ID_ARCHIVED_PROCESSES, BatchPresentationFactory.ARCHIVED_PROCESSES);
        create(BatchPresentationConsts.ID_CURRENT_PROCESSES, BatchPresentationFactory.CURRENT_PROCESSES);
        create(BatchPresentationConsts.ID_CURRENT_PROCESSES_WITH_TASKS, BatchPresentationFactory.CURRENT_PROCESSES_WITH_TASKS);
        create(BatchPresentationConsts.ID_DEFINITIONS, BatchPresentationFactory.DEFINITIONS);
        create(BatchPresentationConsts.ID_DEFINITIONS_HISTORY, BatchPresentationFactory.DEFINITIONS_HISTORY);
        create(BatchPresentationConsts.ID_TASKS, BatchPresentationFactory.TASKS);
        create(BatchPresentationConsts.ID_OBSERVABLE_TASKS, BatchPresentationFactory.OBSERVABLE_TASKS);
        create(BatchPresentationConsts.ID_SYSTEM_LOGS, BatchPresentationFactory.SYSTEM_LOGS);
        create(BatchPresentationConsts.ID_TOKEN_ERRORS, BatchPresentationFactory.TOKEN_ERRORS);
        create(BatchPresentationConsts.ID_CHAT_ROOMS, BatchPresentationFactory.CHAT_ROOMS);
    }

    private static void create(String category, BatchPresentationFactory factory) {
        map.put(category, factory.createDefault(category));
    }

    public static BatchPresentation get(String category, boolean clone) {
        BatchPresentation presentation = map.get(category);
        if (clone) {
            presentation = presentation.clone();
        }
        return presentation;
    }

}
