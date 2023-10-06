package ru.runa.common.web.tag;

import com.google.common.base.Strings;
import java.util.List;
import java.util.Map;
import org.apache.ecs.ConcreteElement;
import org.apache.ecs.Entities;
import org.apache.ecs.StringElement;
import org.apache.ecs.html.Form;
import org.apache.ecs.html.Input;
import org.apache.ecs.html.TD;
import org.apache.ecs.html.TR;
import org.apache.ecs.html.Table;
import org.tldgen.annotations.Attribute;
import ru.runa.common.web.Commons;
import ru.runa.common.web.ConfirmationPopupHelper;
import ru.runa.common.web.MessagesCommon;
import ru.runa.common.web.Resources;
import ru.runa.wf.web.MessagesProcesses;
import ru.runa.wfe.commons.web.PortletUrlType;

/**
 * Provides attibutes action and method for sub classes. Created on 19.08.2004
 */
abstract public class FormTag extends VisibleTag {

    public static final String SUBMIT_BUTTON_NAME = "submitButton";
    public static final String CANCEL_BUTTON_NAME = "cancelButton";
    public static final String MULTIPLE_SUBMIT_BUTTONS = "multipleSubmit";
    public static final String ATTRIBUTE_COLOR = "color";
    public static final String ATTRIBUTE_NAME = "name";
    public static final String ATTRIBUTE_ONCLICK = "onclick";
    public static final String ATTRIBUTE_STYLE = "style";
    public static final String ATTRIBUTE_TYPE = "type";
    private static final long serialVersionUID = 1L;
    private String action;

    private String method = Form.POST;

    protected String buttonAlignment;
    protected Form form;

    public String getAction() {
        return action;
    }

    @Attribute
    public void setAction(String action) {
        this.action = action;
    }

    public String getMethod() {
        return method;
    }

    @Attribute(required = false, rtexprvalue = false)
    public void setMethod(String string) {
        method = string;
    }

    public String getButtonAlignment() {
        return buttonAlignment;
    }

    @Attribute(required = false, rtexprvalue = true)
    public void setButtonAlignment(String buttonAlignment) {
        this.buttonAlignment = buttonAlignment;
    }

    /**
     * In this method descendants fill the form.
     *
     * @param tdFormElement
     * @ if any exception occurred
     */
    abstract protected void fillFormElement(final TD tdFormElement);

    /**
     * @return returns true if form button must be displayed
     */
    protected boolean isSubmitButtonVisible() {
        return true;
    }

    /**
     * @return returns true if form button must be displayed
     */
    protected boolean isSubmitButtonEnabled() {
        return true;
    }

    protected String getSubmitButtonName() {
        return MessagesProcesses.BUTTON_FORM.message(pageContext);
    }

    protected Map<String, Object> getSubmitButtonParam() {
        return null;
    }

    protected boolean isMultipleSubmit() {
        return false;
    }

    protected List<Map<String, String>> getSubmitButtonsData() {
        return null;
    }

    protected Form getForm() {
        return form;
    }

    public boolean isConfirmationPopupEnabled() {
        return ConfirmationPopupHelper.getInstance().isEnabled(getConfirmationPopupParameter());
    }

    protected String getConfirmationPopupParameter() {
        return "";
    }

    protected boolean isCancelButtonEnabled() {
        return false;
    }

    protected String getCancelButtonAction() {
        return "";
    }

    protected String getCancelButtonName() {
        return MessagesCommon.BUTTON_CANCEL.message(pageContext);
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
            if (isMultipleSubmit()) {
                td.addElement(new Input(Input.HIDDEN, MULTIPLE_SUBMIT_BUTTONS, "true"));
                for (Map<String, String> buttonData : getSubmitButtonsData()) {
                    String type = buttonData.remove(ATTRIBUTE_TYPE);
                    if (type == null) {
                        type = Input.SUBMIT;
                    }
                    Input submitButton = new Input(type, SUBMIT_BUTTON_NAME, buttonData.remove(ATTRIBUTE_NAME));
                    String color = buttonData.remove(ATTRIBUTE_COLOR);
                    submitButton.setClass(Resources.CLASS_BUTTON + (Strings.isNullOrEmpty(color) ? "" : "-" + color));
                    buttonData.forEach(submitButton::addAttribute);
                    try {
                        if (!isSubmitButtonEnabled()) {
                            submitButton.setDisabled(true);
                        }
                    } catch (Exception e) {
                        log.debug("isSubmitButtonEnabled", e);
                        submitButton.setDisabled(true);
                    }
                    td.addElement(submitButton);
                    td.addElement(Entities.NBSP);
                }
            } else {
                Input submitButton = new Input(Input.SUBMIT, SUBMIT_BUTTON_NAME, getSubmitButtonName());
                submitButton.setClass(Resources.CLASS_BUTTON);
                if (isConfirmationPopupEnabled()) {
                    submitButton.addAttribute(ATTRIBUTE_ONCLICK,
                            ConfirmationPopupHelper.getInstance().getConfirmationPopupCodeHTML(getConfirmationPopupParameter(), pageContext));
                }
                if (!isSubmitButtonEnabled()) {
                    submitButton.setDisabled(true);
                }
                td.addElement(submitButton);
            }
            if (isCancelButtonEnabled()) {
                Input cancelButton = new Input(Input.BUTTON, CANCEL_BUTTON_NAME, getCancelButtonName());
                cancelButton.setClass(Resources.CLASS_BUTTON);
                cancelButton.addAttribute(ATTRIBUTE_ONCLICK, "window.location='" + getCancelButtonAction() + "'");
                td.addElement(Entities.NBSP);
                td.addElement(cancelButton);
            }
        }
        return form;
    }

    @Override
    protected ConcreteElement getStartElement() {
        return new StringElement();
    }
}