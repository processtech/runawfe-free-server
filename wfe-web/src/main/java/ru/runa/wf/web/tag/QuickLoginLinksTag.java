package ru.runa.wf.web.tag;

import java.util.List;
import javax.servlet.http.HttpServletRequest;
import org.apache.ecs.ConcreteElement;
import org.apache.ecs.StringElement;
import org.apache.ecs.html.A;
import org.apache.ecs.html.Div;
import org.apache.ecs.html.TD;
import org.apache.ecs.html.TH;
import org.apache.ecs.html.TR;
import org.apache.ecs.html.Table;
import org.tldgen.annotations.BodyContent;
import ru.runa.af.web.action.LoginAction;
import ru.runa.af.web.action.LogoutAction;
import ru.runa.common.WebResources;
import ru.runa.common.web.Commons;
import ru.runa.common.web.MessagesCommon;
import ru.runa.common.web.MessagesOther;
import ru.runa.common.web.Resources;
import ru.runa.common.web.StrutsMessage;
import ru.runa.common.web.tag.VisibleTag;
import ru.runa.wf.web.action.ExportDataFileAction;
import ru.runa.wfe.commons.ApplicationContextFactory;
import ru.runa.wfe.commons.web.PortletUrlType;
import ru.runa.wfe.service.delegate.Delegates;
import ru.runa.wfe.user.Actor;
import ru.runa.wfe.user.dao.ExecutorDao;

@org.tldgen.annotations.Tag(bodyContent = BodyContent.JSP, name = "quickLoginLinks")
public class QuickLoginLinksTag extends VisibleTag {
    private static final long serialVersionUID = 1L;

    @Override
    protected ConcreteElement getEndElement() {
        Table table = new Table();

        table.addElement(createHeaderTr());

        HttpServletRequest request = (HttpServletRequest) pageContext.getRequest();
        String contextPath = request.getContextPath();

        for(Actor actor : getActors()) {
            table.addElement(createLoginLinkTr(actor, contextPath));
        }

        return table;
    }

    private ConcreteElement createLoginLinkTr(Actor actor, String contextPath) {
        String actionURL = Commons.getActionUrl(LoginAction.ACTION_NAME, "login", actor.getName(),
                pageContext,  PortletUrlType.Action);
        A a = new A(actionURL).addElement(actor.getName());
        return new TR().addElement(new TD().addElement(a));
    }

    private ConcreteElement createHeaderTr() {
        TR tr = new TR();
        tr.addElement(new TD().addElement(new StringElement(MessagesCommon.LOGIN_PAGE_LOGIN_AS.message(pageContext))));
        return tr;
    }

    @SuppressWarnings("unchecked")
    List<Actor> getActors() {
        ExecutorDao executorDao = ApplicationContextFactory.getContext().getBean(ExecutorDao.class);
        return (List<Actor>) ApplicationContextFactory.getTransactionalExecutor().executeWithResult(() -> executorDao.getAllNonSystemActorsWithLimit(100));
    }

    @Override
    protected ConcreteElement getStartElement() {
        return new StringElement();
    }
}
