package ru.runa.af.web.tag;

import org.apache.ecs.html.Input;
import org.apache.ecs.html.TD;
import org.apache.ecs.html.TR;
import org.apache.ecs.html.Table;
import org.tldgen.annotations.Attribute;
import org.tldgen.annotations.BodyContent;

import ru.runa.af.web.action.StartPeriodicBotsInvocationAction;
import ru.runa.af.web.action.StopPeriodicBotsInvocationAction;
import ru.runa.af.web.form.BotStationForm;
import ru.runa.common.web.tag.TitledFormTag;
import ru.runa.wf.web.MessagesBot;
import ru.runa.wfe.bot.BotStation;
import ru.runa.wfe.security.Permission;
import ru.runa.wfe.service.delegate.BotInvokerServiceDelegate;
import ru.runa.wfe.service.delegate.Delegates;

@org.tldgen.annotations.Tag(bodyContent = BodyContent.JSP, name = "botStationStatusTag")
public class BotStationStatusTag extends TitledFormTag {
    private static final long serialVersionUID = 1920713038009470026L;

    private Long botStationId;

    @Attribute(required = false, rtexprvalue = true)
    public void setBotStationId(Long botStationId) {
        this.botStationId = botStationId;
    }

    public Long getBotStationId() {
        return botStationId;
    }

    private boolean stationOn = false;
    private boolean periodicInvocationOn = false;

    private void renewValues() {
        try {
            BotStation botStation = Delegates.getBotService().getBotStation(botStationId);
            periodicInvocationOn = BotInvokerServiceDelegate.getService(botStation).isRunning();
            stationOn = true;
        } catch (Exception e) {
            log.warn("Unable to obtain status of botstation " + botStationId, e);
            stationOn = false;
            periodicInvocationOn = false;
        }
    }

    @Override
    protected void fillFormElement(TD tdFormElement) {
        renewValues();
        Input hiddenBotStationID = new Input(Input.HIDDEN, BotStationForm.BOT_STATION_ID, String.valueOf(botStationId));
        tdFormElement.addElement(hiddenBotStationID);
        Table table = new Table();
        TR tr = new TR();
        tr.addElement(new TD((stationOn ? MessagesBot.MESSAGE_BOTSTATION_ON : MessagesBot.MESSAGE_BOTSTATION_OFF).message(pageContext)));
        table.addElement(tr);
        if (stationOn) {
            tr = new TR();
            tr.addElement(new TD((periodicInvocationOn ? MessagesBot.MESSAGE_PERIODIC_BOTS_INVOCATION_ON
                    : MessagesBot.MESSAGE_PERIODIC_BOTS_INVOCATION_OFF).message(pageContext)));
            table.addElement(tr);
        }
        tdFormElement.addElement(table);
    }

    @Override
    protected String getTitle() {
        return MessagesBot.TITLE_BOT_STATION_STATUS.message(pageContext);
    }

    @Override
    protected String getFormButtonName() {
        if (periodicInvocationOn) {
            return MessagesBot.BUTTON_STOP_PERIODIC_BOTS_INVOCATION.message(pageContext);
        } else {
            return MessagesBot.BUTTON_START_PERIODIC_BOTS_INVOCATION.message(pageContext);
        }
    }

    @Override
    public String getButtonAlignment() {
        return "left";
    }

    @Override
    public String getAction() {
        if (periodicInvocationOn) {
            return StopPeriodicBotsInvocationAction.STOP_PERIODIC_BOTS_INVOCATION;
        } else {
            return StartPeriodicBotsInvocationAction.START_PERIODIC_BOTS_INVOCATION;
        }
    }

    @Override
    public boolean isFormButtonEnabled() {
        boolean result = Delegates.getAuthorizationService().isAllowed(getUser(), Permission.BOT_STATION_CONFIGURE, BotStation.INSTANCE);
        if (!result) {
            return false;
        }
        renewValues();
        return stationOn;
    }

}
