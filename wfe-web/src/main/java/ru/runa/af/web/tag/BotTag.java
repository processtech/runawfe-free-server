package ru.runa.af.web.tag;

import com.google.common.base.Preconditions;
import org.apache.ecs.html.Input;
import org.apache.ecs.html.TD;
import org.apache.ecs.html.Table;
import org.tldgen.annotations.Attribute;
import org.tldgen.annotations.BodyContent;
import ru.runa.af.web.action.UpdateBotAction;
import ru.runa.af.web.form.BotForm;
import ru.runa.af.web.html.BotTableBuilder;
import ru.runa.common.web.MessagesCommon;
import ru.runa.common.web.tag.TitledFormTag;
import ru.runa.wf.web.MessagesBot;
import ru.runa.wfe.bot.Bot;
import ru.runa.wfe.security.Permission;
import ru.runa.wfe.security.SecuredSingleton;
import ru.runa.wfe.service.delegate.Delegates;

@org.tldgen.annotations.Tag(bodyContent = BodyContent.JSP, name = "botTag")
public class BotTag extends TitledFormTag {
    private static final long serialVersionUID = 1L;

    private Long botId;

    @Attribute(required = false, rtexprvalue = true)
    public void setBotId(Long botId) {
        this.botId = botId;
    }

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
        return MessagesBot.TITLE_BOT_DETAILS.message(pageContext);
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
        return UpdateBotAction.UPDATE_BOT_ACTION_PATH;
    }

    @Override
    public boolean isSubmitButtonEnabled() {
        return Delegates.getAuthorizationService().isAllowed(getUser(), Permission.ALL, SecuredSingleton.BOTSTATIONS);
    }
}
