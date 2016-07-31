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
import ru.runa.wfe.user.Executor;
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
        try {
            JSONParser jsonParser = new JSONParser();
            JSONObject parameters = (JSONObject) jsonParser.parse(request.getReader());
            Long taskId = (Long) parameters.get("taskId");
            Set<Long> tasks = Sets.newHashSet();
            String tasksIdsParam = (String)parameters.get("tasksIds");
            boolean keepCurrentOwners = (Boolean) parameters.get("keepCurrent");
            JSONArray executorIdsArray = (JSONArray) parameters.get("executors");
            Set<Long> executorIds = Sets.newHashSet();
            for (Object executorId : executorIdsArray) {
                executorIds.add((Long) executorId);
            }
            User user = Commons.getUser(request.getSession());
            List<Executor> executors = Lists.newArrayList();
            for (Long executorId : executorIds) {
                executors.add(Delegates.getExecutorService().getExecutor(user, executorId));
            }
            if (taskId != -1L) {
                // Single task processing
                WfTask task = Delegates.getTaskService().getTask(user, taskId);
                Delegates.getTaskService().delegateTask(user, taskId, task.getOwner(), keepCurrentOwners, executors);
            } else if (tasksIdsParam != null) {
                // Multiple tasks processing
                JSONArray tasksIds = (JSONArray) JSONValue.parse(tasksIdsParam);
                for (Object task : tasksIds) {
                    tasks.add((Long) task);
                }
                Delegates.getTaskService().delegateTasks(user, tasks, keepCurrentOwners, executors);
            }
        } catch (Exception e) {
            log.error("Bad request", e);
            response.sendError(HttpServletResponse.SC_BAD_REQUEST);
            return;
        }
    }

}
