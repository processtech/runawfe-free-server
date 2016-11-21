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
package ru.runa.wf.web.form;

import ru.runa.common.web.form.ReturnActionForm;

/**
 * Created 01.08.2005
 * 
 * @struts:form name = "pagingForm"
 */
public class PagingForm extends ReturnActionForm {
    private static final long serialVersionUID = 5035693186577220796L;

    public static final String VIEW_PAGE = "viewPage";

    public static final String BATCH_PRESENTATION_ID = "batchPresentationId";

    private int viewPage = -1;

    private String batchPresentationId;

    public int getViewPage() {
        return viewPage;
    }

    public void setViewPage(int viewPage) {
        this.viewPage = viewPage;
    }

    public String getBatchPresentationId() {
        return batchPresentationId;
    }

    public void setBatchPresentationId(String batchPresentationId) {
        this.batchPresentationId = batchPresentationId;
    }
}
