package ru.runa.wf.web.form;

import lombok.Data;
import ru.runa.common.web.form.IdForm;

@Data
public class MoveTokenForm extends IdForm {
    private static final long serialVersionUID = 1L;
    private Long processId;
    private String nodeId;
}
