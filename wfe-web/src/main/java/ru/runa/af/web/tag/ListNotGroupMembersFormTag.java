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
package ru.runa.af.web.tag;

import java.util.List;

import org.tldgen.annotations.BodyContent;

import ru.runa.af.web.MessagesExecutor;
import ru.runa.af.web.action.AddMembersToGroupAction;
import ru.runa.common.web.MessagesCommon;
import ru.runa.wfe.security.Permission;
import ru.runa.wfe.service.delegate.Delegates;
import ru.runa.wfe.user.Executor;
import ru.runa.wfe.user.ExecutorPermission;
import ru.runa.wfe.user.Group;
import ru.runa.wfe.user.GroupPermission;

@org.tldgen.annotations.Tag(bodyContent = BodyContent.JSP, name = "listNotGroupMembersForm")
public class ListNotGroupMembersFormTag extends ListExecutorsBaseFormTag {

    private static final long serialVersionUID = 1770247337446619592L;

    @Override
    protected Permission getPermission() {
        return GroupPermission.ADD_TO_GROUP;
    }

    @Override
    public String getFormButtonName() {
        return MessagesCommon.BUTTON_ADD.message(pageContext);
    }

    @Override
    protected List<? extends Executor> getExecutors() {
        Group group = (Group) getExecutor();
        return Delegates.getExecutorService().getGroupChildren(getUser(), group, getBatchPresentation(), true);
    }

    @Override
    protected int getExecutorsCount() {
        Group group = (Group) getExecutor();
        return Delegates.getExecutorService().getGroupChildrenCount(getUser(), group, getBatchPresentation(), true);
    }

    @Override
    protected String getTitle() {
        return MessagesExecutor.TITLE_ADD_EXECUTORS_TO_GROUP.message(pageContext);
    }

    @Override
    public String getAction() {
        return AddMembersToGroupAction.ACTION_PATH;
    }

    @Override
    protected Permission getExecutorsPermission() {
        return ExecutorPermission.READ;
    }
}
