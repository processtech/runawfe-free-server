package ru.runa.wfe.bot;

import com.google.common.base.MoreObjects;
import com.google.common.base.Objects;
import java.io.Serializable;
import java.util.Date;
import java.util.concurrent.TimeUnit;
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
import javax.persistence.Version;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;

@Entity
@Table(name = "BOT")
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
public class Bot implements Serializable {
    private static final long serialVersionUID = 1L;

    private Long id;
    private Long version;
    private BotStation botStation;
    private String username;
    private Date createDate;
    /**
     * Whether should all bot tasks be executed sequentially. False for parallel execution.
     */
    private Boolean sequentialExecution = Boolean.FALSE;
    /**
     * In case of true all bot tasks from transactional embedded subprocess will be bound to this bot.
     * 
     * Bot will not handle any other tasks till embedded subprocesses finishes or transactionalTimeout expired.
     */
    private Boolean transactional = Boolean.FALSE;
    /**
     * Configures binding to transactional embedded subprocess timeout in minutes
     */
    private Long transactionalTimeout;
    /**
     * Calculated binding expiration date to transactional embedded subprocess
     */
    private Date boundDueDate;
    /**
     * Process id to which transactional bot is bound
     */
    private Long boundProcessId;
    /**
     * Embedded subprocess id to which transactional bot is bound
     */
    private String boundSubprocessId;

    public Bot() {
    }

    public Bot(BotStation botStation, String username) {
        this.botStation = botStation;
        this.username = username;
        this.createDate = new Date();
    }

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO, generator = "sequence")
    @SequenceGenerator(name = "sequence", sequenceName = "SEQ_BOT", allocationSize = 1)
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

    @ManyToOne(targetEntity = BotStation.class, fetch = FetchType.EAGER)
    @JoinColumn(name = "BOT_STATION_ID", nullable = false, updatable = true, insertable = true)
    public BotStation getBotStation() {
        return botStation;
    }

    public void setBotStation(BotStation bs) {
        botStation = bs;
    }

    /**
     * Username for authentification on WFE server.
     */
    @Column(name = "USERNAME", length = 1024)
    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
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

    @Column(name = "IS_TRANSACTIONAL")
    public Boolean isTransactional() {
        return transactional;
    }

    public void setTransactional(Boolean transactional) {
        this.transactional = transactional == null ? Boolean.FALSE : transactional;
    }

    @Column(name = "TRANSACTIONAL_TIMEOUT")
    public Long getTransactionalTimeout() {
        return transactionalTimeout;
    }

    public void setTransactionalTimeout(Long transactionalTimeout) {
        this.transactionalTimeout = transactionalTimeout;
    }

    @Column(name = "BOUND_DUE_DATE")
    public Date getBoundDueDate() {
        return boundDueDate;
    }

    public void setBoundDueDate(Date boundDueDate) {
        this.boundDueDate = boundDueDate;
    }

    @Column(name = "BOUND_PROCESS_ID")
    public Long getBoundProcessId() {
        return boundProcessId;
    }

    public void setBoundProcessId(Long boundProcessId) {
        this.boundProcessId = boundProcessId;
    }

    @Column(name = "BOUND_SUBPROCESS_ID")
    public String getBoundSubprocessId() {
        return boundSubprocessId;
    }

    public void setBoundSubprocessId(String boundSubprocessId) {
        this.boundSubprocessId = boundSubprocessId;
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(username);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof Bot) {
            Bot b = (Bot) obj;
            return Objects.equal(username, b.username);
        }
        return super.equals(obj);
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this).add("id", id).add("name", username).toString();
    }

    public void bindToEmbeddedSubprocess(Long processId, String subprocessId) {
        setBoundDueDate(new Date(System.currentTimeMillis() + TimeUnit.MINUTES.toMillis(getTransactionalTimeout())));
        setBoundProcessId(processId);
        setBoundSubprocessId(subprocessId);
    }

    public void unbindFromEmbeddedSubprocess() {
        setBoundDueDate(null);
        setBoundProcessId(null);
        setBoundSubprocessId(null);
    }

}
