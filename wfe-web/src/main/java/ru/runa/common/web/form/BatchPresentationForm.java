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
