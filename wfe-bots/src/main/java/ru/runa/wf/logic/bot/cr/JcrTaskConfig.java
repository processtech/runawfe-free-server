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
package ru.runa.wf.logic.bot.cr;

import java.util.ArrayList;
import java.util.List;

public class JcrTaskConfig {
    private final String repositoryName;
    private final String userName;
    private final String password;
    private final List<JcrTask> tasks = new ArrayList<JcrTask>();

    public JcrTaskConfig(String repositoryName, String userName, String password) {
        this.repositoryName = repositoryName;
        this.userName = userName;
        this.password = password;
    }

    public void addTask(JcrTask task) {
        tasks.add(task);
    }

    public List<JcrTask> getTasks() {
        return tasks;
    }

    public String getRepositoryName() {
        return repositoryName;
    }

    public String getUserName() {
        return userName;
    }

    public String getPassword() {
        return password;
    }
}
