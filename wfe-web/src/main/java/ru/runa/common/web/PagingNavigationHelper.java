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
package ru.runa.common.web;

import java.util.Map;

import javax.servlet.jsp.PageContext;

import org.apache.ecs.Entities;
import org.apache.ecs.html.A;
import org.apache.ecs.html.B;
import org.apache.ecs.html.TD;
import org.apache.ecs.html.TR;
import org.apache.ecs.html.Table;

import ru.runa.common.web.form.ReturnActionForm;
import ru.runa.wf.web.action.SetViewPageAction;
import ru.runa.wf.web.form.PagingForm;
import ru.runa.wfe.commons.web.PortletUrlType;
import ru.runa.wfe.presentation.BatchPresentation;

import com.google.common.collect.Maps;

/**
 * Helper class to create paging navigation elements.
 * 
 * @author Konstantinov Aleksey 04.03.2012
 */
public final class PagingNavigationHelper {
    private static final int MAX_PAGING_NOVIGATION_ELEMENTS_TO_SHOW = 10;

    /**
     * Processing request page context.
     */
    private final PageContext pageContext;

    /**
     * {@linkplain BatchPresentation} used to load objects. May be null if only objects count must be shown (without paging navigation).
     */
    private final BatchPresentation batchPresentation;

    /**
     * All instances count (not only shown, but all available for {@linkplain BatchPresentation}).
     */
    private final int instanceCount;

    /**
     * Return action (current page). May be null if only instances count must be shown (without paging navigation).
     */
    private final String returnAction;

    /**
     * Flag equals true to show only instances count; false to show paging navigation and instances count.
     */
    private final boolean onlyCount;

    /**
     * Flag equals true to show download link.
     */
    private final boolean showDownload;

    /**
     * Create helper for adding paging navigation and instances count.
     * 
     * @param pageContext
     *            Processing request page context.
     * @param batchPresentation
     *            {@linkplain BatchPresentation} used to load objects.
     * @param instanceCount
     *            All instances count (not only shown, but all available for {@linkplain BatchPresentation}).
     * @param returnAction
     *            Return action (current page).
     */
    public PagingNavigationHelper(PageContext pageContext, BatchPresentation batchPresentation, int instanceCount, String returnAction) {
        this(pageContext, batchPresentation, instanceCount, returnAction, false);
    }

    /**
     * Create helper for adding paging navigation and instances count.
     * 
     * @param pageContext
     *            Processing request page context.
     * @param batchPresentation
     *            {@linkplain BatchPresentation} used to load objects.
     * @param instanceCount
     *            All instances count (not only shown, but all available for {@linkplain BatchPresentation}).
     * @param returnAction
     *            Return action (current page).
     * @param showDownload
     *            Show download link (current page).
     */
    public PagingNavigationHelper(PageContext pageContext, BatchPresentation batchPresentation, int instanceCount, String returnAction,
            boolean showDownload) {
        super();
        this.pageContext = pageContext;
        this.batchPresentation = batchPresentation;
        this.instanceCount = instanceCount;
        this.returnAction = returnAction;
        this.showDownload = showDownload;
        onlyCount = false;
    }

    /**
     * Create helper for adding only instances count.
     * 
     * @param pageContext
     *            Processing request page context.
     * @param instanceCount
     *            All instances count (not only shown, but all available for {@linkplain BatchPresentation}).
     */
    public PagingNavigationHelper(PageContext pageContext, int instanceCount) {
        super();
        this.pageContext = pageContext;
        this.batchPresentation = null;
        this.instanceCount = instanceCount;
        this.returnAction = null;
        onlyCount = true;
        showDownload = false;
    }

    public void addPagingNavigationTable(TD pagingTableToBeAddedTD) {
        if (instanceCount <= 0) {
            return;
        }
        Table pagingTable = new Table();
        pagingTable.setClass(ru.runa.common.web.Resources.CLASS_PAGING_TABLE);
        TR pagingTR = new TR();
        if (!onlyCount) {
            int pageCount = pageCount(instanceCount, batchPresentation.getRangeSize());
            if (pageCount > 1) {
                pagingTR.addElement(createPagingNavigationTD(pageCount));
            }
        }
        if (showDownload) {
            pagingTR.addElement(createDownloadTD());
        }
        pagingTR.addElement(createElementCountTD());
        pagingTable.addElement(pagingTR);
        pagingTableToBeAddedTD.addElement(pagingTable);
    }

    private TD createDownloadTD() {
        final TD downloadTd = new TD();
        downloadTd.setClass(ru.runa.common.web.Resources.CLASS_PAGING_NAVIGATION_TD);
        final Map<String, Object> params = Maps.newHashMap();
        params.put(PagingForm.BATCH_PRESENTATION_ID, batchPresentation.getCategory());
        final String actionUrl = Commons.getUrl("/download", params, pageContext, PortletUrlType.Resource);
        final A href = new A(actionUrl, MessagesBatch.DOWNLOAD_AS_EXEL.message(pageContext));
        downloadTd.addElement(href);
        return downloadTd;
    }

    private static int pageCount(int objectCount, int pageSize) {
        if (pageSize <= 0) {
            return 1;
        }
        int result = objectCount / pageSize;
        if (objectCount % pageSize > 0) {
            result++;
        }
        if (result == 0) {
            result = 1;
        }
        return result;
    }

    private TD createPagingNavigationTD(int pageCount) {
        int currentPageNumber = batchPresentation.getPageNumber();
        TD pagingNovigationElementTD = new TD();
        pagingNovigationElementTD.setClass(ru.runa.common.web.Resources.CLASS_PAGING_NAVIGATION_TD);

        int startIndex = 1;
        int endIndex = pageCount;

        if (pageCount >= MAX_PAGING_NOVIGATION_ELEMENTS_TO_SHOW) {
            startIndex = Math.max(1, currentPageNumber - MAX_PAGING_NOVIGATION_ELEMENTS_TO_SHOW / 2);
            endIndex = startIndex + MAX_PAGING_NOVIGATION_ELEMENTS_TO_SHOW;
            if (endIndex > pageCount) {
                startIndex -= endIndex - pageCount;
                startIndex = Math.max(1, startIndex);
                endIndex = pageCount;
            }
        }
        if (startIndex > 1) {
            int prevPageNumber = Math.max(1, currentPageNumber - MAX_PAGING_NOVIGATION_ELEMENTS_TO_SHOW);
            addPageNovigationElement(pagingNovigationElementTD, prevPageNumber, MessagesBatch.PAGING_PREV_RANGE.message(pageContext));
            addDoubleNBSP(pagingNovigationElementTD);
        }

        if (currentPageNumber > 1) {
            addPageNovigationElement(pagingNovigationElementTD, currentPageNumber - 1, MessagesBatch.PAGING_PREV_PAGE.message(pageContext));
            addDoubleNBSP(pagingNovigationElementTD);
        }
        for (int i = startIndex; i <= endIndex; i++) {
            pagingNovigationElementTD.addElement(Entities.NBSP);
            if (i == currentPageNumber) {
                pagingNovigationElementTD.addElement(new B().addElement(String.valueOf(currentPageNumber)));
            } else {
                addPageNovigationElement(pagingNovigationElementTD, i, String.valueOf(i));
            }
        }
        addDoubleNBSP(pagingNovigationElementTD);
        if (currentPageNumber < pageCount) {
            addPageNovigationElement(pagingNovigationElementTD, currentPageNumber + 1, MessagesBatch.PAGING_NEXT_PAGE.message(pageContext));
            addDoubleNBSP(pagingNovigationElementTD);
        }
        if (endIndex < pageCount) {
            int nextPageNumber = Math.min(pageCount, currentPageNumber + MAX_PAGING_NOVIGATION_ELEMENTS_TO_SHOW);
            addPageNovigationElement(pagingNovigationElementTD, nextPageNumber, MessagesBatch.PAGING_NEXT_PAGE.message(pageContext));
            addDoubleNBSP(pagingNovigationElementTD);
        }
        return pagingNovigationElementTD;
    }

    private TD createElementCountTD() {
        TD elementsCountTd = new TD();
        elementsCountTd.setClass(ru.runa.common.web.Resources.CLASS_PAGING_TOTAL_COUNT_TD);
        elementsCountTd.addElement(new B().addElement(MessagesBatch.PAGING_TOTAL.message(pageContext) + instanceCount));
        return elementsCountTd;
    }

    private void addPageNovigationElement(TD listTd, int prevPageNumber, String textPresentation) {
        Map<String, Object> params = Maps.newHashMap();
        params.put(PagingForm.BATCH_PRESENTATION_ID, batchPresentation.getCategory());
        params.put(ReturnActionForm.RETURN_ACTION, returnAction);
        params.put(PagingForm.VIEW_PAGE, prevPageNumber);
        String actionUrl = Commons.getActionUrl(SetViewPageAction.ACTION_PATH, params, pageContext, PortletUrlType.Action);
        A hrefBack = new A(actionUrl, textPresentation);
        listTd.addElement(hrefBack);
    }

    private void addDoubleNBSP(TD listTd) {
        listTd.addElement(Entities.NBSP);
        listTd.addElement(Entities.NBSP);
    }
}
