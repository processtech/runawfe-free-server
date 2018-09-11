package ru.runa.wfe.script.batch;

import com.google.common.base.Strings;
import com.google.common.collect.Lists;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlEnum;
import javax.xml.bind.annotation.XmlEnumValue;
import javax.xml.bind.annotation.XmlType;
import lombok.val;
import ru.runa.wfe.presentation.BatchPresentation;
import ru.runa.wfe.presentation.BatchPresentationFactory;
import ru.runa.wfe.script.AdminScriptConstants;
import ru.runa.wfe.script.AdminScriptException;
import ru.runa.wfe.script.common.ScriptExecutionContext;
import ru.runa.wfe.script.common.ScriptOperation;
import ru.runa.wfe.script.common.ScriptValidationException;
import ru.runa.wfe.user.Actor;
import ru.runa.wfe.user.Profile;

@XmlType(name = ReplicateBatchPresentationOperation.SCRIPT_NAME + "Type", namespace = AdminScriptConstants.NAMESPACE)
public class ReplicateBatchPresentationOperation extends ScriptOperation {

    public static final String SCRIPT_NAME = "replicateBatchPresentation";

    @XmlAttribute(name = "batchName")
    public String batchName;

    @XmlAttribute(name = "useTemplates")
    public boolean useTemplates = true;

    @XmlAttribute(name = "setActive")
    public SetActiveMode setActiveMode;

    @XmlElement(name = "batchPresentation", namespace = AdminScriptConstants.NAMESPACE)
    public List<XmlBatchPresentation> batchPresentations = Lists.newArrayList();

    @Override
    public void validate(ScriptExecutionContext context) {
    }

    @Override
    public void execute(ScriptExecutionContext context) {
        BatchPresentation srcBatch = null;
        val replaceableBatchPresentations = new HashSet<BatchPresentation>();
        for (XmlBatchPresentation batchElement : batchPresentations) {
            switch (batchElement.batchReplicationType) {
            case SOURCE:
                if (srcBatch != null) {
                    throw new AdminScriptException("Only one source batchPresentation is allowed inside replicateBatchPresentation.");
                }
                srcBatch = batchElement.getBatchPresentation(this, context);
                break;
            case TEMPLATE:
                replaceableBatchPresentations.add(batchElement.getBatchPresentation(this, context));
                break;
            }
        }
        if (srcBatch == null) {
            throw new AdminScriptException("No source BatchPresentation in replicateBatchPresentation found.");
        }
        if (Strings.isNullOrEmpty(batchName)) {
            batchName = srcBatch.getName();
        }
        srcBatch = srcBatch.clone();
        srcBatch.setName(batchName);
        replicateBatchPresentation(context, srcBatch, replaceableBatchPresentations);
    }

    private void replicateBatchPresentation(ScriptExecutionContext context, BatchPresentation replicateMe, Set<BatchPresentation> templates) {
        List<Actor> allActors = (List<Actor>) context.getExecutorLogic().getExecutors(context.getUser(),
                BatchPresentationFactory.ACTORS.createNonPaged());
        List<Long> actorsIds = Lists.newArrayListWithExpectedSize(allActors.size());
        for (Actor actor : allActors) {
            actorsIds.add(actor.getId());
        }
        List<Profile> profiles = context.getProfileLogic().getProfiles(context.getUser(), actorsIds);
        // For all profiles
        for (Profile profile : profiles) {
            if (useTemplates && !isBatchReplaceNeeded(getBatchFromProfile(profile, replicateMe.getCategory(), replicateMe.getName()), templates)) {
                if (setActiveMode.equals(SetActiveMode.ALL) && getBatchFromProfile(profile, replicateMe.getCategory(), replicateMe.getName()) != null) {
                    profile.setActiveBatchPresentation(replicateMe.getCategory(), replicateMe.getName());
                }
                continue;
            }
            BatchPresentation clone = replicateMe.clone();
            clone.setName(replicateMe.getName());
            profile.addBatchPresentation(clone);
            if (setActiveMode.equals(SetActiveMode.ALL) || setActiveMode.equals(SetActiveMode.CHANGED)) {
                profile.setActiveBatchPresentation(replicateMe.getCategory(), replicateMe.getName());
            }
        }
        context.getProfileLogic().updateProfiles(context.getUser(), profiles);
    }

    private BatchPresentation getBatchFromProfile(Profile profile, String batchID, String batchName) {
        for (BatchPresentation batch : profile.getBatchPresentations(batchID)) {
            if (batch.getName().equals(batchName)) {
                return batch;
            }
        }
        throw new ScriptValidationException(this, "batch presentation with name " + batchName + " is not found");
    }

    private boolean isBatchReplaceNeeded(BatchPresentation batch, Collection<BatchPresentation> templates) {
        if (batch == null) {
            return true;
        }
        for (BatchPresentation template : templates) {
            if (template.fieldEquals(batch)) {
                return true;
            }
        }
        return false;
    }

    @XmlEnum(value = String.class)
    public enum SetActiveMode {
        @XmlEnumValue(value = "all")
        ALL,

        @XmlEnumValue(value = "changed")
        CHANGED,

        @XmlEnumValue(value = "none")
        NONE
    };
}
