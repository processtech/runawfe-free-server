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

import javax.servlet.jsp.tagext.Tag;

import org.apache.ecs.ConcreteElement;
import org.apache.ecs.StringElement;
import org.apache.ecs.html.TD;
import org.apache.ecs.html.TH;
import org.apache.ecs.html.TR;
import org.apache.ecs.html.Table;

import ru.runa.common.web.ActionExceptionHelper;
import ru.runa.common.web.Resources;
import ru.runa.common.web.html.TDBuilder;
import ru.runa.wfe.presentation.BatchPresentation;
import ru.runa.wfe.presentation.ClassPresentation;
import ru.runa.wfe.presentation.FieldDescriptor;
import ru.runa.wfe.presentation.FieldState;

/**
 * Created on 08.09.2004
 * 
 */
public abstract class TitledFormTag extends FormTag {
    private static final long serialVersionUID = 1L;

    private String title;

    private String width;

    private String height;

    private String align;

    private String valign;

    @Override
    protected ConcreteElement getStartElement() {
        StringBuilder sb = new StringBuilder();
        Table table = new Table();
        if (id != null) {
            table.setID(id);
        }
        if (width != null) {
            table.setWidth(width);
        }
        if (height != null) {
            table.setHeight(height);
        }
        table.setClass(Resources.CLASS_BOX);
        sb.append(table.createStartTag());
        if (getTitle() != null) {
            TR trh = new TR(((TH) new TH().setClass(Resources.CLASS_BOX_TITLE)).addElement(getTitle()));
            sb.append(trh.toString());
        }
        sb.append(new TR().createStartTag());
        TD td = new TD();
        td.setClass(Resources.CLASS_BOX_BODY);
        if (align != null) {
            td.setAlign(align);
        }
        if (valign != null) {
            td.setVAlign(valign);
        }
        sb.append(td.createStartTag());
        return new StringElement(sb.toString());
    }

    @Override
    protected ConcreteElement getEndElement() {
        StringBuilder sb = new StringBuilder();
        try {
            sb.append(super.getEndElement());
        } catch (Throwable th) {
            // DEBUG category set due to logging in EJB layer; stack trace
            // is logged only for Web layer errors.
            log.debug("", th);
            sb.append("<span class=\"error\">" + ActionExceptionHelper.getErrorMessage(th, pageContext) + "</span>");
        }
        sb.append(new TD().createEndTag());
        sb.append(new TR().createEndTag());
        sb.append(new Table().createEndTag());
        return new StringElement(sb.toString());
    }

    @Override
    protected int doEndTagReturnedValue() {
        return Tag.EVAL_PAGE;
    }

    @Override
    protected int doStartTagReturnedValue() {
        int result;
        if (isVisible()) {
            result = Tag.EVAL_BODY_INCLUDE;
        } else {
            result = Tag.SKIP_BODY;
        }
        return result;
    }

    /**
     * @jsp.attribute required = "false" rtexprvalue = "true"
     */
    public void setAlign(String align) {
        this.align = align;
    }

    /**
     * @jsp.attribute required = "false" rtexprvalue = "true"
     */
    public void setHeight(String height) {
        this.height = height;
    }

    /**
     * @jsp.attribute required = "false" rtexprvalue = "true"
     */
    public void setTitle(String title) {
        this.title = title;
    }

    protected String getTitle() {
        return title;
    }

    /**
     * @jsp.attribute required = "false" rtexprvalue = "true"
     */
    public void setValign(String valign) {
        this.valign = valign;
    }

    /**
     * @jsp.attribute required = "false" rtexprvalue = "true"
     */
    public void setWidth(String width) {
        this.width = width;
    }

    public String getButtonAlignment() {
        return buttonAlignment;
    }

    /**
     * @jsp.attribute required = "false" rtexprvalue = "true"
     */
    public void setButtonAlignment(String alignment) {
        buttonAlignment = alignment;
    }

    protected static TDBuilder[] getBuilders(TDBuilder[] prefix, BatchPresentation batchPresentation, TDBuilder[] suffix) {
        int displayed = batchPresentation.getDisplayFields().length;
        for (FieldDescriptor field : batchPresentation.getDisplayFields()) {
            if (field.displayName.startsWith(ClassPresentation.editable_prefix) || field.displayName.startsWith(ClassPresentation.filterable_prefix)
                    || field.fieldState != FieldState.ENABLED) {
                --displayed;
            }
        }
        TDBuilder[] builders = new TDBuilder[prefix.length + displayed + suffix.length];
        for (int i = 0; i < prefix.length; ++i) {
            builders[i] = prefix[i];
        }
        int idx = 0;
        for (int i = 0; i < batchPresentation.getDisplayFields().length; ++i) {
            if ((!batchPresentation.getDisplayFields()[i].displayName.startsWith(ClassPresentation.editable_prefix) && !batchPresentation
                    .getDisplayFields()[i].displayName.startsWith(ClassPresentation.filterable_prefix))
                    && batchPresentation.getDisplayFields()[i].fieldState == FieldState.ENABLED) {
                builders[(idx++) + prefix.length] = (TDBuilder) batchPresentation.getDisplayFields()[i].getTDBuilder();
            }
        }
        for (int i = 0; i < suffix.length; ++i) {
            builders[i + prefix.length + displayed] = suffix[i];
        }
        return builders;
    }

    public String getWidth() {
        return width;
    }
}
