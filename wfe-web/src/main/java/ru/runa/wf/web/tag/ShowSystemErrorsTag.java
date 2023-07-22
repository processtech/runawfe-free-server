package ru.runa.wf.web.tag;

import com.google.common.collect.Lists;
import java.util.List;
import org.apache.ecs.ConcreteElement;
import org.apache.ecs.StringElement;
import org.apache.ecs.html.A;
import org.apache.ecs.html.TD;
import org.apache.ecs.html.TH;
import org.apache.ecs.html.TR;
import org.tldgen.annotations.BodyContent;
import ru.runa.common.web.Resources;
import ru.runa.common.web.html.HeaderBuilder;
import ru.runa.common.web.html.RowBuilder;
import ru.runa.common.web.html.TableBuilder;
import ru.runa.common.web.html.TrRowBuilder;
import ru.runa.common.web.tag.VisibleTag;
import ru.runa.wf.web.MessagesError;
import ru.runa.wfe.commons.CalendarUtil;
import ru.runa.wfe.commons.error.SystemError;
import ru.runa.wfe.service.delegate.Delegates;

@org.tldgen.annotations.Tag(bodyContent = BodyContent.JSP, name = "viewSystemErrors")
public class ShowSystemErrorsTag extends VisibleTag {
    private static final long serialVersionUID = 1L;

    @Override
    protected ConcreteElement getStartElement() {
        return new StringElement();
    }

    @Override
    protected ConcreteElement getEndElement() {
        List<TR> rows = Lists.newArrayList();
        for (SystemError systemError : Delegates.getSystemService().getSystemErrors(getUser())) {
            TR tr = new TR();
            tr.addElement(new TD(CalendarUtil.formatDateTime(systemError.getOccurredDate())).setClass(Resources.CLASS_LIST_TABLE_TD));
            String url = "javascript:showSystemError('" + systemError.getMessage() + "')";
            tr.addElement(new TD(new A(url, systemError.getMessage())).setClass(Resources.CLASS_LIST_TABLE_TD));
            A deleteLink = new A("javascript:void(0);", "X");
            deleteLink.setOnClick("deleteSystemError(this, '" + systemError.getMessage() + "')");
            tr.addElement(new TD(deleteLink).setClass(Resources.CLASS_LIST_TABLE_TD));
            rows.add(tr);
        }
        if (rows.size() > 0) {
            this.pageContext.getRequest().setAttribute("errorsExist", true);
        }
        ErrorsHeaderBuilder tasksHistoryHeaderBuilder = new ErrorsHeaderBuilder();
        RowBuilder rowBuilder = new TrRowBuilder(rows);
        TableBuilder tableBuilder = new TableBuilder();
        return tableBuilder.build(tasksHistoryHeaderBuilder, rowBuilder);
    }

    private class ErrorsHeaderBuilder implements HeaderBuilder {

        @Override
        public TR build() {
            TR tr = new TR();
            tr.addElement(new TH(MessagesError.ERRORS_OCCURED.message(pageContext)).setClass(Resources.CLASS_LIST_TABLE_TH));
            tr.addElement(new TH(MessagesError.ERRORS_ERROR.message(pageContext)).setClass(Resources.CLASS_LIST_TABLE_TH));
            tr.addElement(new TH("").setClass(Resources.CLASS_LIST_TABLE_TH));
            return tr;
        }
    }

}
