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
import ru.runa.af.web.action.RemoveExecutorFromGroupsAction;
import ru.runa.common.web.ConfirmationPopupHelper;
import ru.runa.common.web.MessagesCommon;
import ru.runa.wfe.security.Permission;
import ru.runa.wfe.service.ExecutorService;
import ru.runa.wfe.service.delegate.Delegates;
import ru.runa.wfe.user.Executor;

@org.tldgen.annotations.Tag(bodyContent = BodyContent.JSP, name = "listExecutorGroupsForm")
public class ListExecutorGroupsFormTag extends ListExecutorsBaseFormTag {

    private static final long serialVersionUID = -2141545567983138556L;

    @Override
    public String getSubmitButtonName() {
        return MessagesCommon.BUTTON_REMOVE.message(pageContext);
    }

    @Override
    protected List<? extends Executor> getExecutors() {
        ExecutorService executorService = Delegates.getExecutorService();
        return executorService.getExecutorGroups(getUser(), getExecutor(), getBatchPresentation(), false);
    }

    @Override
    protected int getExecutorsCount() {
        ExecutorService executorService = Delegates.getExecutorService();
        return executorService.getExecutorGroupsCount(getUser(), getExecutor(), getBatchPresentation(), false);
    }

    @Override
    protected String getTitle() {
        return MessagesExecutor.TITLE_EXECUTOR_GROUPS.message(pageContext);
    }

    @Override
    public String getAction() {
        return RemoveExecutorFromGroupsAction.ACTION_PATH;
    }

    @Override
    protected Permission getExecutorsPermission() {
        return Permission.UPDATE;  // TODO Was REMOVE_FROM_GROUP. Why in *List*ExecutorGroupsFormTag?
    }

    @Override
    public String getConfirmationPopupParameter() {
        return ConfirmationPopupHelper.REMOVE_EXECUTORS_FROM_GROUPS_PARAMETER;
    }
}
