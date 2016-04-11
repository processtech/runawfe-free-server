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

import ru.runa.common.web.Commons;
import ru.runa.common.web.Messages;
import ru.runa.common.web.Resources;
import ru.runa.common.web.TabHttpSessionHelper;
import ru.runa.wfe.bot.BotStation;
import ru.runa.wfe.commons.web.PortletUrlType;
import ru.runa.wfe.relation.RelationsGroupSecure;
import ru.runa.wfe.security.ASystem;
import ru.runa.wfe.security.Identifiable;
import ru.runa.wfe.security.Permission;
import ru.runa.wfe.service.delegate.Delegates;
import ru.runa.wfe.user.User;

/**
 * Represnts tabs for managing different secured objects types. Created on
 * 04.10.2004
 * 
 * @author Vitaliy S aka Yilativs
 * @author Gordienko_m
 * @jsp.tag name = "tabHeader" body-content = "JSP"
 */

public class TabHeaderTag extends TagSupport {
    private static final long serialVersionUID = 2424605209992058409L;

    private boolean isVertical = true;

    private static final List<MenuForward> FORWARDS = new ArrayList<MenuForward>();
    static {
        FORWARDS.add(new MenuForward("manage_tasks"));
        FORWARDS.add(new MenuForward("manage_definitions"));
        FORWARDS.add(new MenuForward("manage_processes"));
        FORWARDS.add(new MenuForward("manage_executors"));
        FORWARDS.add(new MenuForward("manage_relations", RelationsGroupSecure.INSTANCE));
        FORWARDS.add(new MenuForward("configure_bot_station", BotStation.INSTANCE));
        FORWARDS.add(new MenuForward("manage_system", ASystem.INSTANCE));
        FORWARDS.add(new MenuForward("manage_settings"));
        FORWARDS.add(new MenuForward("view_logs", ASystem.INSTANCE));
    }

    /**
     * @jsp.attribute required = "false" rtexprvalue = "true"
     */
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
                A a = getHref(Messages.getMessage(FORWARDS.get(i).getMenuName(), pageContext), FORWARDS.get(i).getMenuName());
                TR tr = new TR();
                headerTable.addElement(tr);
                TD td = new TD();
                if (FORWARDS.get(i).getMenuName().equals(tabForwardName)) {
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
                A a = getHref(Messages.getMessage(FORWARDS.get(i).getMenuName(), pageContext), FORWARDS.get(i).getMenuName());
                TD td = new TD();
                if (FORWARDS.get(i).getMenuName().equals(tabForwardName)) {
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
			if (menuForward.menuName.equals("manage_settings"))
				return Delegates.getExecutorService().isAdministrator(getUser());
			if (menuForward.menuSecuredObject != null) {
				return Delegates.getAuthorizationService().isAllowed(getUser(), Permission.READ, menuForward.menuSecuredObject);
			}
		} catch (Exception e) {
			return false;
		}
        return true;
    }

    static class MenuForward {
        private final String menuName;
        private final Identifiable menuSecuredObject;

        public MenuForward(String menuName, Identifiable menuSecuredObject) {
            this.menuName = menuName;
            this.menuSecuredObject = menuSecuredObject;
        }

        public MenuForward(String menuName) {
            this(menuName, null);
        }

        public String getMenuName() {
            return menuName;
        }

        public Identifiable getMenuSecuredObject() {
            return menuSecuredObject;
        }
    }
}
