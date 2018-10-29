package ru.runa.wf.web.form;

/**
 * @struts:form name = "processForm"
 */
public class ProcessForm extends CommonProcessForm {
    private static final long serialVersionUID = 1L;
    public static final String ACTOR_ID_INPUT_NAME = "actorId";
    private Long actorId;

    public Long getActorId() {
        return actorId;
    }

    public void setActorId(Long actorId) {
        this.actorId = actorId;
    }
}
