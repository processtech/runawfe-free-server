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
import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import lombok.NonNull;
import ru.runa.wfe.execution.Swimlane;
import ru.runa.wfe.user.Executor;

@XmlAccessorType(XmlAccessType.FIELD)
public class WfSwimlane implements Serializable {
    private static final long serialVersionUID = 1L;

    private Long id;
    private String name;
    private Executor executor;
    /**
     * for web services
     */
    public WfSwimlane() {
    }

    public WfSwimlane(@NonNull Swimlane swimlane, Executor assignedExecutor) {
        this.id = swimlane.getId();
        this.name = swimlane.getName();
        this.executor = assignedExecutor;
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public Executor getExecutor() {
        return executor;
    }

}
