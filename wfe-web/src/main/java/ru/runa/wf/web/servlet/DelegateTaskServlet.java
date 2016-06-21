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
            JSONArray tasksIds = (JSONArray)JSONValue.parse((String)parameters.get("tasksIds"));

            for (Object executorId : executorIds) {
                executors.add((Long) executorId);
            }
            for (Object task : tasksIds) {
            	tasks.add((Long) task);
            }
        } catch (Exception e) {
            log.error("Bad request", e);
            response.sendError(HttpServletResponse.SC_BAD_REQUEST);
            return;
        }
        
        if (taskId != -1L){
        	tasks.add(taskId);
        }
        
        // TODO Code just below works in assumption that all delegated tasks was assigned to the same user. If not - must be refactored. 
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

        Delegates.getTaskService().delegateTasks(user, tasks, currentOwner, executors);
    }

}
