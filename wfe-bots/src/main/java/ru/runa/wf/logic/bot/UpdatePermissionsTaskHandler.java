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
package ru.runa.wf.logic.bot;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Set;
import ru.runa.wf.logic.bot.updatepermission.Method;
import ru.runa.wf.logic.bot.updatepermission.UpdatePermissionsSettings;
import ru.runa.wf.logic.bot.updatepermission.UpdatePermissionsXmlParser;
import ru.runa.wfe.InternalApplicationException;
import ru.runa.wfe.commons.CollectionUtil;
import ru.runa.wfe.execution.logic.SwimlaneInitializerHelper;
import ru.runa.wfe.extension.handler.TaskHandlerBase;
import ru.runa.wfe.security.Permission;
import ru.runa.wfe.security.SecuredObject;
import ru.runa.wfe.service.delegate.Delegates;
import ru.runa.wfe.task.dto.WfTask;
import ru.runa.wfe.user.Executor;
import ru.runa.wfe.user.User;
import ru.runa.wfe.var.VariableProvider;

/**
 * Sets permissions to current process.
 * 
 * @author dofs
 * @since 2.0
 */
public class UpdatePermissionsTaskHandler extends TaskHandlerBase {
    private UpdatePermissionsSettings settings;

    @Override
    public void setConfiguration(String configuration) {
        settings = UpdatePermissionsXmlParser.read(configuration);
    }

    @Override
    public Map<String, Object> handle(User user, VariableProvider variableProvider, WfTask task) throws Exception {
        boolean allowed = true;
        if (settings.isConditionExists()) {
            String conditionVar = variableProvider.getValue(String.class, settings.getConditionVarName());
            if (!settings.getConditionVarValue().equals(conditionVar)) {
                allowed = false;
            }
        }
        if (allowed) {
            Set<Executor> executors = Sets.newHashSet();
            for (String swimlaneInitializer : settings.getSwimlaneInitializers()) {
                executors.addAll(SwimlaneInitializerHelper.evaluate(swimlaneInitializer, variableProvider));
            }
            List<Collection<Permission>> allPermissions = Lists.newArrayListWithExpectedSize(executors.size());
            SecuredObject securedObject = Delegates.getExecutionService().getProcess(user, task.getProcessId());
            List<Long> executorIds = Lists.newArrayList();
            for (Executor executor : executors) {
                List<Permission> oldPermissions = Delegates.getAuthorizationService().getIssuedPermissions(user, executor, securedObject);
                allPermissions.add(getNewPermissions(oldPermissions, settings.getPermissions(), settings.getMethod()));
                executorIds.add(executor.getId());
            }
            Delegates.getAuthorizationService().setPermissions(user, executorIds, allPermissions, securedObject);
        }
        return null;
    }

    private Collection<Permission> getNewPermissions(Collection<Permission> oldPermissions, Collection<Permission> permissions, Method method) {
        if (Method.add == method) {
            return CollectionUtil.unionSet(oldPermissions, permissions);
        } else if (Method.set == method) {
            return permissions;
        } else if (Method.delete == method) {
            return CollectionUtil.diffSet(oldPermissions, permissions);
        } else {
            // should never happened
            throw new InternalApplicationException("Unknown method provided: " + method);
        }
    }
}
