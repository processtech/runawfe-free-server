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
package ru.runa.wfe.definition.dto;

import com.google.common.base.Objects;
import ru.runa.wfe.commons.CalendarUtil;
import ru.runa.wfe.definition.ProcessDefinitionChange;
import ru.runa.wfe.security.Identifiable;
import ru.runa.wfe.security.SecuredObjectType;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import java.io.Serializable;
import java.util.Calendar;

@XmlAccessorType(XmlAccessType.FIELD)
public class WfProcessDefinitionChange extends Identifiable implements Serializable, Comparable<WfProcessDefinitionChange> {
    private static final long serialVersionUID = -6077491023576117945L;
    private Long version;
    private Long deploymentId;
    private Calendar date;
    private String author;
    private String comment;

    public WfProcessDefinitionChange() {
    }

    public WfProcessDefinitionChange(ProcessDefinitionChange processDefinitionChange) {
        this.version = processDefinitionChange.getVersion();
        this.deploymentId = processDefinitionChange.getDeploymentId();
        this.date = processDefinitionChange.getDate();
        this.author = processDefinitionChange.getAuthor();
        this.comment = processDefinitionChange.getComment();
    }

    public Long getVersion() {
        return version;
    }

    public Calendar getDate() {
        return date;
    }

    public String getAuthor() {
        return author;
    }

    public String getComment() {
        return comment;
    }

    @Override
    public Long getIdentifiableId() {
        return getId();
    }

    public Long getId(){
        return version;
    }

    @Override
    public int hashCode() {
        return this.getVersion().toString().concat(CalendarUtil.format(this.getDate(), CalendarUtil.DATE_WITH_HOUR_MINUTES_FORMAT))
                .concat(this.getAuthor()).concat(this.getComment()).hashCode();
    }

    @Override
    public boolean equals(Object other) {
        if (other == null) {
            return false;
        }
        if (other == this) {
            return true;
        }
        if ((other instanceof WfProcessDefinitionChange) != true) {
            return false;
        }

        WfProcessDefinitionChange processDefinitionChange = (WfProcessDefinitionChange) other;
        if (processDefinitionChange.getVersion().equals(this.getVersion()) && processDefinitionChange.getDate().equals(this.getDate())
                && processDefinitionChange.getAuthor().equals(this.getAuthor())
                && processDefinitionChange.getComment().equals(this.getComment())) {
            return true;
        }

        return false;
    }

    @Override
    public int compareTo(WfProcessDefinitionChange other) {
        int result = 0;
        if (this.getVersion() > other.getVersion()) {
            result = 1;
        }else if (this.getVersion() < other.getVersion()){
            result = -1;
        }
        return result;
    }

    @Override
    public SecuredObjectType getSecuredObjectType() {
        return SecuredObjectType.DEFINITION;
    }

    @Override
    public String toString() {
        return Objects.toStringHelper(this).add("version", version).add("date", date).add("author", author)
                .add("comment", comment).toString();
    }

}
