package ru.runa.af.web.form;

import ru.runa.common.web.form.IdsForm;

public class RelationPairsForm extends IdsForm {
    private static final long serialVersionUID = 1L;
    public static final String RELATION_ID = "relationId";
    private Long relationId;

    public Long getRelationId() {
        return relationId;
    }

    public void setRelationId(Long relationId) {
        this.relationId = relationId;
    }
}
