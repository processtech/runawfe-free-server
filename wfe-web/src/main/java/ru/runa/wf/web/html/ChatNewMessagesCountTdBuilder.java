package ru.runa.wf.web.html;

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
public class ChatNewMessagesCountTdBuilder extends BaseTdBuilder {
    private final String propertyName;

    public ChatNewMessagesCountTdBuilder(Permission permission, String propertyName) {
        super(permission);
        this.propertyName = propertyName;
    }

    @Override
    public TD build(Object object, Env env) {
        WfChatRoom chatRoom = (WfChatRoom) object;
        String url = Commons.getActionUrl(WebResources.ACTION_MAPPING_CHAT_PAGE, propertyName, chatRoom.getId(),
                env.getPageContext(), PortletUrlType.Action);
        TD td = new TD(new A(url, getValue(object, env)));
        td.setClass(ru.runa.common.web.Resources.CLASS_LIST_TABLE_TD);
        return td;
    }

    @Override
    public String getValue(Object object, Env env) {
        return String.valueOf(((WfChatRoom) object).getNewMessagesCount());
    }
}
