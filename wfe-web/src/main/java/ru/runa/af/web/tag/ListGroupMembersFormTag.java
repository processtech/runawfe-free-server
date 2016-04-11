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

import ru.runa.af.web.action.RemoveExecutorsFromGroupAction;
import ru.runa.common.web.ConfirmationPopupHelper;
import ru.runa.common.web.Messages;
import ru.runa.wfe.security.Permission;
import ru.runa.wfe.security.SecuredObjectType;
import ru.runa.wfe.service.ExecutorService;
import ru.runa.wfe.service.delegate.Delegates;
import ru.runa.wfe.user.Executor;
import ru.runa.wfe.user.ExecutorPermission;
import ru.runa.wfe.user.Group;
import ru.runa.wfe.user.GroupPermission;

/**
 * Created on 23.08.2004
 * 
 * @jsp.tag name = "listGroupMembersForm" body-content = "JSP"
 */
public class ListGroupMembersFormTag extends ListExecutorsBaseFormTag {

    private static final long serialVersionUID = -2400457393576894819L;

    @Override
    protected Permission getPermission() {
        return GroupPermission.REMOVE_FROM_GROUP;
    }

    @Override
    public String getFormButtonName() {
        return Messages.getMessage(Messages.BUTTON_REMOVE, pageContext);
    }

    @Override
    protected boolean isVisible() {
        return getExecutor() instanceof Group
                && Delegates.getAuthorizationService().isAllowed(getUser(), GroupPermission.LIST_GROUP, SecuredObjectType.GROUP, getIdentifiableId());
    }

    @Override
    protected List<? extends Executor> getExecutors() {
        ExecutorService executorService = Delegates.getExecutorService();
        return executorService.getGroupChildren(getUser(), (Group) getExecutor(), getBatchPresentation(), false);
    }

    @Override
    protected int getExecutorsCount() {
        ExecutorService executorService = Delegates.getExecutorService();
        // java.lang.ClassCastException: ru.runa.wfe.user.Actor cannot be cast
        // to ru.runa.wfe.user.Group
        // at
        // ru.runa.af.web.tag.ListGroupMembersFormTag.getExecutorsCount(ListGroupMembersFormTag.java:67)
        // at
        // ru.runa.af.web.tag.ListExecutorsBaseFormTag.fillFormData(ListExecutorsBaseFormTag.java:78)
        return executorService.getGroupChildrenCount(getUser(), (Group) getExecutor(), getBatchPresentation(), false);
    }

    @Override
    protected String getTitle() {
        return Messages.getMessage(Messages.TITLE_GROUP_MEMBERS, pageContext);
    }

    @Override
    public String getAction() {
        return RemoveExecutorsFromGroupAction.ACTION_PATH;
    }

    @Override
    protected Permission getExecutorsPermission() {
        return ExecutorPermission.READ;
    }

    @Override
    public String getConfirmationPopupParameter() {
        return ConfirmationPopupHelper.REMOVE_EXECUTORS_FROM_GROUPS_PARAMETER;
    }
}
