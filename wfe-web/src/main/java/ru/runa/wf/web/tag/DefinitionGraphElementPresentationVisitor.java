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

import javax.servlet.jsp.PageContext;

import ru.runa.wf.web.html.GraphElementPresentationHelper;
import ru.runa.wfe.graph.view.GraphElementPresentationVisitor;
import ru.runa.wfe.graph.view.MultiinstanceGraphElementPresentation;
import ru.runa.wfe.graph.view.SubprocessGraphElementPresentation;
import ru.runa.wfe.graph.view.TaskGraphElementPresentation;

/**
 * Operation to create links to subprocess definitions and tool tips for
 * minimized states.
 */
public class DefinitionGraphElementPresentationVisitor extends GraphElementPresentationVisitor {
    /**
     * Helper to create html
     */
    private final GraphElementPresentationHelper presentationHelper;

    public DefinitionGraphElementPresentationVisitor(PageContext pageContext, String subprocessId) {
        presentationHelper = new GraphElementPresentationHelper(pageContext, subprocessId);
    }

    @Override
    protected void onSubprocess(SubprocessGraphElementPresentation element) {
        presentationHelper.createSubprocessDefinitionLink(element);
    }

    @Override
    protected void onMultiSubprocess(MultiinstanceGraphElementPresentation element) {
        presentationHelper.createSubprocessDefinitionLink(element);
    }

    @Override
    protected void onTaskState(TaskGraphElementPresentation element) {
        presentationHelper.createTaskTooltip(element);
    }

    public GraphElementPresentationHelper getPresentationHelper() {
        return presentationHelper;
    }

}
