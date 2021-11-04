package ru.runa.wfe.audit;

import java.io.Serializable;
import java.util.Date;
import javax.persistence.Transient;
import lombok.AllArgsConstructor;

/**
 * Introduces so default method implementations of subinterfaces (like ActionLog) may refer to superinterface methods.
 * <p>
 * Subinterfaces (like ActionLog) must be used by archive-transparent code
 * (i.e. when you need to handle both CurrentActionLog and ArchivedActionLog,* use ActionLog).
 * <p>
 * As for BaseProcessLog vs ProcessLog usage:
 * <ul>
 * <li>NodeGraphElement must use class BaseProcessLog because otherwise JAX-WS won't handle it (Apache CXF would fail on startup).
 * <li>But generic archive-transparent code must use ProcessLog, same as cases where archive-transparent subinterface instances
 *     are assigned to parameter / variable.
 * </ul>
 * So there's an inevitable mess of class and interface usages.
 */
public interface ProcessLog extends Attributes, Comparable<ProcessLog>, Serializable {

    // TODO Consider using this to reduce class hiearchy.
    @AllArgsConstructor
    enum Type {
        ALL(CurrentProcessLog.class, ArchivedProcessLog.class),
        ACTION(CurrentActionLog.class, ArchivedActionLog.class),
        ADMIN_ACTION(CurrentAdminActionLog.class, ArchivedAdminActionLog.class),
        CREATE_TIMER(CurrentCreateTimerLog.class, ArchivedCreateTimerLog.class),
        NODE(CurrentNodeLog.class, ArchivedNodeLog.class),
        NODE_ENTER(CurrentNodeEnterLog.class, ArchivedNodeEnterLog.class),
        NODE_LEAVE(CurrentNodeLeaveLog.class, ArchivedNodeLeaveLog.class),
        PROCESS_ACTIVATE(CurrentProcessActivateLog.class, ArchivedProcessActivateLog.class),
        PROCESS_CANCEL(CurrentProcessCancelLog.class, ArchivedProcessCancelLog.class),
        PROCESS_END(CurrentProcessEndLog.class, ArchivedProcessEndLog.class),
        PROCESS_START(CurrentProcessStartLog.class, ArchivedProcessStartLog.class),
        PROCESS_SUSPEND(CurrentProcessSuspendLog.class, ArchivedProcessSuspendLog.class),
        RECEIVED_MESSAGE(CurrentReceiveMessageLog.class, ArchivedReceiveMessageLog.class),
        SEND_MESSAGE(CurrentSendMessageLog.class, ArchivedSendMessageLog.class),
        SUBPROCESS_END(CurrentSubprocessEndLog.class, ArchivedSubprocessEndLog.class),
        SUBPROCESS_START(CurrentSubprocessStartLog.class, ArchivedSubprocessStartLog.class),
        SWIMLANE_ASSIGN(CurrentSwimlaneAssignLog.class, ArchivedSwimlaneAssignLog.class),
        TASK(CurrentTaskLog.class, ArchivedTaskLog.class),
        TASK_ASSIGN(CurrentTaskAssignLog.class, ArchivedTaskAssignLog.class),
        TASK_CREATE(CurrentTaskCreateLog.class, ArchivedTaskCreateLog.class),
        TASK_DELEGATION(CurrentTaskDelegationLog.class, ArchivedTaskDelegationLog.class),
        TASK_END(CurrentTaskEndLog.class, ArchivedTaskEndLog.class),
        TASK_CANCELLED(CurrentTaskCancelledLog.class, ArchivedTaskCancelledLog.class),
        TASK_END_BY_ADMIN(CurrentTaskEndByAdminLog.class, ArchivedTaskEndByAdminLog.class),
        TASK_END_BY_SUBSTITUTOR(CurrentTaskEndBySubstitutorLog.class, ArchivedTaskEndBySubstitutorLog.class),
        TASK_EXPIRED(CurrentTaskExpiredLog.class, ArchivedTaskExpiredLog.class),
        TASK_REMOVED_ON_PROCESS_END(CurrentTaskRemovedOnProcessEndLog.class, ArchivedTaskRemovedOnProcessEndLog.class),
        TASK_ESCALATION(CurrentTaskEscalationLog.class, ArchivedTaskEscalationLog.class),
        TRANSITION(CurrentTransitionLog.class, ArchivedTransitionLog.class),
        VARIABLE(CurrentVariableLog.class, ArchivedVariableLog.class),
        VARIABLE_CREATE(CurrentVariableCreateLog.class, ArchivedVariableCreateLog.class),
        VARIABLE_DELETE(CurrentVariableDeleteLog.class, ArchivedVariableDeleteLog.class),
        VARIABLE_UPDATE(CurrentVariableUpdateLog.class, ArchivedVariableUpdateLog.class);

        public final Class<? extends CurrentProcessLog> currentRootClass;
        public final Class<? extends ArchivedProcessLog> archivedRootClass;
    }

    @Transient
    Type getType();
    @Transient
    boolean isArchived();

    @Transient
    Long getId();

    @Transient
    Long getProcessId();

    @Transient
    Date getCreateDate();

    @Transient
    Severity getSeverity();

    @Transient
    String getContent();
    @Transient
    String getAttribute(String name);
    @Transient
    String getAttributeNotNull(String name);

    @Transient
    String getNodeId();

    @Transient
    Long getTokenId();

    @Transient
    byte[] getBytes();

    /**
     * Applies some operation to process log instance.
     *
     * @param visitor
     *            Operation to apply.
     */
    void processBy(ProcessLogVisitor visitor);

    @Transient
    String getPatternName();

    /**
     * @return Arguments for localized pattern to format log message description.
     */
    @Transient
    Object[] getPatternArguments();

    /**
     * Formats log message description.
     *
     * @param pattern
     *            localized pattern
     * @return formatted message
     */
    String toString(String pattern, Object... arguments);

}
