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

import ru.runa.wfe.InternalApplicationException;

import com.google.common.collect.Maps;

public class BatchPresentations {
    private static Map<String, BatchPresentationFactory> map = Maps.newHashMap();
    static {
        map.put(BatchPresentationConsts.ID_ALL_EXECUTORS, BatchPresentationFactory.EXECUTORS);
        map.put(BatchPresentationConsts.ID_EXECUTORS_GROUPS, BatchPresentationFactory.GROUPS);
        map.put(BatchPresentationConsts.ID_EXECUTORS_WITHOUT_PERMISSIONS_ON_EXECUTOR, BatchPresentationFactory.EXECUTORS);
        map.put(BatchPresentationConsts.ID_GROUP_MEMBERS, BatchPresentationFactory.EXECUTORS);
        map.put(BatchPresentationConsts.ID_NOT_EXECUTOR_IN_GROUPS, BatchPresentationFactory.GROUPS);
        map.put(BatchPresentationConsts.ID_NOT_GROUP_MEMBERS, BatchPresentationFactory.EXECUTORS);
        map.put(BatchPresentationConsts.ID_EXECUTORS_WITHOUT_PERMISSIONS_ON_SYSTEM, BatchPresentationFactory.EXECUTORS);
        map.put(BatchPresentationConsts.ID_EXECUTORS_WITHOUT_PERMISSIONS_ON_DEFINITION, BatchPresentationFactory.EXECUTORS);
        map.put(BatchPresentationConsts.ID_EXECUTORS_WITHOUT_PERMISSIONS_ON_PROCESS, BatchPresentationFactory.EXECUTORS);
        map.put(BatchPresentationConsts.ID_EXECUTORS_WITHOUT_PERMISSIONS_ON_RELATION, BatchPresentationFactory.EXECUTORS);
        map.put(BatchPresentationConsts.ID_EXECUTORS_WITHOUT_BOT_STATION_PERMISSION, BatchPresentationFactory.EXECUTORS);
        map.put(BatchPresentationConsts.ID_EXECUTORS_WITHOUT_REPORTS_PERMISSION, BatchPresentationFactory.EXECUTORS);
        map.put(BatchPresentationConsts.ID_RELATIONS, BatchPresentationFactory.RELATIONS);
        map.put(BatchPresentationConsts.ID_RELATION_PAIRS, BatchPresentationFactory.RELATION_PAIRS);
        map.put(BatchPresentationConsts.REPORTS, BatchPresentationFactory.REPORTS);
        map.put(BatchPresentationConsts.ID_PROCESSES, BatchPresentationFactory.PROCESSES);
        map.put(BatchPresentationConsts.ID_PROCESSES_WITH_TASKS, BatchPresentationFactory.PROCESSES_WITH_TASKS);
        map.put(BatchPresentationConsts.ID_DEFINITIONS, BatchPresentationFactory.DEFINITIONS);
        map.put(BatchPresentationConsts.ID_DEFINITION_CHANGES, BatchPresentationFactory.DEFINITION_CHANGES);
        map.put(BatchPresentationConsts.ID_DEFINITIONS_HISTORY, BatchPresentationFactory.DEFINITIONS_HISTORY);
        map.put(BatchPresentationConsts.ID_TASKS, BatchPresentationFactory.TASKS);
        map.put(BatchPresentationConsts.ID_SYSTEM_LOGS, BatchPresentationFactory.SYSTEM_LOGS);
    }

    public static BatchPresentation createDefault(String batchPresentationId) {
        BatchPresentationFactory factory = map.get(batchPresentationId);
        if (factory == null) {
            throw new InternalApplicationException("No factory configured for id '" + batchPresentationId + "'");
        }
        return factory.createDefault(batchPresentationId);
    }
}
