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

import java.util.Map;

import com.google.common.collect.Maps;

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
