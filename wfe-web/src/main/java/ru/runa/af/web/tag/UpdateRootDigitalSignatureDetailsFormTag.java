package ru.runa.af.web.tag;

import org.apache.ecs.ConcreteElement;
import org.apache.ecs.Entities;
import org.apache.ecs.html.Form;
import org.apache.ecs.html.Input;
import org.apache.ecs.html.TD;
import org.apache.ecs.html.TR;
import org.apache.ecs.html.Table;
import ru.runa.af.web.MessagesExecutor;
import ru.runa.af.web.action.RemoveRootDigitalSignatureAction;
import ru.runa.af.web.action.UpdateRootDigitalSignatureDetailsAction;
import ru.runa.af.web.html.RootDigitalSignatureTableBuilder;
import ru.runa.common.web.Commons;
import ru.runa.common.web.ConfirmationPopupHelper;
import ru.runa.common.web.Resources;
import ru.runa.wfe.commons.web.PortletUrlType;
import ru.runa.wfe.digitalsignature.DigitalSignature;
import ru.runa.wfe.service.delegate.Delegates;

public class UpdateRootDigitalSignatureDetailsFormTag extends UpdateDigitalSignatureDetailsFormTag{
    public Long getIdentifiableId() {
        return getUser().getActor().getId();
    }
    @Override
    public void fillFormData(TD tdFormElement) {
        DigitalSignature digitalSignature = Delegates.getDigitalSignatureService().getRootDigitalSignature(getUser());
        if (digitalSignature == null) {
            return;
        }
        boolean isInputDisabled = !isSubmitButtonEnabled();
        RootDigitalSignatureTableBuilder builder = new RootDigitalSignatureTableBuilder (digitalSignature, isInputDisabled, pageContext);
        tdFormElement.addElement(builder.buildTable());
    }

    @Override
    protected String getCancelButtonAction() {
        return RemoveRootDigitalSignatureAction.ACTION_PATH;
    }
    @Override
    protected String getTitle() {
        return MessagesExecutor.TITLE_ROOT_DIGITAL_SIGNATURE_DETAILS.message(pageContext);
    }

    @Override
    public String getAction() {
        return UpdateRootDigitalSignatureDetailsAction.ACTION_PATH;
    }
    protected DigitalSignature getDigitalSignature() {
        return Delegates.getDigitalSignatureService().getRootDigitalSignature(getUser());
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
            deleteButton.addAttribute(ATTRIBUTE_ONCLICK, "window.location='" + attr +  "'");
            td.addElement(Entities.NBSP);
            td.addElement(deleteButton);
        }
        return form;
    }
}
