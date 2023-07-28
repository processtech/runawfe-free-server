package ru.runa.af.web.html;

import java.util.List;

import javax.servlet.jsp.PageContext;

import org.apache.ecs.html.A;
import org.apache.ecs.html.Input;
import org.apache.ecs.html.TD;
import org.apache.ecs.html.TH;
import org.apache.ecs.html.TR;
import org.apache.ecs.html.Table;

import ru.runa.af.web.form.BotStationForm;
import ru.runa.common.web.Commons;
import ru.runa.common.web.HTMLUtils;
import ru.runa.common.web.Resources;
import ru.runa.common.web.form.IdsForm;
import ru.runa.wf.web.MessagesBot;
import ru.runa.wfe.bot.BotStation;
import ru.runa.wfe.commons.web.PortletUrlType;

/**
 * User: stan79 Date: 29.05.2008 Time: 12:35:18 $Id
 */
public class BotStationTableBuilder {

    private final PageContext pageContext;

    public BotStationTableBuilder(PageContext pageContext) {
        this.pageContext = pageContext;
    }

    public Table buildBotStationTable(List<BotStation> botStations) {
        Table table = new Table();
        table.setClass(Resources.CLASS_LIST_TABLE);
        table.setWidth("100%");
        table.addElement(createTableHeaderTR());
        for (BotStation botStation : botStations) {
            table.addElement(createTR(botStation));
        }
        return table;
    }

    private TR createTR(BotStation botStation) {
        TR tr = new TR();
        tr.setClass(Resources.CLASS_LIST_TABLE_TH);
        Input input = new Input(Input.CHECKBOX, IdsForm.IDS_INPUT_NAME, Long.toString(botStation.getId()));
        input.setChecked(false);
        String path = Commons.getActionUrl("bot_station.do", "botStationId", botStation.getId(), pageContext, PortletUrlType.Render);
        tr.addElement(new TD(input).setClass(Resources.CLASS_LIST_TABLE_TD));
        tr.addElement(new TD(new A(path, botStation.getName())).setClass(Resources.CLASS_LIST_TABLE_TD));
        return tr;
    }

    private TR createTableHeaderTR() {
        TR tr = new TR();
        tr.setClass(Resources.CLASS_LIST_TABLE_TH);
        tr.addElement(new TH(HTMLUtils.createSelectionStatusPropagator()).setWidth("20").setClass(Resources.CLASS_LIST_TABLE_TD));
        tr.addElement(new TH(MessagesBot.LABEL_BOT_STATION_NAME.message(pageContext)).setClass(Resources.CLASS_LIST_TABLE_TD));
        return tr;
    }

    public static Table createBotStationDetailsTable(PageContext pageContext, String name) {
        Table table = new Table();
        table.setClass(Resources.CLASS_LIST_TABLE);
        Input nameInput = HTMLUtils.createInput(BotStationForm.BOT_STATION_NAME, name, true, true);
        table.addElement(HTMLUtils.createRow(MessagesBot.LABEL_BOT_STATION_NAME.message(pageContext), nameInput));
        return table;
    }

}
