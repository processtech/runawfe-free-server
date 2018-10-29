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
