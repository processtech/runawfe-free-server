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
package ru.runa.af.web.form;

import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import org.apache.struts.action.ActionMapping;
import org.apache.struts.upload.FormFile;

import ru.runa.common.web.form.IdsForm;
import ru.runa.wfe.InternalApplicationException;

import com.google.common.collect.Maps;

/**
 * @author petrmikheev
 * @struts:form name = "botTasksForm"
 */
public class BotTasksForm extends IdsForm {
    private static final long serialVersionUID = 1L;
    public static final String BOT_TASK_INPUT_NAME_PREFIX = "task(";
    public static final String NAME_INPUT_NAME = ").name";
    public static final String HANDLER_INPUT_NAME = ").handler";
    public static final String CONFIG_FILE_INPUT_NAME = ").configFile";
    public static final String SEQUENTIAL_INPUT_NAME = ").sequential";
    private static final Map<Long, BotTaskForm> taskBeans = Maps.newHashMap();

    @Override
    public void reset(ActionMapping mapping, HttpServletRequest request) {
        super.reset(mapping, request);
        taskBeans.clear();
    }

    /**
     * Access from struts
     */
    public Object getTask(String taskIdString) {
        Long taskId = Long.valueOf(taskIdString);
        if (!taskBeans.containsKey(taskId)) {
            taskBeans.put(taskId, new BotTaskForm());
        }
        return taskBeans.get(taskId);
    }

    public BotTaskForm getBotTaskNotNull(Long id) {
        if (taskBeans.containsKey(id)) {
            return taskBeans.get(id);
        }
        throw new InternalApplicationException("No task bean for id = " + id + ", all " + taskBeans);
    }

    public static class BotTaskForm {
        private String name;
        private String handler;
        private String config;
        private boolean sequential;
        private FormFile configFile;

        public String getName() {
            return name;
        }

        public void setName(String name) {
            this.name = name;
        }

        public String getConfig() {
            return config;
        }

        public void setConfig(String config) {
            this.config = config;
        }

        public String getHandler() {
            return handler;
        }

        public void setHandler(String handler) {
            this.handler = handler;
        }

        public FormFile getConfigFile() {
            return configFile;
        }

        public void setConfigFile(FormFile configFile) {
            this.configFile = configFile;
        }

        public boolean isSequential() {
            return sequential;
        }

        public void setSequential(boolean sequential) {
            this.sequential = sequential;
        }
    }
}
