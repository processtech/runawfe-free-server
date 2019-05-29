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
