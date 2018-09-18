package ru.runa.wfe.audit.aggregated;

import java.util.Date;
import javax.persistence.Column;
import javax.persistence.DiscriminatorColumn;
import javax.persistence.DiscriminatorType;
import javax.persistence.DiscriminatorValue;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

/**
 * Stores information about assignment for some object (swimlane, task e.t.c)
 */
@Entity
@Table(name = "BPM_AGGLOG_ASSIGNMENTS")
@Inheritance(strategy = InheritanceType.SINGLE_TABLE)
@DiscriminatorColumn(name = "DISCRIMINATOR", discriminatorType = DiscriminatorType.CHAR)
@DiscriminatorValue(value = "V")
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
public class AssignmentHistory {

    private long id;
    /**
     * Date of assignment.
     */
    private Date assignDate;
    /**
     * Previous assignment executor.
     */
    private String oldExecutorName;
    /**
     * New assignment executor name.
     */
    private String newExecutorName;

    public AssignmentHistory() {
        super();
    }

    public AssignmentHistory(long objectId, Date assignDate, String oldExecutorName, String newExecutorName) {
        this.assignDate = assignDate;
        this.oldExecutorName = oldExecutorName;
        this.newExecutorName = newExecutorName;
    }

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO, generator = "sequence")
    @SequenceGenerator(name = "sequence", sequenceName = "SEQ_BPM_AGGLOG_ASSIGNMENTS", allocationSize = 1)
    @Column(name = "ID")
    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    @Column(name = "ASSIGNMENT_DATE", nullable = false)
    public Date getAssignDate() {
        return assignDate;
    }

    public void setAssignDate(Date assignDate) {
        this.assignDate = assignDate;
    }

    @Column(name = "OLD_EXECUTOR_NAME", length = 1024)
    public String getOldExecutorName() {
        return oldExecutorName;
    }

    public void setOldExecutorName(String oldExecutorName) {
        this.oldExecutorName = oldExecutorName;
    }

    @Column(name = "NEW_EXECUTOR_NAME", length = 1024)
    public String getNewExecutorName() {
        return newExecutorName;
    }

    public void setNewExecutorName(String newExecutorName) {
        this.newExecutorName = newExecutorName;
    }
}
