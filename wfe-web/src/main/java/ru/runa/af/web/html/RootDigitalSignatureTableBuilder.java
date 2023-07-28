package ru.runa.af.web.html;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import javax.servlet.jsp.PageContext;
import org.apache.ecs.html.Input;
import org.apache.ecs.html.Table;
import ru.runa.af.web.MessagesExecutor;
import ru.runa.af.web.form.UpdateRootDigitalSignatureDetailsForm;
import ru.runa.common.web.HTMLUtils;
import ru.runa.wfe.digitalsignature.DigitalSignature;

public class RootDigitalSignatureTableBuilder{
    private final DigitalSignature digitalSignature;
    private final PageContext pageContext;
    private final boolean enabled;

    public RootDigitalSignatureTableBuilder(DigitalSignature digitalSignature, boolean areInputsDisabled, PageContext pageContext) {
        this.digitalSignature = digitalSignature;
        this.pageContext = pageContext;
        this.enabled = !areInputsDisabled;
    }
    public RootDigitalSignatureTableBuilder(PageContext pageContext) {
        this.digitalSignature = new DigitalSignature();
        this.pageContext = pageContext;
        this.enabled = true;
    }

    public Table buildTable() {
        Table table = new Table();
        table.setClass(ru.runa.common.web.Resources.CLASS_LIST_TABLE);
        Input commonNameInput = HTMLUtils.createInput(UpdateRootDigitalSignatureDetailsForm.COMMON_NAME_INPUT_NAME, digitalSignature.getCommonName(), enabled, true);
        table.addElement(HTMLUtils.createRow(MessagesExecutor.DIGITAL_SIGNATURE_ROOT_COMMON_NAME.message(pageContext), commonNameInput));

        Input emailInput = HTMLUtils.createInput(UpdateRootDigitalSignatureDetailsForm.EMAIL_INPUT_NAME, digitalSignature.getEmail(), enabled, false);
        table.addElement(HTMLUtils.createRow(MessagesExecutor.DIGITAL_SIGNATURE_EMAIL.message(pageContext), emailInput));

        Input departmentInput = HTMLUtils.createInput(UpdateRootDigitalSignatureDetailsForm.DEPARTMENT_INPUT_NAME, digitalSignature.getDepartment(), enabled, false);
        table.addElement(HTMLUtils.createRow(MessagesExecutor.DIGITAL_SIGNATURE_DEPARTMENT.message(pageContext), departmentInput));

        Input organizationInput = HTMLUtils.createInput(UpdateRootDigitalSignatureDetailsForm.ORGANIZATION_INPUT_NAME, digitalSignature.getOrganization(), enabled, false);
        table.addElement(HTMLUtils.createRow(MessagesExecutor.DIGITAL_SIGNATURE_ORGANISATION.message(pageContext), organizationInput));

        Input cityInput = HTMLUtils.createInput(UpdateRootDigitalSignatureDetailsForm.CITY_INPUT_NAME, digitalSignature.getCity(), enabled, false);
        table.addElement(HTMLUtils.createRow(MessagesExecutor.DIGITAL_SIGNATURE_CITY.message(pageContext), cityInput));

        Input stateInput = HTMLUtils.createInput(UpdateRootDigitalSignatureDetailsForm.STATE_INPUT_NAME, digitalSignature.getState(), enabled, false);
        table.addElement(HTMLUtils.createRow(MessagesExecutor.DIGITAL_SIGNATURE_STATE.message(pageContext), stateInput));

        Input countryInput = HTMLUtils.createInput(UpdateRootDigitalSignatureDetailsForm.COUNTRY_INPUT_NAME, digitalSignature.getCountry(), enabled, false);
        table.addElement(HTMLUtils.createRow(MessagesExecutor.DIGITAL_SIGNATURE_COUNTRY.message(pageContext), countryInput));

        Input validityInput = HTMLUtils.createInput(UpdateRootDigitalSignatureDetailsForm.VALIDITY_INPUT_NAME, digitalSignature.getValidityInMonth().toString(), enabled, false);
        table.addElement(HTMLUtils.createRow(MessagesExecutor.DIGITAL_SIGNATURE_VALIDITY_IN_MONTH.message(pageContext), validityInput));

        DateFormat dateFormat = new SimpleDateFormat("dd-MM-yyyy");
        Input dateOfIssueInput = HTMLUtils.createInput(UpdateRootDigitalSignatureDetailsForm.DATE_OF_ISSUE_OUTPUT_NAME, dateFormat.format(digitalSignature.getDateOfIssue()), false, false);
        table.addElement(HTMLUtils.createRow(MessagesExecutor.DIGITAL_SIGNATURE_DATE_OF_ISSUE.message(pageContext), dateOfIssueInput));

        Input expiryPeriodInput = HTMLUtils.createInput(UpdateRootDigitalSignatureDetailsForm.DATE_OF_EXPIRY_OUTPUT_NAME, dateFormat.format(digitalSignature.getDateOfExpiry()), false, false);
        table.addElement(HTMLUtils.createRow(MessagesExecutor.DIGITAL_SIGNATURE_DATE_OF_EXPIRY.message(pageContext), expiryPeriodInput));

        return table;
    }
}
