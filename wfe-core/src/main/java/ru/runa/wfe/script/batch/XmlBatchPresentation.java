package ru.runa.wfe.script.batch;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlEnumValue;
import javax.xml.bind.annotation.XmlType;

import ru.runa.wfe.presentation.BatchPresentation;
import ru.runa.wfe.presentation.BatchPresentationConsts;
import ru.runa.wfe.presentation.BatchPresentationFactory;
import ru.runa.wfe.script.AdminScriptConstants;
import ru.runa.wfe.script.common.ScriptExecutionContext;
import ru.runa.wfe.script.common.ScriptOperation;
import ru.runa.wfe.script.common.ScriptValidationException;
import ru.runa.wfe.user.Profile;

import com.google.common.base.Strings;

@XmlType(name = "BatchPresentationReferenceType", namespace = AdminScriptConstants.NAMESPACE)
public class XmlBatchPresentation {

    @XmlAttribute(name = AdminScriptConstants.NAME_ATTRIBUTE_NAME)
    public BatchPresentationReplicationType batchReplicationType;

    @XmlAttribute(name = "actorName", required = false)
    public String actorName;

    @XmlAttribute(name = "batchName", required = false)
    public String batchName;

    @XmlAttribute(name = "batchId", required = false)
    public BatchPresentationIdEnum batchId = BatchPresentationIdEnum.EXECUTORS;

    public BatchPresentation getBatchPresentation(ScriptOperation scriptOperation, ScriptExecutionContext context) {
        if (Strings.isNullOrEmpty(actorName) && Strings.isNullOrEmpty(batchName)) {
            switch (batchId) {
            case DEFINITIONS:
                return BatchPresentationFactory.DEFINITIONS.createDefault();
            case EXECUTORS:
                return BatchPresentationFactory.EXECUTORS.createDefault();
            case PROCESSES:
                return BatchPresentationFactory.PROCESSES.createDefault();
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
        Profile profile = context.getProfileLogic().getProfile(context.getUser(), actorId);
        return getBatchFromProfile(scriptOperation, profile, batchId.getPresentationId(), batchName);
    }

    private BatchPresentation getBatchFromProfile(ScriptOperation scriptOperation, Profile profile, String batchID, String batchName) {
        for (BatchPresentation batch : profile.getBatchPresentations(batchID)) {
            if (batch.getName().equals(batchName)) {
                return batch;
            }
        }
        throw new ScriptValidationException(scriptOperation, "batch presentation with name " + batchName + " or actor " + actorName + " is not found");
    }

    @XmlEnum(value = String.class)
    enum BatchPresentationIdEnum {
        @XmlEnumValue(value = BatchPresentationConsts.ID_ALL_EXECUTORS)
        EXECUTORS {
            @Override
            public String getPresentationId() {
                return BatchPresentationConsts.ID_ALL_EXECUTORS;
            }
        },

        @XmlEnumValue(value = BatchPresentationConsts.ID_RELATIONS)
        RELATIONS {
            @Override
            public String getPresentationId() {
                return BatchPresentationConsts.ID_RELATIONS;
            }
        },

        @XmlEnumValue(value = BatchPresentationConsts.ID_RELATION_PAIRS)
        RELATION_PAIRS {
            @Override
            public String getPresentationId() {
                return BatchPresentationConsts.ID_RELATION_PAIRS;
            }
        },

        @XmlEnumValue(value = BatchPresentationConsts.ID_DEFINITIONS)
        DEFINITIONS {
            @Override
            public String getPresentationId() {
                return BatchPresentationConsts.ID_DEFINITIONS;
            }
        },

        @XmlEnumValue(value = BatchPresentationConsts.ID_PROCESSES)
        PROCESSES {
            @Override
            public String getPresentationId() {
                return BatchPresentationConsts.ID_PROCESSES;
            }
        },

        @XmlEnumValue(value = BatchPresentationConsts.ID_TASKS)
        TASKS {
            @Override
            public String getPresentationId() {
                return BatchPresentationConsts.ID_TASKS;
            }
        },

        @XmlEnumValue(value = BatchPresentationConsts.REPORTS)
        REPORTS {
            @Override
            public String getPresentationId() {
                return BatchPresentationConsts.REPORTS;
            }
        };

        public abstract String getPresentationId();
    }

    @XmlEnum(value = String.class)
    enum BatchPresentationReplicationType {
        @XmlEnumValue(value = "source")
        SOURCE,

        @XmlEnumValue(value = "template")
        TEMPLATE
    }
}
