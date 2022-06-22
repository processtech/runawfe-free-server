package ru.runa.common.web.tag;

import java.util.ArrayList;
import java.util.List;
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
import ru.runa.wfe.commons.SystemProperties;
import ru.runa.wfe.commons.web.PortletUrlType;
import ru.runa.wfe.datasource.DataSourceStorage;
import ru.runa.wfe.datasource.DataSourceStuff;
import ru.runa.wfe.datasource.ExcelDataSource;
import ru.runa.wfe.security.Permission;
import ru.runa.wfe.security.SecuredObject;
import ru.runa.wfe.security.SecuredObjectType;
import ru.runa.wfe.security.SecuredSingleton;
import ru.runa.wfe.service.delegate.Delegates;
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

    private static final List<MenuForward> FORWARDS = new ArrayList<>();

    static {
        FORWARDS.add(new MenuForward(MessagesCommon.MAIN_MENU_ITEM_TASKS));
        FORWARDS.add(new MenuForward(MessagesCommon.MAIN_MENU_ITEM_DEFINITIONS));
        FORWARDS.add(new MenuForward(MessagesCommon.MAIN_MENU_ITEM_PROCESSES));
        FORWARDS.add(new MenuForward(MessagesCommon.MAIN_MENU_ITEM_ARCHIVED_PROCESSES));
        FORWARDS.add(new MenuForward(MessagesCommon.MAIN_MENU_ITEM_EXECUTORS));
        FORWARDS.add(new MenuForward(MessagesCommon.MAIN_MENU_ITEM_REPORTS));
        FORWARDS.add(new MenuForward(MessagesCommon.MAIN_MENU_ITEM_RELATIONS, SecuredSingleton.RELATIONS));
        FORWARDS.add(new MenuForward(MessagesCommon.MAIN_MENU_ITEM_BOT_STATION, SecuredSingleton.BOTSTATIONS));
        FORWARDS.add(new MenuForward(MessagesCommon.MAIN_MENU_ITEM_DATA_SOURCES, SecuredSingleton.DATASOURCES, true));
        FORWARDS.add(new MenuForward(MessagesCommon.MAIN_MENU_ITEM_INTERNAL_STORAGE, null, true));
        FORWARDS.add(new MenuForward(MessagesCommon.MAIN_MENU_ITEM_SYSTEM, SecuredSingleton.SYSTEM));
        FORWARDS.add(new MenuForward(MessagesCommon.MAIN_MENU_ITEM_ERRORS, SecuredSingleton.ERRORS, true));
        FORWARDS.add(new MenuForward(MessagesCommon.MAIN_MENU_ITEM_SETTINGS, null, true));
        FORWARDS.add(new MenuForward(MessagesCommon.MAIN_MENU_ITEM_LOGS));
        FORWARDS.add(new MenuForward(MessagesCommon.MAIN_MENU_ITEM_OBSERVABLE_TASKS));
        FORWARDS.add(new MenuForward(MessagesCommon.MAIN_MENU_ITEM_SEND_SIGNAL, null, true));
        FORWARDS.add(new MenuForward(MessagesCommon.MAIN_MENU_ITEM_CHATS));
    }

    @Attribute
    public void setVertical(boolean isVertical) {
        this.isVertical = isVertical;
    }

    public boolean isVertical() {
        return isVertical;
    }

    @Override
    public int doStartTag() {
        String tabForwardName = TabHttpSessionHelper.getTabForwardName(pageContext.getSession());
        Table headerTable = new Table();
        headerTable.setClass(Resources.CLASS_TAB_TABLE);
        if (isVertical()) {
            for (MenuForward fw : FORWARDS) {
                if (!isMenuForwardVisible(fw)) {
                    continue;
                }
                A a = getHref(fw.menuMessage.message(pageContext), fw.menuMessage.getKey());
                TR tr = new TR();
                headerTable.addElement(tr);
                TD td = new TD();
                if (fw.menuMessage.getKey().equals(tabForwardName)) {
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
            for (MenuForward fw : FORWARDS) {
                if (!isMenuForwardVisible(fw)) {
                    continue;
                }
                A a = getHref(fw.menuMessage.message(pageContext), fw.menuMessage.getKey());
                TD td = new TD();
                if (fw.menuMessage.getKey().equals(tabForwardName)) {
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
        return new A(url, title);
    }

    private User getUser() {
        return Commons.getUser(pageContext.getSession());
    }

    private boolean isMenuForwardVisible(MenuForward menuForward) {
        try {
            if (menuForward.forAdministratorOnly) {
                if (!Delegates.getExecutorService().isAdministrator(getUser())) {
                    return false;
                }
                if (menuForward.menuMessage.getKey().equals(MessagesCommon.MAIN_MENU_ITEM_INTERNAL_STORAGE.getKey())) {
                    final boolean isInternalStoragePresent = DataSourceStorage.getNames().contains(DataSourceStuff.INTERNAL_STORAGE_DATA_SOURCE_NAME);
                    if (!isInternalStoragePresent) {
                        return false;
                    }
                    if (!(DataSourceStorage.getDataSource(DataSourceStuff.INTERNAL_STORAGE_DATA_SOURCE_NAME) instanceof ExcelDataSource)) {
                        return false;
                    }
                }
                return true;
            }
            if (menuForward.menuMessage.getKey().equals("chat_rooms")) {
                return SystemProperties.isChatEnabled();
            }
            if (menuForward.menuMessage.getKey().equals("view_logs")) {
                return Delegates.getAuthorizationService().isAllowed(getUser(), Permission.VIEW_LOGS, SecuredSingleton.SYSTEM);
            }
            if (menuForward.menuMessage.getKey().equals("manage_observable_tasks")
                    && Delegates.getAuthorizationService().isAllowedForAny(getUser(), Permission.VIEW_TASKS, SecuredObjectType.EXECUTOR)) {
                return true;
            }
            if (menuForward.object != null) {
                return Delegates.getAuthorizationService().isAllowed(getUser(), Permission.READ, menuForward.object);
            } else {
                return true;
            }
        } catch (Exception e) {
            return false;
        }
    }

    static class MenuForward {
        final StrutsMessage menuMessage;
        final SecuredObject object;
        final boolean forAdministratorOnly;

        MenuForward(StrutsMessage menuMessage, SecuredObject object, boolean forAdministratorOnly) {
            this.menuMessage = menuMessage;
            this.object = object;
            this.forAdministratorOnly = forAdministratorOnly;
        }

        MenuForward(StrutsMessage menuMessage, SecuredObject object) {
            this(menuMessage, object, false);
        }

        MenuForward(StrutsMessage menuMessage) {
            this(menuMessage, null, false);
        }
    }
}
