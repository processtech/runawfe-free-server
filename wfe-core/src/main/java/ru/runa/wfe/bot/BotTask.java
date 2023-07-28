package ru.runa.wfe.bot;

import com.google.common.base.MoreObjects;
import com.google.common.base.Objects;
import java.io.Serializable;
import java.util.Date;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.JoinColumn;
import javax.persistence.Lob;
import javax.persistence.ManyToOne;
import javax.persistence.SequenceGenerator;
import javax.persistence.Table;
import javax.persistence.Version;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

@Entity
@Table(name = "BOT_TASK")
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
public class BotTask implements Serializable {
    private static final long serialVersionUID = 1L;

    private Long id;
    private Long version;
    private Bot bot;
    private String name;
    private String taskHandlerClassName;
    private byte[] configuration;
    private byte[] embeddedFile;
    private String embeddedFileName;
    private Date createDate;
    /**
     * Flag, equals true, if tasks from different process instances must be executed sequential; false if parallel execution is allowed.
     */
    private Boolean sequentialExecution = Boolean.FALSE;

    public BotTask() {

    }

    public BotTask(Bot bot, String name) {
        this.bot = bot;
        this.name = name;
        this.createDate = new Date();
    }

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO, generator = "sequence")
    @SequenceGenerator(name = "sequence", sequenceName = "SEQ_BOT_TASK", allocationSize = 1)
    @Column(name = "ID", nullable = false)
    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    @Version
    @Column(name = "VERSION")
    public Long getVersion() {
        return version;
    }

    public void setVersion(Long version) {
        this.version = version;
    }

    @ManyToOne(targetEntity = Bot.class, fetch = FetchType.EAGER)
    @JoinColumn(name = "BOT_ID", nullable = false, updatable = true, insertable = true)
    public Bot getBot() {
        return bot;
    }

    public void setBot(Bot bot) {
        this.bot = bot;
    }

    @Column(name = "NAME", length = 1024)
    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @Column(name = "TASK_HANDLER", length = 1024)
    public String getTaskHandlerClassName() {
        return taskHandlerClassName;
    }

    public void setTaskHandlerClassName(String clazz) {
        taskHandlerClassName = clazz;
    }

    @Lob
    @Column(length = 16777216, name = "CONFIGURATION")
    public byte[] getConfiguration() {
        return configuration;
    }

    public void setConfiguration(byte[] configuration) {
        this.configuration = configuration;
    }

    @Lob
    @Column(length = 16777216, name = "EMBEDDED_FILE")
    public byte[] getEmbeddedFile() {
        return embeddedFile;
    }

    public void setEmbeddedFile(byte[] embeddedFile) {
        this.embeddedFile = embeddedFile;
    }

    @Column(name = "EMBEDDED_FILE_NAME", length = 1024)
    public String getEmbeddedFileName() {
        return embeddedFileName;
    }

    public void setEmbeddedFileName(String embeddedFileName) {
        this.embeddedFileName = embeddedFileName;
    }

    @Column(name = "CREATE_DATE", nullable = false)
    public Date getCreateDate() {
        return createDate;
    }

    public void setCreateDate(Date createDate) {
        this.createDate = createDate;
    }

    @Column(name = "IS_SEQUENTIAL")
    public Boolean isSequentialExecution() {
        return sequentialExecution;
    }

    public void setSequentialExecution(Boolean sequentialExecution) {
        this.sequentialExecution = sequentialExecution == null ? Boolean.FALSE : sequentialExecution;
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(name);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof BotTask) {
            BotTask b = (BotTask) obj;
            return Objects.equal(name, b.name);
        }
        return super.equals(obj);
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this).add("id", id).add("taskHandler", taskHandlerClassName).add("name", name).toString();
    }
}
