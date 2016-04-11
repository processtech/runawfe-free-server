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

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import javax.servlet.jsp.JspException;
import javax.servlet.jsp.JspWriter;

import org.apache.ecs.Entities;
import org.apache.ecs.html.A;
import org.apache.ecs.html.IMG;
import org.apache.ecs.html.Option;
import org.apache.ecs.html.Script;
import org.apache.ecs.html.Select;
import org.apache.ecs.html.TD;
import org.apache.ecs.html.TR;
import org.apache.ecs.html.Table;

import ru.runa.common.web.BatchPresentationsVisibility;
import ru.runa.common.web.Commons;
import ru.runa.common.web.Messages;
import ru.runa.common.web.ProfileHttpSessionHelper;
import ru.runa.common.web.Resources;
import ru.runa.common.web.action.ChangeActiveBatchPresentationAction;
import ru.runa.common.web.action.HideableBlockAction;
import ru.runa.common.web.form.BatchPresentationForm;
import ru.runa.common.web.form.ReturnActionForm;
import ru.runa.wfe.commons.web.PortletUrlType;
import ru.runa.wfe.presentation.BatchPresentation;
import ru.runa.wfe.user.Profile;

/**
 * Created 18.05.2005
 * 
 * @jsp.tag name = "viewControlsHideableBlock" body-content = "JSP"
 */
public class ViewControlsHideableBlockAjaxTag extends AbstractReturningTag {
    private static final long serialVersionUID = -4644961104278379700L;

    private String hideableBlockId;

    public void setHideableBlockId(String id) {
        hideableBlockId = id;
    }

    public String getHideableBlockId() {
        return hideableBlockId;
    }

    public String getHideTitle() {
        return Messages.getMessage(Messages.LABEL_HIDE_CONTROLS, pageContext);
    }

    public String getShowTitle() {
        return Messages.getMessage(Messages.LABEL_SHOW_CONTROLS, pageContext);
    }

    @Override
    public String getAction() {
        return HideableBlockAction.ACTION_PATH;
    }

    @Override
    public int doStartTag() throws JspException {
        JspWriter jspOut = pageContext.getOut();
        try {
            jspOut.println(new Table().createStartTag());
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
            Option option = new Option();
            Map<String, String> params = new HashMap<String, String>();
            params.put(BatchPresentationForm.BATCH_PRESENTATION_ID, activeBatchPresentation.getCategory());
            params.put(BatchPresentationForm.BATCH_PRESENTATION_NAME, batchPresentation.getName());
            params.put(ReturnActionForm.RETURN_ACTION, getReturnAction());
            String actionUrl = Commons.getActionUrl(ChangeActiveBatchPresentationAction.ACTION_PATH, params, pageContext, PortletUrlType.Action);
            option.setValue(actionUrl);
            if (batchPresentation.isDefault()) {
                option.addElement(Messages.getMessage(batchPresentationName, pageContext));
            } else {
                option.addElement(batchPresentationName);
            }
            if (batchPresentationName.equals(activeBatchPresentation.getName())) {
                option.setSelected(true);
            }
            select.addElement(option);
        }
        select.setOnChange("document.location=this.options[this.selectedIndex].value");
        td.addElement(select);
        td.addElement(Entities.NBSP);
    }

}
