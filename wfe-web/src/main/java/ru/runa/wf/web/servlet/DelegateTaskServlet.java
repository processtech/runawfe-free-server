package ru.runa.wf.web.servlet;

import java.io.IOException;
import java.util.List;
import java.util.Set;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.JSONValue;
import org.json.simple.parser.JSONParser;

import ru.runa.common.WebResources;
import ru.runa.common.web.Commons;
import ru.runa.wfe.service.delegate.Delegates;
import ru.runa.wfe.task.dto.WfTask;
import ru.runa.wfe.user.Actor;
import ru.runa.wfe.user.Executor;
import ru.runa.wfe.user.Group;
import ru.runa.wfe.user.TemporaryGroup;
import ru.runa.wfe.user.User;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

public class DelegateTaskServlet extends HttpServlet {
    private static final long serialVersionUID = 1L;
    private static final Log log = LogFactory.getLog(DelegateTaskServlet.class);

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        if (!WebResources.isTaskDelegationEnabled()) {
            response.sendError(HttpServletResponse.SC_FORBIDDEN, "task.delegation.enabled");
            return;
        }

        User user = Commons.getUser(request.getSession());

        JSONObject parameters;
        try {
            JSONParser jsonParser = new JSONParser();
            parameters = (JSONObject) jsonParser.parse(request.getReader());
        } catch (Exception e) {
            parameters = new JSONObject();
        }

        Long taskId;
        Boolean keepCurrent;
        Set<Long> executors = Sets.newHashSet();
        Set<Long> tasks = Sets.newHashSet();

        try {
            JSONArray executorIds = (JSONArray) parameters.get("executors");
            keepCurrent = (Boolean) parameters.get("keepCurrent");
            taskId = (Long) parameters.get("taskId");
            for (Object executorId : executorIds) {
                executors.add((Long) executorId);
            }
            String tasksIdsParam = (String)parameters.get("tasksIds");
            if (tasksIdsParam != null) {
            	JSONArray tasksIds = (JSONArray)JSONValue.parse(tasksIdsParam);
                for (Object task : tasksIds) {
                	tasks.add((Long) task);
                }
            }
        } catch (Exception e) {
            log.error("Bad request", e);
            response.sendError(HttpServletResponse.SC_BAD_REQUEST);
            return;
        }

        // taskId == -1L - means that single delegation are processed. We stick it to one case with multiple delegation
        if (taskId != -1L){
        	tasks.add(taskId);
        }

        // currentOwner are got in assumption that all delegated tasks was assigned to the same executor.
        // If not - let it go as is, it will be adjusted in TaskLogic.
        WfTask task = Delegates.getTaskService().getTask(user, tasks.iterator().next());
        Executor currentOwner = task.getOwner();

        if (keepCurrent) {
	        if (currentOwner instanceof TemporaryGroup) {
	            Group g = (Group) currentOwner;
	            for (Actor actor : Delegates.getExecutorService().getGroupActors(user, g)) {
	                executors.add(actor.getId());
	            }
	        } else {
	            executors.add(currentOwner.getId());
	        }
        }

        List<Executor> executorList = Lists.newArrayList();
        for (Long executorId : executors) {
            Executor newOwner = Delegates.getExecutorService().getExecutor(user, executorId);
            executorList.add(newOwner);
        }

        Delegates.getTaskService().delegateTasks(user, tasks, currentOwner, executorList);
    }

}
