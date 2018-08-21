package ru.runa.wfe.audit;

import java.util.Date;
import javax.persistence.Transient;
import lombok.AllArgsConstructor;

/**
 * Introduces so default method implementations of subinterfaces (like IActionLog) may refer to superinterface methods.
 * <p>
 * Subinterfaces (like IActionLog) must be used by archive-transparent code
 * (i.e. when you need to handle both ActionLog and ArchivedActionLog,* use IActionLog).
 * <p>
 * As for BaseProcessLog vs IProcessLog usage:
 * <ul>
 * <li>NodeGraphElement must use class BaseProcessLog because otherwise JAX-WS won't handle it (Apache CXF would fail on startup).
 * <li>But generic archive-transparent code must use IProcessLog, same as cases where archive-transparent subinterface instances
 *     are assigned to parameter / variable.
 * </ul>
 * So there's an inevitable mess of class and interface usages.
 */
public interface IProcessLog extends Attributes, Comparable<IProcessLog> {

    // TODO Consider using this to reduce class hiearchy.
    @AllArgsConstructor
    enum Type {
        ALL(ProcessLog.class, ArchivedProcessLog.class),
        ACTION(ActionLog.class, ArchivedActionLog.class),
        ADMIN_ACTION(AdminActionLog.class, ArchivedAdminActionLog.class),
        CREATE_TIMER(CreateTimerLog.class, ArchivedCreateTimerLog.class),
        NODE(NodeLog.class, ArchivedNodeLog.class),
        NODE_ENTER(NodeEnterLog.class, ArchivedNodeEnterLog.class),
        NODE_LEAVE(NodeLeaveLog.class, ArchivedNodeLeaveLog.class),
        PROCESS_ACTIVATE(ProcessActivateLog.class, ArchivedProcessActivateLog.class),
        PROCESS_CANCEL(ProcessCancelLog.class, ArchivedProcessCancelLog.class),
        PROCESS_END(ProcessEndLog.class, ArchivedProcessEndLog.class),
        PROCESS_START(ProcessStartLog.class, ArchivedProcessStartLog.class),
        PROCESS_SUSPEND(ProcessSuspendLog.class, ArchivedProcessSuspendLog.class),
        RECEIVED_MESSAGE(ReceiveMessageLog.class, ArchivedReceiveMessageLog.class),
        SEND_MESSAGE(SendMessageLog.class, ArchivedSendMessageLog.class),
        SUBPROCESS_END(SubprocessEndLog.class, ArchivedSubprocessEndLog.class),
        SUBPROCESS_START(SubprocessStartLog.class, ArchivedSubprocessStartLog.class),
        SWIMLANE_ASSIGN(SwimlaneAssignLog.class, ArchivedSwimlaneAssignLog.class),
        TASK(TaskLog.class, ArchivedTaskLog.class),
        TASK_ASSIGN(TaskAssignLog.class, ArchivedTaskAssignLog.class),
        TASK_CREATE(TaskCreateLog.class, ArchivedTaskCreateLog.class),
        TASK_DELEGATION(TaskDelegationLog.class, ArchivedTaskDelegationLog.class),
        TASK_END(TaskEndLog.class, ArchivedTaskEndLog.class),
        TASK_CANCELLED(TaskCancelledLog.class, ArchivedTaskCancelledLog.class),
        TASK_END_BY_ADMIN(TaskEndByAdminLog.class, ArchivedTaskEndByAdminLog.class),
        TASK_END_BY_SUBSTITUTOR(TaskEndBySubstitutorLog.class, ArchivedTaskEndBySubstitutorLog.class),
        TASK_EXPIRED(TaskExpiredLog.class, ArchivedTaskExpiredLog.class),
        TASK_REMOVED_ON_PROCESS_END(TaskRemovedOnProcessEndLog.class, ArchivedTaskRemovedOnProcessEndLog.class),
        TASK_ESCALATION(TaskEscalationLog.class, ArchivedTaskEscalationLog.class),
        TRANSITION(TransitionLog.class, ArchivedTransitionLog.class),
        VARIABLE(VariableLog.class, ArchivedVariableLog.class),
        VARIABLE_CREATE(VariableCreateLog.class, ArchivedVariableCreateLog.class),
        VARIABLE_DELETE(VariableDeleteLog.class, ArchivedVariableDeleteLog.class),
        VARIABLE_UPDATE(VariableUpdateLog.class, ArchivedVariableUpdateLog.class);

        public final Class<? extends ProcessLog> currentRootClass;
        public final Class<? extends ArchivedProcessLog> archivedRootClass;
    }

    @Transient
    Type getType();
    @Transient
    boolean isArchive();

    @Transient
    Long getId();
    void setId(Long id);

    @Transient
    Long getProcessId();
    void setProcessId(Long processId);

    @Transient
    Date getCreateDate();
    void setCreateDate(Date date);

    @Transient
    Severity getSeverity();
    void setSeverity(Severity severity);

    @Transient
    String getContent();
    void setContent(String content);
    @Transient
    String getAttribute(String name);
    @Transient
    String getAttributeNotNull(String name);

    @Transient
    String getNodeId();
    void setNodeId(String nodeId);

    @Transient
    Long getTokenId();
    void setTokenId(Long tokenId);

    @Transient
    byte[] getBytes();
    void setBytes(byte[] bytes);

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
