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
