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
package ru.runa.af.web.form;

import org.apache.struts.action.ActionForm;

public class RelationPairForm extends ActionForm {
    private static final long serialVersionUID = 1L;
    public static final String RELATION_ID = "relationId";
    public static final String EXECUTOR_FROM = "executorFrom";
    public static final String EXECUTOR_TO = "executorTo";
    private Long relationId;
    private String executorFrom;
    private String executorTo;

    public Long getRelationId() {
        return relationId;
    }

    public void setRelationId(Long relationId) {
        this.relationId = relationId;
    }

    public String getExecutorFrom() {
        return executorFrom;
    }

    public void setExecutorFrom(String relationFrom) {
        executorFrom = relationFrom;
    }

    public String getExecutorTo() {
        return executorTo;
    }

    public void setExecutorTo(String relationTo) {
        executorTo = relationTo;
    }

}
