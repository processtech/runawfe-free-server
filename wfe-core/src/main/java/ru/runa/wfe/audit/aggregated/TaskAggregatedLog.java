package ru.runa.wfe.audit.aggregated;

import java.util.Date;
import java.util.EnumSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.OneToMany;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;

import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.annotations.Cascade;
import org.hibernate.annotations.CascadeType;
import org.hibernate.annotations.Index;
import org.hibernate.annotations.IndexColumn;

import ru.runa.wfe.audit.TaskAssignLog;
import ru.runa.wfe.audit.TaskCreateLog;
import ru.runa.wfe.definition.dao.IProcessDefinitionLoader;
import ru.runa.wfe.execution.Process;
import ru.runa.wfe.execution.Token;
import ru.runa.wfe.lang.InteractionNode;
import ru.runa.wfe.lang.Node;
import ru.runa.wfe.lang.TaskDefinition;

import com.google.common.base.Strings;
import com.google.common.collect.Maps;

@Entity
@Table(name = "BPM_AGGLOG_TASKS")
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
@XmlAccessorType(XmlAccessType.FIELD)
public class TaskAggregatedLog {
    /**
     * Identity for this log instance.
     */
    private Long id;
    /**
     * Task instance id.
     */
    private Long taskId;
    /**
     * Process instance id.
     */
    private Long processId;
    /**
     * Actor name, which initially assigned to task.
     */
    private String initialActorName;
    /**
     * Actor name, which complete task. May be null if task not completed or
     * completed not by user (timeout and so on).
     */
    private String completeActorName;
    /**
     * Task instance creation date.
     */
    private Date createDate;
    /**
     * Task instance deadline date.
     */
    private Date deadlineDate;
    /**
     * Task instance end date. May be null, if task instance still not ended.
     */
    private Date endDate;
    /**
     * Task instance complete reason.
     */
    private EndReason endReason;
    /**
     * Token id.
     */
    private Long tokenId;
    /**
     * Process definition node id.
     */
    private String nodeId;
    /**
     * Task name.
     */
    private String taskName;
    private Integer taskIndex;
    /**
     * Swimlane, assigned to task.
     */
    private String swimlaneName;
    /**
     * Assignment history for task instance. Initial assignment and completed
     * actor is also here.
     */
    private List<TaskAssignmentHistory> assignmentHistory = new LinkedList<TaskAssignmentHistory>();

    public TaskAggregatedLog() {
        super();
    }

    public TaskAggregatedLog(TaskCreateLog taskCreateLog, IProcessDefinitionLoader processDefinitionLoader, Process process, Token token) {
        taskId = taskCreateLog.getTaskId();
        processId = taskCreateLog.getProcessId();
        createDate = taskCreateLog.getCreateDate();
        deadlineDate = taskCreateLog.getDeadlineDate();
        tokenId = taskCreateLog.getTokenId();
        nodeId = taskCreateLog.getNodeId();
        taskName = taskCreateLog.getTaskName();
        taskIndex = taskCreateLog.getTaskIndex();
        Node node = processDefinitionLoader.getDefinition(process).getNode(taskCreateLog.getNodeId());
        if (node != null && node instanceof InteractionNode) {
            List<TaskDefinition> tasks = ((InteractionNode) node).getTasks();
            if (tasks != null && !tasks.isEmpty() && tasks.get(0).getSwimlane() != null) {
                swimlaneName = tasks.get(0).getSwimlane().getName();
            }
        }
        endReason = EndReason.PROCESSING;
    }

    /**
     * Updates information on task assignment.
     *
     * @param taskAssignLog
     *            Task assignment log to update information.
     */
    public void updateAssignment(TaskAssignLog taskAssignLog) {
        saveAssignment(taskAssignLog.getCreateDate(), taskAssignLog.getNewExecutorName());
        if (!Strings.isNullOrEmpty(initialActorName)) {
            return;
        }
        initialActorName = taskAssignLog.getNewExecutorName();
    }

    /**
     * Updates information on task end.
     *
     * @param endDate
     *            Task instance end date.
     * @param actorName
     *            Actor, which end's task.
     * @param endReason
     *            Task instance complete reason.
     */
    public void updateOnEnd(Date endDate, String actorName, EndReason endReason) {
        saveAssignment(endDate, actorName);
        this.endDate = endDate;
        completeActorName = actorName;
        this.endReason = endReason;
    }

    /**
     * Save assignment if actor name is changed.
     *
     * @param assignmentDate
     *            Assignment date.
     * @param newExecutorName
     *            Assignment actor name.
     */
    private void saveAssignment(Date assignmentDate, String newExecutorName) {
        String oldExecutorName = null;
        if (!assignmentHistory.isEmpty()) {
            oldExecutorName = assignmentHistory.get(assignmentHistory.size() - 1).getNewExecutorName();
            for (TaskAssignmentHistory assignment : assignmentHistory) {
                // This check is for import - assignment may already be saved
                // before import operation.
                if (assignment.getAssingnDate().equals(assignmentDate) && assignment.getNewExecutorName().equals(newExecutorName)) {
                    oldExecutorName = newExecutorName;
                    break;
                }
            }
        }
        if (oldExecutorName == null || !oldExecutorName.equals(newExecutorName)) {
            assignmentHistory.add(new TaskAssignmentHistory(this, id, assignmentDate, oldExecutorName, newExecutorName));
        }
    }

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO, generator = "sequence")
    @SequenceGenerator(name = "sequence", sequenceName = "SEQ_BPM_AGGLOG_TASKS", allocationSize = 1)
    @Column(name = "ID")
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    @Column(name = "TASK_ID", nullable = false)
    public Long getTaskId() {
        return taskId;
    }

    public void setTaskId(Long taskId) {
        this.taskId = taskId;
    }

    @Column(name = "PROCESS_ID", nullable = false)
    @Index(name = "IX_AGGLOG_TASKS_PROCESS")
    public Long getProcessId() {
        return processId;
    }

    public void setProcessId(Long processId) {
        this.processId = processId;
    }

    @Column(name = "INITIAL_ACTOR_NAME")
    public String getInitialActorName() {
        return initialActorName;
    }

    public void setInitialActorName(String initialActorName) {
        this.initialActorName = initialActorName;
    }

    @Column(name = "COMPLETE_ACTOR_NAME")
    public String getCompleteActorName() {
        return completeActorName;
    }

    public void setCompleteActorName(String completeActorName) {
        this.completeActorName = completeActorName;
    }

    @Column(name = "CREATE_DATE", nullable = false)
    @Index(name = "IX_AGGLOG_TASKS_CREATE_DATE")
    public Date getCreateDate() {
        return createDate;
    }

    public void setCreateDate(Date createDate) {
        this.createDate = createDate;
    }

    @Column(name = "DEADLINE_DATE")
    public Date getDeadlineDate() {
        return deadlineDate;
    }

    public void setDeadlineDate(Date deadlineDate) {
        this.deadlineDate = deadlineDate;
    }

    @Column(name = "END_DATE")
    @Index(name = "IX_AGGLOG_TASKS_END_DATE")
    public Date getEndDate() {
        return endDate;
    }

    public void setEndDate(Date endDate) {
        this.endDate = endDate;
    }

    @Column(name = "END_REASON", nullable = false)
    public int getEndReason() {
        return endReason.getDbValue();
    }

    public void setEndReason(int endReason) {
        this.endReason = EndReason.fromDbValue(endReason);
    }

    @Column(name = "TOKEN_ID", nullable = false)
    public Long getTokenId() {
        return tokenId;
    }

    public void setTokenId(Long tokenId) {
        this.tokenId = tokenId;
    }

    @Column(name = "NODE_ID", nullable = false)
    public String getNodeId() {
        return nodeId;
    }

    public void setNodeId(String nodeId) {
        this.nodeId = nodeId;
    }

    @Column(name = "TASK_NAME", nullable = false)
    public String getTaskName() {
        return taskName;
    }

    public void setTaskName(String taskName) {
        this.taskName = taskName;
    }

    @Column(name = "TASK_INDEX")
    public Integer getTaskIndex() {
        return taskIndex;
    }

    public void setTaskIndex(Integer taskIndex) {
        this.taskIndex = taskIndex;
    }

    @Column(name = "SWIMLANE_NAME")
    public String getSwimlaneName() {
        return swimlaneName;
    }

    public void setSwimlaneName(String swimlaneName) {
        this.swimlaneName = swimlaneName;
    }

    @OneToMany(targetEntity = TaskAssignmentHistory.class, fetch = FetchType.EAGER)
    @JoinColumn(name = "ASSIGNMENT_OBJECT_ID", nullable = false)
    @IndexColumn(name = "IDX")
    @Cascade({ CascadeType.ALL, CascadeType.DELETE_ORPHAN })
    public List<TaskAssignmentHistory> getAssignmentHistory() {
        return assignmentHistory;
    }

    public void setAssignmentHistory(List<TaskAssignmentHistory> assignmentHistory) {
        this.assignmentHistory = assignmentHistory;
    }

    /**
     * End task reason.
     */
    public static enum EndReason {
        /**
         * Something wrong - unsupported value e.t.c.
         */
        UNKNOWN(-1),
        /**
         * Task is in processing.
         */
        PROCESSING(0),
        /**
         * Task is completed normally.
         */
        COMPLETED(1),
        /**
         * Task was cancelled.
         */
        CANCELLED(2),
        /**
         * Task was timeout.
         */
        TIMEOUT(3),
        /**
         * Task was end by substitutor.
         */
        SUBSTITUTOR_END(4),
        /**
         * Task was end by process end.
         */
        PROCESS_END(5),
        /**
         * Task was end by admin.
         */
        ADMIN_END(6);

        /**
         * Value, used to store reason in database.
         */
        private final int dbValue;

        private final static Map<Integer, EndReason> registry = Maps.newHashMap();

        static {
            for (EndReason reason : EnumSet.allOf(EndReason.class)) {
                registry.put(reason.getDbValue(), reason);
            }
        }

        private EndReason(int dbValue) {
            this.dbValue = dbValue;
        }

        public int getDbValue() {
            return dbValue;
        }

        public static EndReason fromDbValue(int dbValue) {
            EndReason reason = registry.get(dbValue);
            return reason == null ? EndReason.UNKNOWN : reason;
        }
    }
}
