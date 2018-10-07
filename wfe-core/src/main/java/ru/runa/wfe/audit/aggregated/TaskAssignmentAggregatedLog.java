package ru.runa.wfe.audit.aggregated;

import java.util.Date;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

/**
 * Stores information about task assignment for some object (swimlane, task, etc.)
 */
@Entity
@Table(name = "BPM_AGGLOG_ASSIGNMENT")
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
@Getter
@Setter
public class TaskAssignmentAggregatedLog {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO, generator = "sequence")
    @SequenceGenerator(name = "sequence", sequenceName = "SEQ_BPM_AGGLOG_ASSIGNMENT", allocationSize = 1)
    @Column(name = "ID")
    private long id;

    @ManyToOne(targetEntity = TaskAggregatedLog.class)
    @JoinColumn(name = "AGGLOG_TASK_ID", nullable = false)
    private TaskAggregatedLog log;

    @Column(name = "IDX", nullable = false)
    private Integer idx;

    @Column(name = "ASSIGNMENT_DATE", nullable = false)
    private Date assignDate;

    @Column(name = "OLD_EXECUTOR_NAME", length = 1024)
    private String oldExecutorName;

    @Column(name = "NEW_EXECUTOR_NAME", length = 1024)
    private String newExecutorName;
}
