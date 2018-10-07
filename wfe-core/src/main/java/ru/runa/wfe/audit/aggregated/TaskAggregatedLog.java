package ru.runa.wfe.audit.aggregated;

import java.util.Date;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.annotations.Type;
import ru.runa.wfe.commons.hibernate.DbAwareEnum;

@Entity
@Table(name = "BPM_AGGLOG_TASK")
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
@XmlAccessorType(XmlAccessType.FIELD)
@Getter
@Setter
public class TaskAggregatedLog {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO, generator = "sequence")
    @SequenceGenerator(name = "sequence", sequenceName = "SEQ_BPM_AGGLOG_TASK", allocationSize = 1)
    @Column(name = "ID")
    private Long id;

    @Column(name = "TASK_ID", nullable = false)
    private Long taskId;

    @Column(name = "PROCESS_ID", nullable = false)
    private Long processId;

    /**
     * Actor name which was initially assigned to task.
     */
    @Column(name = "INITIAL_ACTOR_NAME", length = 1024)
    private String initialActorName;

    /**
     * Actor name which completed task. Null if task is not completed or completed not by user (timeout and so on).
     */
    @Column(name = "COMPLETE_ACTOR_NAME", length = 1024)
    private String completeActorName;

    /**
     * Task instance creation date.
     */
    @Column(name = "CREATE_DATE", nullable = false)
    private Date createDate;

    /**
     * Task instance deadline date.
     */
    @Column(name = "DEADLINE_DATE")
    private Date deadlineDate;

    /**
     * Task instance end date. Null if task instance still not ended.
     */
    @Column(name = "END_DATE")
    private Date endDate;

    /**
     * Task instance completion reason.
     */
    @Column(name = "END_REASON", nullable = false)
    @Type(type = "ru.runa.wfe.commons.hibernate.EndReasonEnumType")
    private EndReason endReason;

    @Column(name = "TOKEN_ID", nullable = false)
    private Long tokenId;

    /**
     * Process definition node id.
     */
    @Column(name = "NODE_ID", nullable = false, length = 1024)
    private String nodeId;

    @Column(name = "TASK_NAME", nullable = false, length = 1024)
    private String taskName;

    @Column(name = "TASK_INDEX")
    private Integer taskIndex;

    /**
     * Swimlane, assigned to task.
     */
    @Column(name = "SWIMLANE_NAME", length = 1024)
    private String swimlaneName;

    /**
     * End task reason.
     */
    @RequiredArgsConstructor
    public enum EndReason implements DbAwareEnum<Integer> {
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

        @Getter
        private final Integer dbValue;
    }
}
