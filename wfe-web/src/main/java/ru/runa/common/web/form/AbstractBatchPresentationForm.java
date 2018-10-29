package ru.runa.common.web.form;

/**
 * Created on 28.10.2005
 * 
 */
public class AbstractBatchPresentationForm extends ReturnActionForm {
    private static final long serialVersionUID = 603213747881966539L;

    public static final String BATCH_PRESENTATION_ID = "batchPresentationId";

    private String batchPresentationId;

    public String getBatchPresentationId() {
        return batchPresentationId;
    }

    public void setBatchPresentationId(String batchPresentationId) {
        this.batchPresentationId = batchPresentationId;
    }
}
