package ru.runa.wfe.task;

import java.io.Serializable;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.persistence.Version;
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
    @Column(name = "DATA_B64")
    private String dataB64;
}
