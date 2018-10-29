package ru.runa.af.web.tag;

import org.apache.ecs.html.Input;
import org.apache.ecs.html.TD;
import org.apache.ecs.html.Table;
import org.tldgen.annotations.Attribute;
import org.tldgen.annotations.BodyContent;

import ru.runa.af.web.MessagesExecutor;
import ru.runa.af.web.action.CreateRelationPairAction;
import ru.runa.af.web.form.RelationPairForm;
import ru.runa.common.web.HTMLUtils;
import ru.runa.common.web.Messages;
import ru.runa.common.web.MessagesCommon;
import ru.runa.common.web.Resources;
import ru.runa.common.web.tag.TitledFormTag;

@org.tldgen.annotations.Tag(bodyContent = BodyContent.EMPTY, name = "createRelationPairForm")
public class CreateRelationPairFormTag extends TitledFormTag {
    private static final long serialVersionUID = 1L;
    private Long relationId;

    @Override
    public String getAction() {
        return CreateRelationPairAction.ACTION_PATH;
    }

    @Override
    protected String getTitle() {
        return MessagesExecutor.TITLE_CREATE_RELATION_PAIR.message(pageContext);
    }

    @Override
    protected String getSubmitButtonName() {
        return MessagesCommon.BUTTON_CREATE.message(pageContext);
    }

    public Long getRelationId() {
        return relationId;
    }

    @Attribute(required = true, rtexprvalue = true)
    public void setRelationId(Long relationId) {
        this.relationId = relationId;
    }

    @Override
    protected void fillFormElement(TD tdFormElement) {
        Table table = new Table();
        table.setClass(Resources.CLASS_LIST_TABLE);
        ActorSelect actorSelectFrom = new ActorSelect(getUser(), RelationPairForm.EXECUTOR_FROM, null, false);
        table.addElement(HTMLUtils.createSelectRow(MessagesExecutor.LABEL_RELATION_FROM.message(pageContext), actorSelectFrom, true));
        ActorSelect actorSelectTo = new ActorSelect(getUser(), RelationPairForm.EXECUTOR_TO, null, false);
        table.addElement(HTMLUtils.createSelectRow(MessagesExecutor.LABEL_RELATION_TO.message(pageContext), actorSelectTo, true));
        tdFormElement.addElement(table);
        tdFormElement.addElement(new Input(Input.HIDDEN, RelationPairForm.RELATION_ID, String.valueOf(relationId)));
    }
}
