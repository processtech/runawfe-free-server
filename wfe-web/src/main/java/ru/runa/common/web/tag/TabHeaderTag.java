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
package ru.runa.common.web.tag;

import java.util.ArrayList;
import java.util.List;

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.JspWriter;
import javax.servlet.jsp.tagext.TagSupport;

import org.apache.ecs.html.A;
import org.apache.ecs.html.TD;
import org.apache.ecs.html.TR;
import org.apache.ecs.html.Table;
import org.tldgen.annotations.Attribute;
import org.tldgen.annotations.BodyContent;

import ru.runa.common.web.Commons;
import ru.runa.common.web.MessagesCommon;
import ru.runa.common.web.Resources;
import ru.runa.common.web.StrutsMessage;
import ru.runa.common.web.TabHttpSessionHelper;
import ru.runa.wfe.bot.BotStation;
import ru.runa.wfe.commons.web.PortletUrlType;
import ru.runa.wfe.relation.RelationsGroupSecure;
import ru.runa.wfe.report.ReportsSecure;
import ru.runa.wfe.security.ASystem;
import ru.runa.wfe.security.Identifiable;
import ru.runa.wfe.security.Permission;
import ru.runa.wfe.security.SecuredObjectType;
import ru.runa.wfe.service.delegate.Delegates;
import ru.runa.wfe.user.ActorPermission;
import ru.runa.wfe.user.User;

/**
 * Represents tabs for managing different secured objects types. Created on 04.10.2004
 * 
 * @author Vitaliy S aka Yilativs
 * @author Gordienko_m
 */
@org.tldgen.annotations.Tag(bodyContent = BodyContent.JSP, name = "tabHeader")
public class TabHeaderTag extends TagSupport {
    private static final long serialVersionUID = 2424605209992058409L;

    private boolean isVertical = true;

    private static final List<MenuForward> FORWARDS = new ArrayList<MenuForward>();
    static {
        FORWARDS.add(new MenuForward(MessagesCommon.MAIN_MENU_ITEM_TASKS));
        FORWARDS.add(new MenuForward(MessagesCommon.MAIN_MENU_ITEM_DEFINITIONS));
        FORWARDS.add(new MenuForward(MessagesCommon.MAIN_MENU_ITEM_PROCESSES));
        FORWARDS.add(new MenuForward(MessagesCommon.MAIN_MENU_ITEM_EXECUTORS));
        FORWARDS.add(new MenuForward(MessagesCommon.MAIN_MENU_ITEM_REPORTS, ReportsSecure.INSTANCE));
        FORWARDS.add(new MenuForward(MessagesCommon.MAIN_MENU_ITEM_RELATIONS, RelationsGroupSecure.INSTANCE));
        FORWARDS.add(new MenuForward(MessagesCommon.MAIN_MENU_ITEM_BOT_STATION, BotStation.INSTANCE));
        FORWARDS.add(new MenuForward(MessagesCommon.MAIN_MENU_ITEM_DATA_SOURCES, ASystem.INSTANCE));
        FORWARDS.add(new MenuForward(MessagesCommon.MAIN_MENU_ITEM_SYSTEM, ASystem.INSTANCE));
        FORWARDS.add(new MenuForward(MessagesCommon.MAIN_MENU_ITEM_SETTINGS));
        FORWARDS.add(new MenuForward(MessagesCommon.MAIN_MENU_ITEM_LOGS, ASystem.INSTANCE));
        FORWARDS.add(new MenuForward(MessagesCommon.MAIN_MENU_ITEM_OBSERVABLE_TASKS, ASystem.INSTANCE));
    }

    @Attribute(required = false, rtexprvalue = true)
    public void setVertical(boolean isVertical) {
        this.isVertical = isVertical;
    }

    public boolean isVertical() {
        return isVertical;
    }

    @Override
    public int doStartTag() throws JspException {
        String tabForwardName = TabHttpSessionHelper.getTabForwardName(pageContext.getSession());
        Table headerTable = new Table();
        headerTable.setClass(Resources.CLASS_TAB_TABLE);
        if (isVertical()) {
            for (int i = 0; i < FORWARDS.size(); i++) {
                if (!isMenuForwardVisible(FORWARDS.get(i))) {
                    continue;
                }
                A a = getHref(FORWARDS.get(i).getMenuMessage().message(pageContext), FORWARDS.get(i).getMenuMessage().getKey());
                TR tr = new TR();
                headerTable.addElement(tr);
                TD td = new TD();
                if (FORWARDS.get(i).getMenuMessage().getKey().equals(tabForwardName)) {
                    td.setClass(Resources.CLASS_TAB_CELL_SELECTED);
                } else {
                    td.setClass(Resources.CLASS_TAB_CELL);
                }
                tr.addElement(td);
                td.addElement(a);
            }
        } else {
            TR tr = new TR();
            headerTable.addElement(tr);
            headerTable.setWidth("100%");
            for (int i = 0; i < FORWARDS.size(); i++) {
                if (!isMenuForwardVisible(FORWARDS.get(i))) {
                    continue;
                }
                A a = getHref(FORWARDS.get(i).getMenuMessage().message(pageContext), FORWARDS.get(i).getMenuMessage().getKey());
                TD td = new TD();
                if (FORWARDS.get(i).getMenuMessage().getKey().equals(tabForwardName)) {
                    td.setClass(Resources.CLASS_TAB_CELL_SELECTED);
                }
                tr.addElement(td);
                td.addElement(a);
            }
        }
        JspWriter writer = pageContext.getOut();
        headerTable.output(writer);
        return SKIP_BODY;
    }

    private A getHref(String title, String forward) {
        String url = Commons.getForwardUrl(forward, pageContext, PortletUrlType.Render);
        A link = new A(url, title);
        return link;
    }

    private User getUser() {
        return Commons.getUser(pageContext.getSession());
    }

    private boolean isMenuForwardVisible(MenuForward menuForward) {
        try {
            if (menuForward.getMenuMessage().getKey().equals("manage_settings")) {
                return Delegates.getExecutorService().isAdministrator(getUser());
            }
            if (
                    menuForward.getMenuMessage().getKey().equals("manage_observable_tasks") &&
                    Delegates.getAuthorizationService().isAllowedForAny(getUser(), ActorPermission.VIEW_TASKS, SecuredObjectType.ACTOR)
            ) {
                return true;
            }
            if (menuForward.menuSecuredObject != null) {
                return Delegates.getAuthorizationService().isAllowed(getUser(), Permission.READ, menuForward.menuSecuredObject);
            }
        } catch (Exception e) {
            return false;
        }
        return true;
    }

    static class MenuForward {
        private final StrutsMessage menuMessage;
        private final Identifiable menuSecuredObject;

        public MenuForward(StrutsMessage menuMessage, Identifiable menuSecuredObject) {
            this.menuMessage = menuMessage;
            this.menuSecuredObject = menuSecuredObject;
        }

        public MenuForward(StrutsMessage menuMessage) {
            this(menuMessage, null);
        }

        public StrutsMessage getMenuMessage() {
            return menuMessage;
        }

        public Identifiable getMenuSecuredObject() {
            return menuSecuredObject;
        }
    }
}
