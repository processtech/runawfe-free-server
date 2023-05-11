package ru.runa.af.web.tag;

import org.apache.ecs.html.Input;
import org.apache.ecs.html.TD;
import org.tldgen.annotations.Attribute;
import org.tldgen.annotations.BodyContent;
import ru.runa.af.web.action.CreateDigitalSignatureAction;
import ru.runa.af.web.form.CreateDigitalSignatureForm;
import ru.runa.af.web.html.DigitalSignatureTableBuilder;
import ru.runa.af.web.MessagesExecutor;
import ru.runa.common.web.MessagesCommon;
import ru.runa.common.web.tag.TitledFormTag;
import ru.runa.wfe.security.Permission;
import ru.runa.wfe.security.SecuredSingleton;
import ru.runa.wfe.service.delegate.Delegates;

@org.tldgen.annotations.Tag(bodyContent = BodyContent.EMPTY, name = "createDigitalSignatureForm")
public class CreateDigitalSignatureFormTag extends TitledFormTag {
    private static final long serialVersionUID = 8049519129092850184L;
    private Long identifiableId;

    @Override
    protected String getTitle() {
        return MessagesExecutor.TITLE_CREATE_DIGITAL_SIGNATURE.message(pageContext);
    }

    @Attribute(required = true)
    public void setIdentifiableId(Long identifiableId) {
        this.identifiableId = identifiableId;
    }

    public Long getIdentifiableId() {
        return identifiableId;
    }

    @Override
    public void fillFormElement(TD tdFormElement) {
        Delegates.getAuthorizationService().checkAllowed(getUser(), Permission.CREATE_DIGITAL_SIGNATURE, SecuredSingleton.SYSTEM);
        DigitalSignatureTableBuilder builder = new DigitalSignatureTableBuilder(identifiableId, pageContext);
        tdFormElement.addElement(builder.buildTable());
        tdFormElement.addElement(createHiddenUserId());
    }

    private Input createHiddenUserId() {
        return new Input(Input.HIDDEN, CreateDigitalSignatureForm.EXECUTOR_ID_INPUT_NAME, identifiableId.toString());
    }

    @Override
    public String getSubmitButtonName() {
        return MessagesCommon.BUTTON_CREATE.message(pageContext);
    }

    @Override
    public String getAction() {
        return CreateDigitalSignatureAction.ACTION_PATH;
    }

}
