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
package ru.runa.common.web.form;

/**
 * Created on 18.07.2005
 * 
 * @struts:form name = "batchPresentationForm"
 */
public class BatchPresentationForm extends AbstractBatchPresentationForm {
    private static final long serialVersionUID = 5889581982110276396L;

    public static final String BATCH_PRESENTATION_NAME = "batchPresentationName";

    private String batchPresentationName;

    public String getBatchPresentationName() {
        return batchPresentationName;
    }

    public void setBatchPresentationName(String batchPresentationId) {
        batchPresentationName = batchPresentationId;
    }
}
