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
    public static final BatchPresentationFactory PROCESSES = new BatchPresentationFactory(ClassPresentationType.PROCESS);
    public static final BatchPresentationFactory EXTENDED_PROCESSES = new BatchPresentationFactory(ClassPresentationType.EXTENDED_PROCESS);
    public static final BatchPresentationFactory DEFINITIONS = new BatchPresentationFactory(ClassPresentationType.DEFINITION, 100);
    public static final BatchPresentationFactory DEFINITIONS_HISTORY = new BatchPresentationFactory(ClassPresentationType.DEFINITION_HISTORY);
    public static final BatchPresentationFactory TASKS = new BatchPresentationFactory(ClassPresentationType.TASK);
    public static final BatchPresentationFactory REPORTS = new BatchPresentationFactory(ClassPresentationType.REPORTS);

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

    // TODO this method for loading all must be changed? It actually not loaded all - it loaded much, but not all.
    public BatchPresentation createNonPaged() {
        BatchPresentation batchPresentation = createDefault(BatchPresentationConsts.DEFAULT_ID);
        batchPresentation.setRangeSize(10000);
        return batchPresentation;
    }

}
