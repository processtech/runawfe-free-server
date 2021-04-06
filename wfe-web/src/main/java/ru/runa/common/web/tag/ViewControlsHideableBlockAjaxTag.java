package ru.runa.common.web.tag;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.JspWriter;

import org.apache.ecs.Entities;
import org.apache.ecs.html.A;
import org.apache.ecs.html.IMG;
import org.apache.ecs.html.Select;
import org.apache.ecs.html.TD;
import org.apache.ecs.html.TR;
import org.apache.ecs.html.Table;
import org.tldgen.annotations.Attribute;
import org.tldgen.annotations.BodyContent;

import ru.runa.common.web.BatchPresentationsVisibility;
import ru.runa.common.web.Commons;
import ru.runa.common.web.HTMLUtils;
import ru.runa.common.web.Messages;
import ru.runa.common.web.MessagesBatch;
import ru.runa.common.web.ProfileHttpSessionHelper;
import ru.runa.common.web.Resources;
import ru.runa.common.web.action.ChangeActiveBatchPresentationAction;
import ru.runa.common.web.action.HideableBlockAction;
import ru.runa.common.web.form.BatchPresentationForm;
import ru.runa.common.web.form.ReturnActionForm;
import ru.runa.wfe.commons.web.PortletUrlType;
import ru.runa.wfe.presentation.BatchPresentation;
import ru.runa.wfe.user.Profile;

@org.tldgen.annotations.Tag(bodyContent = BodyContent.JSP, name = "viewControlsHideableBlock")
public class ViewControlsHideableBlockAjaxTag extends AbstractReturningTag {
    private static final long serialVersionUID = -4644961104278379700L;

    private String hideableBlockId;

    @Attribute(required = true, rtexprvalue = true)
    public void setHideableBlockId(String id) {
        hideableBlockId = id;
    }

    public String getHideableBlockId() {
        return hideableBlockId;
    }

    public String getHideTitle() {
        return MessagesBatch.HIDE_CONTROLS.message(pageContext);
    }

    public String getShowTitle() {
        return MessagesBatch.SHOW_CONTROLS.message(pageContext);
    }

    @Override
    public String getAction() {
        return HideableBlockAction.ACTION_PATH;
    }

    @Override
    public int doStartTag() throws JspException {
        JspWriter jspOut = pageContext.getOut();
        try {
            jspOut.println(new Table().setWidth("100%").createStartTag());
            TR tr = new TR();
            TD headerTD = new TD();
            tr.addElement(headerTD);
            headerTD.setClass(Resources.CLASS_HIDEABLEBLOCK);

            boolean contentVisible = BatchPresentationsVisibility.get(pageContext.getSession()).isBlockVisible(hideableBlockId);
            headerTD.addElement(Entities.NBSP);

            String id = getHideableBlockId() + "Controls";

            A link = new A("javascript:viewBlock('" + getHideableBlockId() + "');");
            headerTD.addElement(link);
            link.setID(id + "Link");
            link.setClass(Resources.CLASS_HIDEABLEBLOCK);
            String imgLink = contentVisible ? Resources.VISIBLE_IMAGE : Resources.HIDDEN_IMAGE;
            IMG img = new IMG(Commons.getUrl(imgLink, pageContext, PortletUrlType.Resource));
            img.setID(id + "Img");
            link.addElement(img);
            img.setAlt(contentVisible ? Resources.VISIBLE_ALT : Resources.HIDDEN_ALT);
            img.setClass(Resources.CLASS_HIDEABLEBLOCK);
            link.addElement(contentVisible ? getHideTitle() : getShowTitle());

            headerTD.addElement(Entities.NBSP);
            addEndOptionalContent(headerTD);

            tr.output(jspOut);
            String style = "";
            if (!contentVisible) {
                style = "style=\"display: none;\"";
            }
            jspOut.println("<tr><td id=\"" + id + "\" " + style + ">");
            return EVAL_BODY_INCLUDE;
        } catch (IOException e) {
            throw new JspException(e);
        }
    }

    @Override
    public int doEndTag() throws JspException {
        JspWriter jspOut = pageContext.getOut();
        try {
            jspOut.println("</td></tr>");
            jspOut.println(new Table().createEndTag());
        } catch (IOException e) {
            throw new JspException(e);
        }
        return EVAL_PAGE;
    }

    public void addEndOptionalContent(TD td) throws JspException {
        td.addElement(Entities.NBSP);
        Select select = new Select(BatchPresentationForm.BATCH_PRESENTATION_NAME);
        Profile profile = ProfileHttpSessionHelper.getProfile(pageContext.getSession());
        BatchPresentation activeBatchPresentation = profile.getActiveBatchPresentation(getHideableBlockId());

        for (BatchPresentation batchPresentation : profile.getBatchPresentations(getHideableBlockId())) {
            String batchPresentationName = batchPresentation.getName();
            Map<String, String> params = new HashMap<String, String>();
            params.put(BatchPresentationForm.BATCH_PRESENTATION_ID, activeBatchPresentation.getCategory());
            params.put(BatchPresentationForm.BATCH_PRESENTATION_NAME, batchPresentation.getName());
            params.put(ReturnActionForm.RETURN_ACTION, getReturnAction());
            String actionUrl = Commons.getActionUrl(ChangeActiveBatchPresentationAction.ACTION_PATH, params, pageContext, PortletUrlType.Action);
            String label = batchPresentation.isDefault() ? Messages.getMessage(batchPresentationName, pageContext) : batchPresentationName;
            boolean isSelected = batchPresentationName.equals(activeBatchPresentation.getName());
            select.addElement(HTMLUtils.createOption(actionUrl, label, isSelected));
        }
        select.setOnChange("document.location=this.options[this.selectedIndex].value");
        td.addElement(select);
        td.addElement(Entities.NBSP);
    }

}
