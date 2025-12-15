package ru.runa.wfe.task.dao;

import org.springframework.stereotype.Component;
import ru.runa.wfe.commons.dao.CommonDao;
import ru.runa.wfe.task.TaskFormDraft;
import ru.runa.wfe.user.User;

@Component
public class TaskFormDraftDao extends CommonDao {

    public TaskFormDraft find(User user, Long taskId) {
        return (TaskFormDraft) sessionFactory.getCurrentSession()
                .createQuery("SELECT tfd FROM TaskFormDraft tfd WHERE tfd.taskId = :taskId and tfd.actorId = :actorId")
                .setParameter("taskId", taskId)
                .setParameter("actorId", user.getActor().getId())
                .uniqueResult();
    }

    public void delete(Long taskId) {
        sessionFactory.getCurrentSession()
                .createQuery("DELETE FROM TaskFormDraft tfd WHERE tfd.taskId = :taskId")
                .setParameter("taskId", taskId)
                .executeUpdate();
    }

    public void delete(User user, Long taskId) {
        TaskFormDraft taskFormDraft = find(user, taskId);
        if (null != taskFormDraft)
            sessionFactory.getCurrentSession().delete(taskFormDraft);
    }

    public void save(User user, Long taskId, byte[] data) {
        TaskFormDraft taskFormDraft = find(user, taskId);
        if (null == taskFormDraft) {
            taskFormDraft = new TaskFormDraft();
            taskFormDraft.setActorId(user.getActor().getId());
            taskFormDraft.setTaskId(taskId);
        }

        taskFormDraft.setData(data);

        sessionFactory.getCurrentSession().save(taskFormDraft);
    }
}
