package ru.runa.af.web.tag;

import org.apache.ecs.ConcreteElement;
import org.apache.ecs.Entities;
import org.apache.ecs.html.Form;
import org.apache.ecs.html.Input;
import org.apache.ecs.html.TD;
import org.apache.ecs.html.TR;
import org.apache.ecs.html.Table;
import org.tldgen.annotations.Attribute;
import org.tldgen.annotations.BodyContent;
import ru.runa.af.web.action.RemoveDigitalSignatureAction;
import ru.runa.af.web.action.UpdateDigitalSignatureDetailsAction;
import ru.runa.af.web.form.CreateDigitalSignatureForm;
import ru.runa.af.web.html.DigitalSignatureTableBuilder;
import ru.runa.af.web.MessagesExecutor;
import ru.runa.common.web.Commons;
import ru.runa.common.web.ConfirmationPopupHelper;
import ru.runa.common.web.MessagesCommon;
import ru.runa.common.web.Resources;
import ru.runa.common.web.tag.SecuredObjectFormTag;
import ru.runa.wfe.commons.web.PortletUrlType;
import ru.runa.wfe.security.Permission;
import ru.runa.wfe.security.SecuredObject;
import ru.runa.wfe.service.delegate.Delegates;
import ru.runa.wfe.digitalsignature.DigitalSignature;
import ru.runa.wfe.user.Executor;

@org.tldgen.annotations.Tag(bodyContent = BodyContent.JSP, name = "updateDigitalSignatureDetailsForm")
public class UpdateDigitalSignatureDetailsFormTag extends SecuredObjectFormTag {
    private Long identifiableId;

    @Attribute(required = true)
    public void setIdentifiableId(Long identifiableId) {
        this.identifiableId = identifiableId;
    }

    public Long getIdentifiableId() {
        return identifiableId;
    }

    @Override
    public void fillFormData(TD tdFormElement) {
        DigitalSignature digitalSignature = Delegates.getDigitalSignatureService().getDigitalSignature(getUser(), identifiableId);
        if (digitalSignature == null) {
            return;
        }
        boolean isInputDisabled = !isSubmitButtonEnabled();
        DigitalSignatureTableBuilder builder = new DigitalSignatureTableBuilder(digitalSignature, isInputDisabled, pageContext);
        tdFormElement.addElement(builder.buildTable());
        tdFormElement.addElement(createHiddenUserId());
    }

    private Input createHiddenUserId() {
        return new Input(Input.HIDDEN, CreateDigitalSignatureForm.EXECUTOR_ID_INPUT_NAME, identifiableId.toString());
    }

    @Override
    protected boolean isSubmitButtonVisible() {
        return getDigitalSignature() != null;
    }

    @Override
    protected Permission getSubmitPermission() {
        return null;
    }

    @Override
    protected boolean isCancelButtonEnabled() {
        return isSubmitButtonEnabled();
    }

    @Override
    protected String getCancelButtonAction() {
        return RemoveDigitalSignatureAction.ACTION_PATH;
    }

    @Override
    protected SecuredObject getSecuredObject() {
        return null;
    }

    @Override
    protected boolean isSubmitButtonEnabled() {
        if (getDigitalSignature() == null) {
            return false;
        }
        return Delegates.getAuthorizationService().isAllowed(getUser(), Permission.UPDATE, getDigitalSignature());
    }

    @Override
    public String getSubmitButtonName() {
        return MessagesCommon.BUTTON_UPDATE.message(pageContext);
    }

    @Override
    public String getCancelButtonName() {
        return MessagesCommon.BUTTON_REMOVE.message(pageContext);
    }

    @Override
    protected String getTitle() {
        return MessagesExecutor.TITLE_DIGITAL_SIGNATURE_DETAILS.message(pageContext);
    }

    @Override
    public String getAction() {
        return UpdateDigitalSignatureDetailsAction.ACTION_PATH;
    }

    protected final Executor getExecutor() {
        return Delegates.getExecutorService().getExecutor(getUser(), identifiableId);
    }

    protected final DigitalSignature getDigitalSignature() {
        return Delegates.getDigitalSignatureService().getDigitalSignature(getUser(), identifiableId);
    }

    @Override
    protected ConcreteElement getEndElement() {
        form = new Form();
        Table table = new Table();
        table.setClass(Resources.CLASS_BOX);
        form.addElement(table);
        TR formElementTR = new TR();
        table.addElement(formElementTR);
        TD formElementTD = new TD();
        formElementTD.setClass(Resources.CLASS_BOX_BODY);
        formElementTR.addElement(formElementTD);
        fillFormElement(formElementTD);
        if (getAction() != null) {
            form.setAction(Commons.getActionUrl(getAction(), getSubmitButtonParam(), pageContext, PortletUrlType.Action));
        }
        if (getMethod() != null) {
            form.setMethod(getMethod());
        }
        if (isSubmitButtonVisible()) {
            TR tr = new TR();
            table.addElement(tr);
            TD td = new TD();
            tr.addElement(td);
            td.setClass(Resources.CLASS_BOX_BODY);
            if (buttonAlignment != null) {
                td.setAlign(buttonAlignment);
            }
            Input submitButton = new Input(Input.SUBMIT, SUBMIT_BUTTON_NAME, getSubmitButtonName());
            submitButton.setClass(Resources.CLASS_BUTTON);
            submitButton.addAttribute(ATTRIBUTE_ONCLICK,
                    ConfirmationPopupHelper.getInstance().getConfirmationPopupCodeHTML(ConfirmationPopupHelper.UPDATE_DIGITAL_SIGNATURE_PARAMETER, pageContext));
            if (!isSubmitButtonEnabled()) {
                submitButton.setDisabled(true);
            }
            td.addElement(submitButton);
            Input deleteButton = new Input(Input.BUTTON, SUBMIT_BUTTON_NAME, getCancelButtonName());
            deleteButton.setClass(Resources.CLASS_BUTTON);
            String attr = Commons.getActionUrl(getCancelButtonAction(), getSubmitButtonParam(), pageContext, PortletUrlType.Action);
            deleteButton.addAttribute(ATTRIBUTE_ONCLICK, "window.location='" + attr + "?id=" + getIdentifiableId().toString() + "'");
            td.addElement(Entities.NBSP);
            td.addElement(deleteButton);
        }
        return form;
    }
}
