package ru.runa.af.web.tag;

import org.apache.ecs.html.Input;
import org.apache.ecs.html.TD;
import org.apache.ecs.html.Table;
import org.tldgen.annotations.Attribute;
import org.tldgen.annotations.BodyContent;

import ru.runa.af.web.action.CreateBotAction;
import ru.runa.af.web.form.BotForm;
import ru.runa.af.web.html.BotTableBuilder;
import ru.runa.common.web.tag.TitledFormTag;
import ru.runa.wf.web.MessagesBot;

/**
 * @author: petrmikheev
 */
@org.tldgen.annotations.Tag(bodyContent = BodyContent.JSP, name = "addBotTag")
public class AddBotTag extends TitledFormTag {
    private static final long serialVersionUID = 1920713038009470026L;

    private Long botStationId;

    @Attribute(required = false, rtexprvalue = true)
    public void setBotStationId(Long botStationId) {
        this.botStationId = botStationId;
    }

    public Long getBotStationId() {
        return botStationId;
    }

    @Override
    protected void fillFormElement(TD tdFormElement) {
        Input hiddenBotStationID = new Input(Input.HIDDEN, BotForm.BOT_STATION_ID, String.valueOf(botStationId));
        tdFormElement.addElement(hiddenBotStationID);
        Table table = BotTableBuilder.buildBotDetailsTable(getUser(), pageContext, null);
        tdFormElement.addElement(table);
    }

    @Override
    protected String getTitle() {
        return MessagesBot.TITLE_ADD_BOT.message(pageContext);
    }

    @Override
    public String getButtonAlignment() {
        return "right";
    }

    @Override
    protected String getSubmitButtonName() {
        return MessagesBot.BUTTON_ADD_BOT.message(pageContext);
    }

    @Override
    public String getAction() {
        return CreateBotAction.CREATE_BOT_ACTION_PATH;
    }
}
