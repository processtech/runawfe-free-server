package ru.runa.wfe.extension.handler;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import com.google.common.base.Strings;
import com.google.common.base.Throwables;
import com.google.common.io.ByteStreams;

import ru.runa.wfe.commons.ClassLoaderUtil;
import ru.runa.wfe.commons.email.EmailConfig;
import ru.runa.wfe.commons.email.EmailConfigParser;
import ru.runa.wfe.commons.email.EmailUtils;
import ru.runa.wfe.execution.ExecutionContext;
import ru.runa.wfe.extension.ActionHandlerBase;
import ru.runa.wfe.presentation.BatchPresentationFactory;
import ru.runa.wfe.task.Task;
import ru.runa.wfe.task.dao.TaskDAO;
import ru.runa.wfe.user.Actor;
import ru.runa.wfe.user.dao.ExecutorDAO;

public class ExpiredTaskNotifierHandler extends ActionHandlerBase {
	@Autowired
    private ExecutorDAO executorDAO;
	@Autowired
    private TaskDAO taskDAO;
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

    public void execute() throws Exception {
    	List<Actor> actors = executorDAO.getAllActors(BatchPresentationFactory.ACTORS.createNonPaged());
        List<String> emails = new ArrayList<>();
        for (Actor actor : actors) {
            if (!actor.isActive()) {
                continue;
            }
            String email = actor.getEmail();
            if (!Strings.isNullOrEmpty(email)) {
                List<Task> taskList = taskDAO.findByExecutor(actor);
                for (Task task : taskList) {
                	if(task.getDeadlineDate().before(new Date())){
                		EmailConfig config = EmailConfigParser.parse(configBytes);
                        config.getHeaderProperties().put("To", email);
                        config.setMessage(config.getMessage() + " - " + task.getName());
                        EmailUtils.sendMessageRequest(config);
                        emails.add(email);
                    }
                }
            }
        }
    }

	@Override
	public void execute(ExecutionContext executionContext) throws Exception {}
}
