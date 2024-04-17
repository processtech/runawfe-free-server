package ru.runa.wfe.lang;

import com.google.common.base.Preconditions;
import ru.runa.wfe.commons.SystemProperties;

/**
 * defines a task and how the actor must be calculated at runtime.
 */
public class TaskDefinition extends GraphElement {
    private static final long serialVersionUID = 1L;

    protected String deadlineDuration;
    protected InteractionNode node;
    protected SwimlaneDefinition swimlaneDefinition;
    /**
     * reassign swimlane value to evaluated swimlane initializer due to task create
     */
    protected Boolean reassignSwimlaneToInitializer = null;
    // TODO switch reassignSwimlane to useSwimlaneInitializerForTaskExecutor;
    /**
     * reassign swimlane value to actor who completed task on task end
     */
    protected Boolean reassignSwimlaneToTaskPerformer = null;
    protected boolean ignoreSubsitutionRules;
    protected Boolean taskButtonLabelBySingleTransitionName = null;

    @Override
    public void validate() {
        super.validate();
        Preconditions.checkNotNull(node, "node in " + this);
        if (!(node instanceof MultiTaskNode)) {
            Preconditions.checkNotNull(swimlaneDefinition, "swimlane in " + this);
        }
    }

    /**
     * sets the swimlane unidirectionally. Since a task can have max one of swimlane or assignmentHandler, this method removes the assignmentHandler
     * and assignmentExpression if one of those isset.
     */
    public void setSwimlane(SwimlaneDefinition swimlaneDefinition) {
        this.swimlaneDefinition = swimlaneDefinition;
    }

    public boolean isReassignSwimlaneToInitializer() {
        return reassignSwimlaneToInitializer != null ? reassignSwimlaneToInitializer : SystemProperties.isReassignSwimlaneToInitializer();
    }

    public void setReassignSwimlaneToInitializer(boolean reassignSwimlaneToInitializer) {
        this.reassignSwimlaneToInitializer = reassignSwimlaneToInitializer;
    }

    public boolean isReassignSwimlaneToTaskPerformer() {
        return reassignSwimlaneToTaskPerformer != null ? reassignSwimlaneToTaskPerformer : SystemProperties.isReassignSwimlaneToTaskPerformer();
    }

    public void setReassignSwimlaneToTaskPerformer(boolean reassignSwimlaneToTaskExecutor) {
        this.reassignSwimlaneToTaskPerformer = reassignSwimlaneToTaskExecutor;
    }

    public boolean isIgnoreSubsitutionRules() {
        return ignoreSubsitutionRules;
    }

    public void setIgnoreSubsitutionRules(boolean ignoreSubsitutionRules) {
        this.ignoreSubsitutionRules = ignoreSubsitutionRules;
    }

    @Override
    public GraphElement getParentElement() {
        return node;
    }

    public SwimlaneDefinition getSwimlane() {
        return swimlaneDefinition;
    }

    public InteractionNode getNode() {
        return node;
    }

    public void setNode(InteractionNode node) {
        this.node = node;
    }

    public String getDeadlineDuration() {
        return deadlineDuration;
    }

    public void setDeadlineDuration(String deadlineDuration) {
        this.deadlineDuration = deadlineDuration;
    }

    public Boolean isTaskButtonLabelBySingleTransitionName() {
        return this.taskButtonLabelBySingleTransitionName;
    }

    public void setTaskButtonLabelBySingleTransitionName(Boolean value) {
        this.taskButtonLabelBySingleTransitionName = value;
    }
}
