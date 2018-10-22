package ru.runa.wf.web.tag;

import org.apache.ecs.Entities;
import org.apache.ecs.StringElement;
import org.apache.ecs.html.Form;
import org.apache.ecs.html.Input;
import org.apache.ecs.html.Label;
import org.apache.ecs.html.TD;
import org.apache.ecs.html.Table;
import org.tldgen.annotations.BodyContent;

import ru.runa.common.WebResources;
import ru.runa.common.web.HTMLUtils;
import ru.runa.common.web.MessagesOther;
import ru.runa.common.web.Resources;
import ru.runa.common.web.form.FileForm;
import ru.runa.common.web.tag.TitledFormTag;
import ru.runa.wf.web.action.ImportDataFileAction;

@org.tldgen.annotations.Tag(bodyContent = BodyContent.JSP, name = "importDataFile")
public class ImportDataFileTag extends TitledFormTag {
    private static final long serialVersionUID = 1L;

    @Override
    public boolean isVisible() {
        return WebResources.isImportExportEnabled();
    }

    @Override
    protected void fillFormElement(TD tdFormElement) {
        getForm().setEncType(Form.ENC_UPLOAD);
        Table table = new Table();
        table.setClass(Resources.CLASS_LIST_TABLE);
        createAddDataRow(table);
        clearPasswordRow(table);
        clearPasswordForDataSourcesRow(table);
        Input fileInput = HTMLUtils.createInput(Input.FILE, FileForm.FILE_INPUT_NAME, "", true, true);
        table.addElement(HTMLUtils.createRow(MessagesOther.TITLE_DATAFILE.message(pageContext), fileInput));
        tdFormElement.addElement(table);
    }

    private void createAddDataRow(Table table) {
        TD td = new TD();
        Input uploadInput = new Input(Input.RADIO, ImportDataFileAction.UPLOAD_PARAM, ImportDataFileAction.UPLOAD_ONLY);
        uploadInput.setID(ImportDataFileAction.UPLOAD_ONLY);
        uploadInput.setChecked(true);
        td.addElement(uploadInput);
        Label label = new Label(ImportDataFileAction.UPLOAD_ONLY);
        label.addElement(new StringElement(MessagesOther.LABEL_DATAFILE_UPLOADONLY.message(pageContext)));
        td.addElement(label);
        td.addElement(Entities.NBSP);
        Input uploadAndClearInput = new Input(Input.RADIO, ImportDataFileAction.UPLOAD_PARAM, ImportDataFileAction.CLEAR_BEFORE_UPLOAD);
        uploadAndClearInput.setID(ImportDataFileAction.CLEAR_BEFORE_UPLOAD);
        td.addElement(uploadAndClearInput);
        label = new Label(ImportDataFileAction.CLEAR_BEFORE_UPLOAD);
        label.addElement(new StringElement(MessagesOther.LABEL_DATAFILE_CLEARBEFOREUPLOAD.message(pageContext)));
        td.addElement(label);
        table.addElement(HTMLUtils.createRow(MessagesOther.TITLE_DATAFILE_ACTION.message(pageContext), td));
    }

    private void clearPasswordRow(Table table) {
        TD td = new TD();
        Input setPasswordInput = new Input(Input.RADIO, ImportDataFileAction.PASSWORD_PARAM, ImportDataFileAction.SET_PASSWORD);
        setPasswordInput.setID(ImportDataFileAction.SET_PASSWORD);
        setPasswordInput.setChecked(true);
        td.addElement(setPasswordInput);
        Label label = new Label(ImportDataFileAction.SET_PASSWORD);
        label.addElement(new StringElement(MessagesOther.LABEL_DATAFILE_SET_PASSWORD.message(pageContext)));
        td.addElement(label);
        td.addElement(Entities.NBSP);
        Input clearPasswordInput = new Input(Input.RADIO, ImportDataFileAction.PASSWORD_PARAM, ImportDataFileAction.CLEAR_PASSWORD);
        clearPasswordInput.setID(ImportDataFileAction.CLEAR_PASSWORD);
        td.addElement(clearPasswordInput);
        label = new Label(ImportDataFileAction.CLEAR_PASSWORD);
        label.addElement(new StringElement(MessagesOther.LABEL_DATAFILE_CLEAR_PASSWORD.message(pageContext)));
        td.addElement(label);
        table.addElement(HTMLUtils.createRow(MessagesOther.TITLE_DATAFILE_ACTION_PASSWORD.message(pageContext), td));

        TD passInputTd = new TD();
        Input passwordText = new Input(Input.TEXT, ImportDataFileAction.PASSWORD_VALUE_PARAM, "123");
        passwordText.setID(ImportDataFileAction.PASSWORD_VALUE_PARAM);
        passwordText.setStyle("width: 300px;");
        passInputTd.addElement(passwordText);
        table.addElement(HTMLUtils.createRow(MessagesOther.TITLE_DATAFILE_PASSWORD.message(pageContext), passInputTd));
    }

    private void clearPasswordForDataSourcesRow(Table table) {
        TD td = new TD();
        Input setPasswordInput = new Input(Input.RADIO, ImportDataFileAction.PASSWORD_DATA_SOURCE_PARAM, ImportDataFileAction.SET_PASSWORD);
        setPasswordInput.setID(ImportDataFileAction.SET_PASSWORD);
        setPasswordInput.setChecked(true);
        td.addElement(setPasswordInput);
        Label label = new Label(ImportDataFileAction.SET_PASSWORD);
        label.addElement(new StringElement(MessagesOther.LABEL_DATAFILE_SET_PASSWORD_DATA_SOURCE.message(pageContext)));
        td.addElement(label);
        td.addElement(Entities.NBSP);
        Input clearPasswordInput = new Input(Input.RADIO, ImportDataFileAction.PASSWORD_DATA_SOURCE_PARAM, ImportDataFileAction.CLEAR_PASSWORD);
        clearPasswordInput.setID(ImportDataFileAction.CLEAR_PASSWORD);
        td.addElement(clearPasswordInput);
        label = new Label(ImportDataFileAction.CLEAR_PASSWORD);
        label.addElement(new StringElement(MessagesOther.LABEL_DATAFILE_CLEAR_PASSWORD_DATA_SOURCE.message(pageContext)));
        td.addElement(label);
        table.addElement(HTMLUtils.createRow(MessagesOther.TITLE_DATAFILE_ACTION_PASSWORD_DATA_SOURCE.message(pageContext), td));
        TD passInputTd = new TD();
        Input passwordText = new Input(Input.TEXT, ImportDataFileAction.PASSWORD_VALUE_DATA_SOURCE_PARAM, "321");
        passwordText.setID(ImportDataFileAction.PASSWORD_VALUE_DATA_SOURCE_PARAM);
        passwordText.setStyle("width: 300px;");
        passInputTd.addElement(passwordText);
        table.addElement(HTMLUtils.createRow(MessagesOther.TITLE_DATAFILE_PASSWORD_DATA_SOURCE.message(pageContext), passInputTd));
    }

    @Override
    protected String getTitle() {
        return MessagesOther.TITLE_IMPORT_DATAFILE.message(pageContext);
    }

    @Override
    public String getAction() {
        return ImportDataFileAction.ACTION_PATH;
    }

    @Override
    protected String getSubmitButtonName() {
        return MessagesOther.TITLE_IMPORT_DATAFILE.message(pageContext);
    }
}
