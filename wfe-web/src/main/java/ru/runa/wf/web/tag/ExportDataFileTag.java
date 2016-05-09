package ru.runa.wf.web.tag;

import org.apache.ecs.ConcreteElement;
import org.apache.ecs.StringElement;
import org.apache.ecs.html.A;
import org.apache.ecs.html.TD;
import org.apache.ecs.html.TH;
import org.apache.ecs.html.TR;
import org.apache.ecs.html.Table;
import org.tldgen.annotations.BodyContent;

import ru.runa.common.WebResources;
import ru.runa.common.web.Commons;
import ru.runa.common.web.Messages;
import ru.runa.common.web.MessagesOther;
import ru.runa.common.web.Resources;
import ru.runa.common.web.tag.VisibleTag;
import ru.runa.wf.web.action.ExportDataFileAction;
import ru.runa.wfe.commons.web.PortletUrlType;
import ru.runa.wfe.service.delegate.Delegates;

@org.tldgen.annotations.Tag(bodyContent = BodyContent.JSP, name = "exportDataFile")
public class ExportDataFileTag extends VisibleTag {
    private static final long serialVersionUID = 1L;

    @Override
    protected ConcreteElement getEndElement() {
        Table table = new Table();
        TR tr = new TR();
        TD td = new TD();
        if (Delegates.getExecutorService().isAdministrator(getUser())) {
            String downloadUrl = Commons.getActionUrl(ExportDataFileAction.ACTION_PATH, pageContext, PortletUrlType.Render);
            A a = new A(downloadUrl, MessagesOther.LABEL_EXPORT.message(pageContext));
            a.setClass(Resources.CLASS_LINK);
            td.addElement(a);
        } else {
            td.addElement(MessagesOther.LABEL_EXPORT.message(pageContext));
        }
        tr.addElement(td);
        table.addElement(tr);

        StringBuilder sb = new StringBuilder();
        sb.append(table);
        sb.append(new TD().createEndTag());
        sb.append(new TR().createEndTag());
        sb.append(new Table().createEndTag());
        return new StringElement(sb.toString());
    }

    @Override
    protected ConcreteElement getStartElement() {
        StringBuilder sb = new StringBuilder();
        Table table = new Table();
        table.setClass(Resources.CLASS_BOX);
        sb.append(table.createStartTag());
        if (getTitle() != null) {
            TR trh = new TR(((TH) new TH().setClass(Resources.CLASS_BOX_TITLE)).addElement(getTitle()));
            sb.append(trh.toString());
        }
        sb.append(new TR().createStartTag());
        TD td = new TD();
        td.setClass(Resources.CLASS_BOX_BODY);
        sb.append(td.createStartTag());
        return new StringElement(sb.toString());
    }

    @Override
    public boolean isVisible() {
        return WebResources.getBooleanProperty("action.datafile.enabled", false);
    }

    protected String getTitle() {
        return MessagesOther.TITLE_EXPORT_DATAFILE.message(pageContext);
    }
}
