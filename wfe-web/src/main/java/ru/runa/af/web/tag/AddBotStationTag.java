package ru.runa.af.web.tag;

import org.apache.ecs.html.TD;
import org.apache.ecs.html.Table;
import org.tldgen.annotations.BodyContent;

import ru.runa.af.web.action.CreateBotStationAction;
import ru.runa.af.web.html.BotStationTableBuilder;
import ru.runa.common.web.tag.TitledFormTag;
import ru.runa.wf.web.MessagesBot;

/**
 * User: stanley Date: 08.06.2008 Time: 13:32:44
 */
@org.tldgen.annotations.Tag(bodyContent = BodyContent.JSP, name = "addBotStationTag")
public class AddBotStationTag extends TitledFormTag {
    private static final long serialVersionUID = 1920713038009470026L;

    @Override
    protected void fillFormElement(TD tdFormElement) {
        Table table = BotStationTableBuilder.createBotStationDetailsTable(pageContext, "", "");
        tdFormElement.addElement(table);
    }

    @Override
    protected String getTitle() {
        return MessagesBot.TITLE_ADD_BOT_STATION.message(pageContext);
    }

    @Override
    public String getButtonAlignment() {
        return "right";
    }

    @Override
    protected String getSubmitButtonName() {
        return MessagesBot.BUTTON_ADD_BOT_STATION.message(pageContext);
    }

    @Override
    public String getAction() {
        return CreateBotStationAction.CREATE_BOT_STATION_ACTION_PATH;
    }
}
