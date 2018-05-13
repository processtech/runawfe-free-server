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

package ru.runa.wf.web.tag;

import org.apache.ecs.html.TD;
import org.tldgen.annotations.BodyContent;

import ru.runa.common.web.Messages;
import ru.runa.common.web.tag.TitledFormTag;
import ru.runa.wf.web.FormPresentationUtils;
import ru.runa.wfe.definition.DefinitionClassPresentation;

@org.tldgen.annotations.Tag(bodyContent = BodyContent.EMPTY, name = "htmlFilter")
public class HtmlFilterTag extends TitledFormTag {

    private static final long serialVersionUID = 3401243801430914L;

    @Override
    protected void fillFormElement(TD tdFormElement) {
        byte[] htmlBytes = (byte[]) pageContext.getRequest().getAttribute("htmlBytes");
        Long processDefinitionId = (Long) pageContext.getRequest().getAttribute("processDefinitionId");
        String pageHref = (String) pageContext.getRequest().getAttribute("pageHref");
        String filtered = FormPresentationUtils.adjustUrls(pageContext, processDefinitionId, pageHref, htmlBytes);
        tdFormElement.addElement(filtered);
    }

    @Override
    protected String getTitle() {
        return Messages.getMessage(DefinitionClassPresentation.DESCRIPTION, pageContext);
    }

    @Override
    protected boolean isSubmitButtonVisible() {
        return false;
    }

}
