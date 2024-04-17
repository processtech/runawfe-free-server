package ru.runa.wf.web.tag;

import com.google.common.base.Strings;
import java.util.Arrays;
import org.apache.ecs.html.Form;
import org.apache.ecs.html.Input;
import org.apache.ecs.html.Select;
import org.apache.ecs.html.TD;
import org.apache.ecs.html.TR;
import org.apache.ecs.html.Table;
import org.tldgen.annotations.BodyContent;
import ru.runa.common.web.Messages;
import ru.runa.common.web.Resources;
import ru.runa.common.web.tag.TitledFormTag;
import ru.runa.wf.web.MessagesProcesses;
import ru.runa.wf.web.action.ShowDefinitionHistoryDiffAction;
import ru.runa.wfe.InternalApplicationException;
import ru.runa.wfe.definition.DefinitionClassPresentation;
import ru.runa.wfe.lang.ParsedProcessDefinition;
import ru.runa.wfe.presentation.ClassPresentationType;
import ru.runa.wfe.service.delegate.Delegates;

@org.tldgen.annotations.Tag(bodyContent = BodyContent.JSP, name = "processDefinitionFileAnnotationForm")
public class ProcessDefinitionFileAnnotationFormTag extends TitledFormTag {
    private static final long serialVersionUID = 1L;
    public static final String ACTION_PATH = "/process_definition_file_annotation";
    public static final String PROCESS_DEFINITION_ID_PARAMETER = "id";
    public static final String FILE_NAME_PARAMETER = "fileName";
    public static final String LIMIT_PARAMETER = "limit";

    @Override
    protected void fillFormElement(TD content) {
        String processDefinitionIdAsString = pageContext.getRequest().getParameter(PROCESS_DEFINITION_ID_PARAMETER);
        if (processDefinitionIdAsString == null) {
            throw new InternalApplicationException("No " + PROCESS_DEFINITION_ID_PARAMETER + " parameter has been provided");
        }
        content.addElement(new Input(Input.HIDDEN, PROCESS_DEFINITION_ID_PARAMETER, processDefinitionIdAsString));
        Long processDefinitionId = Long.parseLong(processDefinitionIdAsString);
        ParsedProcessDefinition processDefinition = Delegates.getDefinitionService().getParsedProcessDefinition(getUser(), processDefinitionId);
        String processDefinitionName = processDefinition.getName();
        String fileName = pageContext.getRequest().getParameter(FILE_NAME_PARAMETER);
        String limitAsString = pageContext.getRequest().getParameter(LIMIT_PARAMETER);
        Integer limit = Strings.isNullOrEmpty(limitAsString) ? 0 : Integer.parseInt(limitAsString);
        if (fileName == null) {
            limit = 10;
        }

        Table table = new Table();
        content.addElement(table);
        table.setClass(Resources.CLASS_LIST_TABLE);
        table.setStyle("width: 85vw;");

        TR nameTR = new TR();
        table.addElement(nameTR);
        nameTR.addElement(new TD(Messages.getMessage(ClassPresentationType.DEFINITION, DefinitionClassPresentation.NAME, pageContext))
                .setClass(Resources.CLASS_LIST_TABLE_TD));
        TD nameTD = new TD();
        nameTD.addElement(processDefinitionName);
        nameTR.addElement(nameTD.setClass(Resources.CLASS_LIST_TABLE_TD));

        TR versionTR = new TR();
        table.addElement(versionTR);
        versionTR.addElement(new TD(Messages.getMessage(ClassPresentationType.DEFINITION, DefinitionClassPresentation.VERSION, pageContext))
                .setClass(Resources.CLASS_LIST_TABLE_TD));
        TD versionTD = new TD();
        versionTD.addElement(processDefinition.getVersion().toString());
        versionTR.addElement(versionTD.setClass(Resources.CLASS_LIST_TABLE_TD));

        TR fileTR = new TR();
        table.addElement(fileTR);
        fileTR.addElement(new TD(MessagesProcesses.LABEL_FILE.message(pageContext)).setClass(Resources.CLASS_LIST_TABLE_TD));
        TD fileTD = new TD();
        Select chooseFileCombo = new Select(FILE_NAME_PARAMETER);
        fileTD.addElement(chooseFileCombo);
        String[] comboOptions = processDefinition.getProcessFiles().keySet().stream().filter(ShowDefinitionHistoryDiffAction::isTextFile).sorted()
                .toArray(String[]::new);
        chooseFileCombo.addElement(comboOptions);
        if (fileName != null) {
            chooseFileCombo.selectOption(Arrays.binarySearch(comboOptions, fileName));
        }
        fileTR.addElement(fileTD.setClass(Resources.CLASS_LIST_TABLE_TD));

        TR limitTR = new TR();
        table.addElement(limitTR);
        limitTR.addElement(new TD(MessagesProcesses.LABEL_VERSIONS_LIMIT.message(pageContext)).setClass(Resources.CLASS_LIST_TABLE_TD));
        TD limitTD = new TD();
        limitTD.addElement(new Input(Input.TEXT, LIMIT_PARAMETER, String.valueOf(limit)));
        limitTR.addElement(limitTD.setClass(Resources.CLASS_LIST_TABLE_TD));
    }

    @Override
    protected String getTitle() {
        return MessagesProcesses.TITLE_PROCESS_DEFINITION_FILE_ANNOTATION.message(pageContext);
    }

    @Override
    public String getAction() {
        return ACTION_PATH;
    }

    @Override
    public String getMethod() {
        return Form.GET;
    }

    @Override
    protected String getSubmitButtonName() {
        return MessagesProcesses.BUTTON_ANNOTATE_CHANGES.message(pageContext);
    }

}
