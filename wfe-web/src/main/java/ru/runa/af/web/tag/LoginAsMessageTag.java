package ru.runa.af.web.tag;

import org.apache.ecs.html.A;
import org.tldgen.annotations.BodyContent;

import ru.runa.common.WebResources;
import ru.runa.common.web.Commons;
import ru.runa.common.web.form.IdForm;
import ru.runa.common.web.tag.MessageTag;
import ru.runa.wfe.commons.web.PortletUrlType;
import ru.runa.wfe.user.User;

@org.tldgen.annotations.Tag(bodyContent = BodyContent.EMPTY, name = "loginAsMessage")
public class LoginAsMessageTag extends MessageTag {

    private static final long serialVersionUID = 1L;

    @Override
    public String getMessage() {
        User user = Commons.getUser(pageContext.getSession());
        String url = Commons.getActionUrl(WebResources.ACTION_MAPPING_UPDATE_EXECUTOR, IdForm.ID_INPUT_NAME, user.getActor().getId(), pageContext,
                PortletUrlType.Render);
        A a = new A(url, "<I>" + user.getName() + "</I>");
        return super.getMessage() + " " + a.toString();
    }
}
