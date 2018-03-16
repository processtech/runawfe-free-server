/*
 * This file is part of the RUNA WFE project.
 * 
 * This program is free software; you can redistribute it and/or 
 * modify it under the terms of the GNU Lesser General Public License 
 * as published by the Free Software Foundation; version 2.1 
 * of the License. 
 * 
 * This program is distributed in the hope that it will be useful, 
 * but WITHOUT ANY WARRANTY; without even the implied warranty of 
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the 
 * GNU Lesser General Public License for more details. 
 * 
 * You should have received a copy of the GNU Lesser General Public License 
 * along with this program; if not, write to the Free Software 
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA.
 */
package ru.runa.wfe.bot;

import java.io.Serializable;
import java.util.Date;

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
import org.hibernate.annotations.ForeignKey;
import org.hibernate.annotations.Index;

import com.google.common.base.Objects;

@Entity
@Table(name = "BOT")
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
public class Bot implements Serializable {
    private static final long serialVersionUID = 1L;

    private Long id;
    private Long version;
    private BotStation botStation;
    private String username;
    private String password;
    private Date createDate;
    private Long timeout;
    private Date botTimeout;
    private Long processId;
    private String subprocessNodeId;
    private String subprocessDefinitionNodeId;

    /**
     * Flag, equals true, if all bot tasks must be executed sequential; false if parallel execution is allowed.
     */
    private Boolean sequentialExecution = Boolean.FALSE;
    private Boolean transactional = Boolean.FALSE;

    public Bot() {
    }

    public Bot(BotStation botStation, String username, String password) {
        this.botStation = botStation;
        this.username = username;
        this.password = password;
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
    @ForeignKey(name = "FK_BOT_STATION")
    @Index(name = "IX_BOT_STATION")
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

    @Column(name = "PASSWORD", length = 1024)
    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    @Column(name = "CREATE_DATE", nullable = false)
    public Date getCreateDate() {
        return createDate;
    }

    public void setCreateDate(Date createDate) {
        this.createDate = createDate;
    }

    @Column(name = "TIMEOUT", nullable = false)
    public Long getTimeout() {
        return timeout;
    }

    public void setTimeout(Long timeout) {
        this.timeout = timeout;
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

    @Column(name = "BOT_TIMEOUT")
    public Date getBotTimeout() {
        return botTimeout;
    }

    public void setBotTimeout(Date botTimeout) {
        this.botTimeout = botTimeout;
    }

    @Column(name = "PROCESS_ID")
    public Long getProcessId() {
        return processId;
    }

    public void setProcessId(Long processId) {
        this.processId = processId;
    }

    @Column(name = "SUBPROCESS_NODE_ID")
    public String getSubprocessNodeId() {
        return subprocessNodeId;
    }

    public void setSubprocessNodeId(String subprocessNodeId) {
        this.subprocessNodeId = subprocessNodeId;
    }

    @Column(name = "SUBPROCESS_DEFINITION_NODE_ID")
    public String getSubprocessDefinitionNodeId() {
        return subprocessDefinitionNodeId;
    }

    public void setSubprocessDefinitionNodeId(String subprocessDefinitionNodeId) {
        this.subprocessDefinitionNodeId = subprocessDefinitionNodeId;
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
        return Objects.toStringHelper(this).add("id", id).add("name", username).toString();
    }

}
