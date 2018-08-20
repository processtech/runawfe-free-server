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
package ru.runa.wfe.extension.handler;

import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;

import ru.runa.wfe.commons.ClassLoaderUtil;
import ru.runa.wfe.commons.email.EmailConfig;
import ru.runa.wfe.commons.email.EmailConfigParser;
import ru.runa.wfe.commons.email.EmailUtils;
import ru.runa.wfe.execution.ExecutionContext;
import ru.runa.wfe.extension.ActionHandlerBase;
import ru.runa.wfe.form.Interaction;
import ru.runa.wfe.presentation.BatchPresentationFactory;
import ru.runa.wfe.security.auth.UserHolder;
import ru.runa.wfe.task.Task;
import ru.runa.wfe.task.dao.TaskDao;
import ru.runa.wfe.user.Actor;
import ru.runa.wfe.user.dao.ExecutorDao;

import com.google.common.base.Objects;
import com.google.common.base.Strings;
import com.google.common.base.Throwables;
import com.google.common.io.ByteStreams;

/**
 * Created on 28.10.2008
 * 
 * @author A. Shautsou
 * @version 1.0 Initial version
 */
public class EmailTaskNotifierActionHandler extends ActionHandlerBase {
    @Autowired
    private ExecutorDao executorDao;
    @Autowired
    private TaskDao taskDao;
    private byte[] configBytes;

    @Override
    public void setConfiguration(String path) {
        super.setConfiguration(path);
        InputStream in = ClassLoaderUtil.getAsStreamNotNull(path, getClass());
        try {
            configBytes = ByteStreams.toByteArray(in);
        } catch (IOException e) {
            Throwables.propagate(e);
        }
    }

    @Override
    public void execute(ExecutionContext executionContext) throws Exception {
        List<Actor> actors = executorDao.getAllActors(BatchPresentationFactory.ACTORS.createNonPaged());
        for (Actor actor : actors) {
            if (!actor.isActive()) {
                continue;
            }
            String email = actor.getEmail();
            if (!Strings.isNullOrEmpty(email)) {
                List<Task> taskList = taskDao.findByExecutor(actor);
                for (Task task : taskList) {
                    if (!Objects.equal(task, executionContext.getTask())) {
                        EmailConfig config = EmailConfigParser.parse(configBytes);
                        config.getHeaderProperties().put("To", email);
                        Interaction interaction = executionContext.getProcessDefinition().getInteractionNotNull(task.getNodeId());
                        EmailUtils.prepareMessage(UserHolder.get(), config, interaction, executionContext.getVariableProvider());
                        EmailUtils.sendMessageRequest(config);
                    }
                }
            }
        }
    }
}
