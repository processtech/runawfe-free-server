package ru.runa.wfe.task;

import java.io.Serializable;
import javax.persistence.*;

import lombok.Getter;
import lombok.Setter;
import lombok.extern.apachecommons.CommonsLog;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

@Entity
@Table(name = "BPM_TASK_FORM_DRAFT")
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
@CommonsLog
@Getter
@Setter
public class TaskFormDraft implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO, generator = "sequence")
    @SequenceGenerator(name = "sequence", sequenceName = "SEQ_BPM_TASK_FORM_DRAFT", allocationSize = 1)
    @Column(name = "ID")
    private Long id;
    @Version
    @Column(name = "VERSION")
    private Long version;
    @Column(name = "TASK_ID")
    private Long taskId;
    @Column(name = "ACTOR_ID")
    private Long actorId;
    @Lob
    @Basic(fetch = FetchType.LAZY)
    @Column(name = "DATA")
    private byte[] data;
}
