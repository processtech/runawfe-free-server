package ru.runa.wfe.script.batch;

import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlEnumValue;
import javax.xml.bind.annotation.XmlType;
import lombok.RequiredArgsConstructor;
import ru.runa.wfe.presentation.BatchPresentation;
import ru.runa.wfe.presentation.BatchPresentationConsts;
import ru.runa.wfe.presentation.BatchPresentationFactory;
import ru.runa.wfe.script.AdminScriptConstants;
import ru.runa.wfe.script.common.ScriptExecutionContext;
import ru.runa.wfe.script.common.ScriptOperation;
import ru.runa.wfe.script.common.ScriptValidationException;
import ru.runa.wfe.user.Profile;

@XmlType(name = "BatchPresentationReferenceType", namespace = AdminScriptConstants.NAMESPACE)
public class XmlBatchPresentation {

    @XmlAttribute(name = "name", required = true)
    public BatchPresentationReplicationType batchReplicationType;

    @XmlAttribute(name = "actorName")
    public String actorName;

    @XmlAttribute(name = "batchName")
    public String batchName;

    @XmlAttribute(name = "batchId", required = true)
    public BatchPresentationIdEnum batchId = BatchPresentationIdEnum.EXECUTORS;

    public BatchPresentation getBatchPresentation(ScriptOperation scriptOperation, ScriptExecutionContext context) {
        if (Strings.isNullOrEmpty(actorName) || Strings.isNullOrEmpty(batchName)) {
            switch (batchId) {
                case DEFINITIONS:
                    return BatchPresentationFactory.DEFINITIONS.createDefault();
                case EXECUTORS:
                    return BatchPresentationFactory.EXECUTORS.createDefault();
                case ARCHIVED_PROCESSES:
                    return BatchPresentationFactory.ARCHIVED_PROCESSES.createDefault();
                case CURRENT_PROCESSES:
                    return BatchPresentationFactory.CURRENT_PROCESSES.createDefault();
                case CURRENT_PROCESSES_WITH_TASKS:
                    return BatchPresentationFactory.CURRENT_PROCESSES_WITH_TASKS.createDefault();
                case RELATIONS:
                    return BatchPresentationFactory.RELATIONS.createDefault();
                case RELATION_PAIRS:
                    return BatchPresentationFactory.RELATION_PAIRS.createDefault();
                case REPORTS:
                    return BatchPresentationFactory.REPORTS.createDefault();
                case TASKS:
                    return BatchPresentationFactory.TASKS.createDefault();
                default:
                    return BatchPresentationFactory.EXECUTORS.createDefault();
            }
        }
        Long actorId = context.getExecutorLogic().getExecutor(context.getUser(), actorName).getId();
        Profile profile = context.getProfileLogic().getProfiles(context.getUser(), Lists.newArrayList(actorId)).get(0);

        for (BatchPresentation batch : profile.getBatchPresentations(batchId.presentationId)) {
            if (batch.getName().equals(batchName)) {
                return batch;
            }
        }
        throw new ScriptValidationException(scriptOperation, "batch presentation with name " + batchName + " or actor " + actorName + " is not found");
    }

    @RequiredArgsConstructor
    @XmlEnum
    enum BatchPresentationIdEnum {
        @XmlEnumValue(value = BatchPresentationConsts.ID_ALL_EXECUTORS)
        EXECUTORS(BatchPresentationConsts.ID_ALL_EXECUTORS),

        @XmlEnumValue(value = BatchPresentationConsts.ID_RELATIONS)
        RELATIONS(BatchPresentationConsts.ID_RELATIONS),

        @XmlEnumValue(value = BatchPresentationConsts.ID_RELATION_PAIRS)
        RELATION_PAIRS(BatchPresentationConsts.ID_RELATION_PAIRS),

        @XmlEnumValue(value = BatchPresentationConsts.ID_DEFINITIONS)
        DEFINITIONS(BatchPresentationConsts.ID_DEFINITIONS),

        @XmlEnumValue(value = BatchPresentationConsts.ID_ARCHIVED_PROCESSES)
        ARCHIVED_PROCESSES(BatchPresentationConsts.ID_ARCHIVED_PROCESSES),

        @XmlEnumValue(value = BatchPresentationConsts.ID_CURRENT_PROCESSES)
        CURRENT_PROCESSES(BatchPresentationConsts.ID_CURRENT_PROCESSES),

        @XmlEnumValue(value = BatchPresentationConsts.ID_CURRENT_PROCESSES_WITH_TASKS)
        CURRENT_PROCESSES_WITH_TASKS(BatchPresentationConsts.ID_CURRENT_PROCESSES_WITH_TASKS),

        @XmlEnumValue(value = BatchPresentationConsts.ID_TASKS)
        TASKS(BatchPresentationConsts.ID_TASKS),

        @XmlEnumValue(value = BatchPresentationConsts.ID_REPORTS)
        REPORTS(BatchPresentationConsts.ID_REPORTS);

        public final String presentationId;
    }

    @XmlEnum
    enum BatchPresentationReplicationType {
        @XmlEnumValue(value = "source")
        SOURCE,

        @XmlEnumValue(value = "template")
        TEMPLATE
    }
}
