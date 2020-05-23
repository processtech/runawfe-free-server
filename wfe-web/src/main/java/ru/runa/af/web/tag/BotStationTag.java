package ru.runa.af.web.tag;

import org.apache.ecs.html.Input;
import org.apache.ecs.html.TD;
import org.apache.ecs.html.Table;
import org.tldgen.annotations.Attribute;
import org.tldgen.annotations.BodyContent;
import ru.runa.af.web.action.UpdateBotStationAction;
import ru.runa.af.web.form.BotStationForm;
import ru.runa.af.web.html.BotStationTableBuilder;
import ru.runa.common.web.MessagesCommon;
import ru.runa.common.web.tag.TitledFormTag;
import ru.runa.wf.web.MessagesBot;
import ru.runa.wfe.bot.BotStation;
import ru.runa.wfe.security.Permission;
import ru.runa.wfe.security.SecuredSingleton;
import ru.runa.wfe.service.delegate.Delegates;

@org.tldgen.annotations.Tag(bodyContent = BodyContent.JSP, name = "botStationTag")
public class BotStationTag extends TitledFormTag {
    private static final long serialVersionUID = 1L;

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
        BotStation botStation = Delegates.getBotService().getBotStation(botStationId);
        Input hiddenBotStationID = new Input(Input.HIDDEN, BotStationForm.BOT_STATION_ID, String.valueOf(botStationId));
        tdFormElement.addElement(hiddenBotStationID);
        Table table = BotStationTableBuilder.createBotStationDetailsTable(pageContext, botStation.getName(), botStation.getAddress());
        tdFormElement.addElement(table);
    }

    @Override
    protected String getTitle() {
        return MessagesBot.TITLE_BOT_STATION_DETAILS.message(pageContext);
    }

    @Override
    protected String getSubmitButtonName() {
        return MessagesCommon.BUTTON_APPLY.message(pageContext);
    }

    @Override
    public String getButtonAlignment() {
        return "right";
    }

    @Override
    public String getAction() {
        return UpdateBotStationAction.UPDATE_BOT_STATION_ACTION_PATH;
    }

    @Override
    public boolean isSubmitButtonEnabled() {
        return Delegates.getAuthorizationService().isAllowed(getUser(), Permission.UPDATE, SecuredSingleton.BOTSTATIONS);
    }
}
