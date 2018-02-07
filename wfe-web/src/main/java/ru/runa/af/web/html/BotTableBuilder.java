/*
 * This file is part of the RUNA WFE project.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public License
 * as published by the Free Software Foundation; version 2.1
 * of the License.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA.
 */
package ru.runa.af.web.html;

import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import javax.servlet.jsp.PageContext;

import org.apache.ecs.Element;
import org.apache.ecs.html.A;
import org.apache.ecs.html.Input;
import org.apache.ecs.html.TD;
import org.apache.ecs.html.TH;
import org.apache.ecs.html.TR;
import org.apache.ecs.html.Table;

import ru.runa.af.web.form.BotForm;
import ru.runa.af.web.tag.ActorSelect;
import ru.runa.common.web.Commons;
import ru.runa.common.web.HTMLUtils;
import ru.runa.common.web.Resources;
import ru.runa.common.web.form.IdsForm;
import ru.runa.wf.web.MessagesBot;
import ru.runa.wfe.bot.Bot;
import ru.runa.wfe.commons.web.PortletUrlType;
import ru.runa.wfe.user.User;

/**
 * @author petrmikheev
 */
public class BotTableBuilder {

    private final PageContext pageContext;

    public BotTableBuilder(PageContext pageContext) {
        this.pageContext = pageContext;
    }

    public Table buildBotTable(List<Bot> bots) {
        Table table = new Table();
        table.setClass(Resources.CLASS_LIST_TABLE);
        table.setWidth("100%");
        table.addElement(createTableHeaderTR());
        for (Iterator<Bot> iterator = bots.iterator(); iterator.hasNext();) {
            table.addElement(createTR(iterator.next()));
        }
        return table;
    }

    private TR createTR(Bot bot) {
        TR tr = new TR();
        tr.setClass(Resources.CLASS_LIST_TABLE_TH);
        Input input = new Input(Input.CHECKBOX, IdsForm.IDS_INPUT_NAME, Long.toString(bot.getId()));
        String path = Commons.getActionUrl("bot.do", BotForm.BOT_ID, bot.getId(), pageContext, PortletUrlType.Render);
        tr.addElement(new TD(input).setClass(Resources.CLASS_LIST_TABLE_TD));
        tr.addElement(new TD(new A(path, bot.getUsername())).setClass(Resources.CLASS_LIST_TABLE_TD));
        return tr;
    }

    private TR createTableHeaderTR() {
        TR tr = new TR();
        tr.setClass(Resources.CLASS_LIST_TABLE_TH);
        tr.addElement(new TH(HTMLUtils.createSelectionStatusPropagator()).setWidth("20").setClass(Resources.CLASS_LIST_TABLE_TD));
        tr.addElement(new TH(MessagesBot.LABEL_BOT_NAME.message(pageContext)).setClass(Resources.CLASS_LIST_TABLE_TD));
        return tr;
    }

    public static Table buildBotDetailsTable(User user, PageContext pageContext, Bot bot) {
        Table table = new Table();
        table.setClass(Resources.CLASS_LIST_TABLE);
        ActorSelect actorSelect = new ActorSelect(user, BotForm.USER_NAME, bot != null ? bot.getUsername() : "", true);
        table.addElement(HTMLUtils.createSelectRow(MessagesBot.LABEL_BOT_NAME.message(pageContext), actorSelect, true));
        Input passwordInput = HTMLUtils.createInput(Input.PASSWORD, BotForm.PASSWORD, bot != null ? bot.getPassword() : "");
        table.addElement(HTMLUtils.createRow(MessagesBot.LABEL_BOT_PASSWORD.message(pageContext), passwordInput));
        if (bot.isTransactional()) {
        	table.addElement(HTMLUtils.createCheckboxRow(MessagesBot.LABEL_BOT_SEQUENTIAL.message(pageContext), BotForm.IS_SEQUENTIAL,
                    true, false, false));
        } else {
			table.addElement(HTMLUtils.createCheckboxRow(MessagesBot.LABEL_BOT_SEQUENTIAL.message(pageContext),
					BotForm.IS_SEQUENTIAL, bot != null ? bot.isSequentialExecution() : false, true, false));
        }

        TR transactionalCheckRow = createTransactionalCheckRow(pageContext, bot);
        
        table.addElement(transactionalCheckRow);
        
        return table;
    }

	private static TR createTransactionalCheckRow(PageContext pageContext, Bot bot) {
		Input timeoutInput = null;
        if (bot.isTransactional()) {
        	 timeoutInput = HTMLUtils.createInput(Input.TEXT, BotForm.BOT_TIMEOUT, bot != null ? String.valueOf(bot.getTimeout()) : "");
        } else {
        	 timeoutInput = HTMLUtils.createInput(Input.TEXT, BotForm.BOT_TIMEOUT, "");
        }
		
		TR tr = new TR();
        tr.addElement(new TD(MessagesBot.LABEL_BOT_TRANSACTIONAL.message(pageContext)).setClass(Resources.CLASS_LIST_TABLE_TD)); 
        TD td = new TD(HTMLUtils.createCheckboxInput(BotForm.IS_TRANSACTIONAL,
        		bot !=null ? bot.isTransactional() : false, true, false));
        timeoutInput.addAttribute("style", "width: 5%");
        timeoutInput.setDisabled(!bot.isTransactional());
        td.addElement(MessagesBot.LABEL_BOT_TRANSACTIONAL_TIMEOUT.message(pageContext));
        td.addElement(timeoutInput);
        td.setClass(Resources.CLASS_LIST_TABLE_TD);
        tr.addElement(td);
		return tr;
	}
}
