package ru.runa.wfe.execution;

import java.io.Serializable;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import lombok.Data;
import ru.runa.wfe.lang.StartNode;

@Entity
@Table(name = "BPM_EVENT_SUBPROCESS_TRIG")
@Data
public class EventSubprocessTrigger implements Serializable {
    private static final long serialVersionUID = 1L;

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO, generator = "sequence")
    @SequenceGenerator(name = "sequence", sequenceName = "SEQ_BPM_EVENT_SUBPROCESS_TRIG", allocationSize = 1)
    @Column(name = "ID", nullable = false)
    private Long id;

    @ManyToOne(targetEntity = CurrentProcess.class, fetch = FetchType.LAZY)
    @JoinColumn(name = "PROCESS_ID")
    private CurrentProcess process;

    @Column(name = "NODE_ID", length = 1024)
    private String nodeId;

    @Column(name = "MESSAGE_SELECTOR", length = 1024)
    private String messageSelector;

    public EventSubprocessTrigger() {
    }

    public EventSubprocessTrigger(CurrentProcess process, StartNode startNode, String messageSelector) {
        this.process = process;
        this.nodeId = startNode.getNodeId();
        this.messageSelector = messageSelector;
    }
}
