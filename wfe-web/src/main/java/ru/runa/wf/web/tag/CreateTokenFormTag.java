package ru.runa.wf.web.tag;

import org.apache.ecs.Entities;
import org.apache.ecs.html.Input;
import org.apache.ecs.html.TD;
import org.apache.ecs.html.TR;
import org.apache.ecs.html.Table;
import org.tldgen.annotations.Attribute;
import org.tldgen.annotations.BodyContent;
import ru.runa.af.web.MessagesExecutor;
import ru.runa.common.web.Messages;
import ru.runa.common.web.MessagesCommon;
import ru.runa.common.web.Resources;
import ru.runa.common.web.form.IdForm;
import ru.runa.common.web.tag.TitledFormTag;
import ru.runa.wf.web.MessagesProcesses;
import ru.runa.wf.web.action.CreateTokenAction;

@org.tldgen.annotations.Tag(bodyContent = BodyContent.EMPTY, name = "createTokenForm")
public class CreateTokenFormTag extends TitledFormTag {
    private static final long serialVersionUID = 1L;

    private Long processId;

    @Attribute
    public void setProcessId(Long processId) {
        this.processId = processId;
    }

    @Override
    protected void fillFormElement(TD tdFormElement) {
        tdFormElement.addElement(new Input(Input.HIDDEN, IdForm.ID_INPUT_NAME, processId.toString()));

        Table table = new Table();
        table.setClass(Resources.CLASS_LIST_TABLE);
        table.setStyle("width: 400px;");

        TR nodeTr = new TR();
        TD nodeLabelTd = new TD(Messages.getMessage("batch_presentation.token.nodeId", pageContext));
        nodeLabelTd.setClass(Resources.CLASS_LIST_TABLE_TD);
        nodeTr.addElement(nodeLabelTd);
        TD nodeTd = new TD();
        nodeTd.setClass(Resources.CLASS_LIST_TABLE_TD);
        Input nodeInput = new Input(Input.TEXT, "name");
        nodeInput.setID("createTokenNodeId");
        nodeInput.setStyle("width: 200px");
        nodeTd.addElement(nodeInput);
        nodeTd.addElement(Entities.NBSP);
        Input nodeSelectButton = new Input(Input.BUTTON);

        nodeSelectButton.setValue(MessagesExecutor.LABEL_SELECT.message(pageContext));
        nodeSelectButton
                .setOnClick("currentNodeIdInput = \"createTokenNodeId\";showImageDialog('/wfe/process_graph_component.do?id=" + processId
                        + "&graphMode=Select')");
        nodeTd.addElement(nodeSelectButton);
        nodeTr.addElement(nodeTd);
        table.addElement(nodeTr);

        tdFormElement.addElement(table);
    }

    @Override
    protected String getTitle() {
        return MessagesProcesses.TITLE_CREATE_TOKEN.message(pageContext);
    }

    @Override
    protected String getSubmitButtonName() {
        return MessagesCommon.BUTTON_CREATE.message(pageContext);
    }

    @Override
    public String getAction() {
        return CreateTokenAction.ACTION_PATH;
    }

}
