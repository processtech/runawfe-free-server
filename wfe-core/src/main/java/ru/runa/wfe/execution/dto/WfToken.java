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
package ru.runa.wfe.execution.dto;

import java.io.Serializable;
import java.util.Date;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;

import ru.runa.wfe.execution.ExecutionStatus;
import ru.runa.wfe.execution.Token;
import ru.runa.wfe.lang.ProcessDefinition;
import ru.runa.wfe.lang.dto.WfNode;
import ru.runa.wfe.security.SecuredObjectBase;
import ru.runa.wfe.security.SecuredObjectType;

import com.google.common.base.MoreObjects;
import com.google.common.base.Objects;

/**
 * @since 4.3.0
 */
@XmlAccessorType(XmlAccessType.FIELD)
public class WfToken extends SecuredObjectBase implements Serializable {
    private static final long serialVersionUID = 1L;
    private Long parentId;
    private Long id;
    private Long processId;
    private String name;
    private Date startDate;
    private Date endDate;
    private WfNode node;
    private String transitionId;
    private ExecutionStatus executionStatus;
    private Date errorDate;
    private String errorMessage;

    public WfToken() {
    }

    public WfToken(Token token, ProcessDefinition processDefinition) {
        parentId = token.getParent() != null ? token.getParent().getId() : null;
        id = token.getId();
        processId = token.getProcess().getId();
        name = token.getName();
        startDate = token.getStartDate();
        endDate = token.getEndDate();
        node = new WfNode(token.getNodeNotNull(processDefinition));
        transitionId = token.getTransitionId();
        executionStatus = token.getExecutionStatus();
        errorDate = token.getErrorDate();
        errorMessage = token.getErrorMessage();
    }

    public Long getParentId() {
        return parentId;
    }

    @Override
    public Long getId() {
        return id;
    }

    public Long getProcessId() {
        return processId;
    }

    public String getName() {
        return name;
    }

    public Date getStartDate() {
        return startDate;
    }

    public Date getEndDate() {
        return endDate;
    }

    public WfNode getNode() {
        return node;
    }

    public String getTransitionId() {
        return transitionId;
    }

    public ExecutionStatus getExecutionStatus() {
        return executionStatus;
    }

    public Date getErrorDate() {
        return errorDate;
    }

    public String getErrorMessage() {
        return errorMessage;
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }

    @Override
    public boolean equals(Object obj) {
        if (obj instanceof WfToken) {
            return Objects.equal(id, ((WfToken) obj).id);
        }
        return super.equals(obj);
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this).add("id", id).add("name", name).toString();
    }

    @Override
    public SecuredObjectType getSecuredObjectType() {
        return SecuredObjectType.TOKENS;
    }

}
