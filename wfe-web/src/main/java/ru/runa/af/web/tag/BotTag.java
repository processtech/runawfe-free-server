package ru.runa.af.web.tag;

import org.apache.ecs.html.Input;
import org.apache.ecs.html.TD;
import org.apache.ecs.html.Table;

import ru.runa.af.web.action.UpdateBotAction;
import ru.runa.af.web.form.BotForm;
import ru.runa.af.web.html.BotTableBuilder;
import ru.runa.common.web.Messages;
import ru.runa.common.web.tag.TitledFormTag;
import ru.runa.wfe.bot.Bot;
import ru.runa.wfe.bot.BotStation;
import ru.runa.wfe.bot.BotStationPermission;
import ru.runa.wfe.service.delegate.Delegates;

import com.google.common.base.Preconditions;

/**
 * @author petrmikheev
 * @jsp.tag name = "botTag" body-content = "JSP"
 */
public class BotTag extends TitledFormTag {
    private static final long serialVersionUID = 1L;

    private Long botId;

    public void setBotId(Long botId) {
        this.botId = botId;
    }

    /**
     * @jsp.attribute required = "false" rtexprvalue = "true"
     */
    public Long getBotId() {
        return botId;
    }

    @Override
    protected void fillFormElement(TD tdFormElement) {
        Bot bot = findBot();
        Preconditions.checkNotNull(bot);
        Input hiddenBotStationID = new Input(Input.HIDDEN, BotForm.BOT_STATION_ID, bot.getBotStation().getId().intValue());
        Input hiddenBotID = new Input(Input.HIDDEN, BotForm.BOT_ID, String.valueOf(botId));
        tdFormElement.addElement(hiddenBotStationID);
        tdFormElement.addElement(hiddenBotID);
        Table table = BotTableBuilder.buildBotDetailsTable(getUser(), pageContext, bot);
        tdFormElement.addElement(table);
    }

    private Bot findBot() {
        return Delegates.getBotService().getBot(getUser(), botId);
    }

    @Override
    protected String getTitle() {
        return Messages.getMessage(Messages.TITLE_BOT_DETAILS, pageContext);
    }

    @Override
    protected String getFormButtonName() {
        return Messages.getMessage(Messages.BUTTON_APPLY, pageContext);
    }

    @Override
    public String getButtonAlignment() {
        return "right";
    }

    @Override
    public String getAction() {
        return UpdateBotAction.UPDATE_BOT_ACTION_PATH;
    }

    @Override
    public boolean isFormButtonEnabled() {
        return Delegates.getAuthorizationService().isAllowed(getUser(), BotStationPermission.BOT_STATION_CONFIGURE, BotStation.INSTANCE);
    }
}
