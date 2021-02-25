package ru.runa.wf.web.html;

import org.apache.ecs.ConcreteElement;
import org.apache.ecs.StringElement;
import org.apache.ecs.html.A;
import org.apache.ecs.html.TD;
import ru.runa.common.WebResources;
import ru.runa.common.web.Commons;
import ru.runa.common.web.html.BaseTdBuilder;
import ru.runa.wfe.chat.dto.WfChatRoom;
import ru.runa.wfe.commons.web.PortletUrlType;
import ru.runa.wfe.security.Permission;

/**
 * Created on 25.02.2021
 *
 * @author Sergey Inyakin
 */
public class ChatRoomTdBuilder extends BaseTdBuilder {
    private final String propertyName;

    public ChatRoomTdBuilder(Permission permission, String propertyName) {
        super(permission);
        this.propertyName = propertyName;
    }

    @Override
    public TD build(Object object, Env env) {
        ConcreteElement element;
        WfChatRoom chatRoom = (WfChatRoom) object;
        if (chatRoom == null || !isEnabled(object, env)) {
            element = new StringElement(getValue(object, env));
        } else {
            String url = Commons.getActionUrl(WebResources.ACTION_MAPPING_CHAT_PAGE, propertyName, chatRoom.getId(),
                    env.getPageContext(), PortletUrlType.Action);
            element = new A(url, getValue(object, env));
        }
        TD td = new TD();
        td.setClass(ru.runa.common.web.Resources.CLASS_LIST_TABLE_TD);
        td.addElement(element);
        return td;
    }

    @Override
    public String getValue(Object object, Env env) {
        return String.valueOf(((WfChatRoom) object).getNewMessagesCount());
    }
}
