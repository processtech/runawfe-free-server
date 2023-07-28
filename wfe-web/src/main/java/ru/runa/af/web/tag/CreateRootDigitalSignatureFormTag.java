package ru.runa.af.web.tag;

import org.apache.ecs.html.TD;
import ru.runa.af.web.MessagesExecutor;
import ru.runa.af.web.action.CreateRootDigitalSignatureAction;
import ru.runa.af.web.html.RootDigitalSignatureTableBuilder;
import ru.runa.common.web.MessagesCommon;
import ru.runa.common.web.tag.TitledFormTag;
import ru.runa.wfe.security.Permission;
import ru.runa.wfe.security.SecuredSingleton;
import ru.runa.wfe.service.delegate.Delegates;

public class CreateRootDigitalSignatureFormTag extends TitledFormTag {
    private static final long serialVersionUID = -4626770497970478977L;
    @Override
    protected String getTitle() {
        return MessagesExecutor.TITLE_CREATE_ROOT_DIGITAL_SIGNATURE.message(pageContext);
    }
    @Override
    public void fillFormElement(TD tdFormElement) {
        Delegates.getAuthorizationService().checkAllowed(getUser(), Permission.CREATE_DIGITAL_SIGNATURE, SecuredSingleton.SYSTEM);
        RootDigitalSignatureTableBuilder builder = new RootDigitalSignatureTableBuilder(pageContext);
        tdFormElement.addElement(builder.buildTable());
    }
    @Override
    public String getSubmitButtonName() {
        return MessagesCommon.BUTTON_CREATE.message(pageContext);
    }
    @Override
    public String getAction() {
        return CreateRootDigitalSignatureAction.ACTION_PATH;
    }
}
