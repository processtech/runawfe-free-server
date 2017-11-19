package ru.runa.wfe.redmine.api;

import java.io.InputStream;
import java.util.Date;
import java.util.Map;

import com.taskadapter.redmineapi.bean.Issue;
import com.taskadapter.redmineapi.bean.Project;
import com.taskadapter.redmineapi.bean.TimeEntry;

/**
 * Интерфейс для работы с redmine
 * @author Veniamin
 *
 */
public interface RedmineFacade {
    
    /**
     * Создает новый проект в redmine
     * @param name - название проекта.
     * @param id   - идентификатор проекта.
     * @return
     * @throws Exception
     */
    public Project createProject(
        String name,
        String id) 
            throws Exception;
    
    /**
     * Удаляет проект из redmine.
     * @param id - идентификатор проекта.
     * @throws Exception
     */
    public void removeProject(
        String id)
            throws Exception;
    
    /**
     * Создает задачу.
     * @param projectId     - идентификатор проекта.
     * @param startDate     - дата начала.
     * @param dueDate       - дата окончания. 
     * @param assignetId    - идентификатор исполнителя.
     * @param subject       - заголовок. 
     * @param description   - описание задачи.
     * @param estimateHours - оценка в часах.
     * @return
     * @throws Exception
     */
    public Issue createIssue(
        int projectId,
        Date startDate,
        Date dueDate,
        String assigneeName,
        String subject,
        String description,
        float estimateHours)
            throws Exception;
    
    /**
     * Обновляет оценку.
     * @param issueId - идентификатор задачи.
     * @param estimateHours - количество часов.
     */
    public void updateIssue(
        int issueId,
        float estimateHours) 
            throws Exception;
    
    /**
     * Обновляет оценку и исполнителя.
     * @param issueId - идентфикатор задачи.
     * @param estimateHours - оценка в часах.
     * @param assigneeName - имя пользователя.
     * @throws Exception
     */
    public void updateIssue(
        int issueId,
        float estimateHours,
        String assigneeName)
            throws Exception;
    
    /**
     * Добавляет файлы.
     * @param issueId - идентификатор задачи
     * @param userName - имя пользователя
     * @param files - список файлов. Первый параметр название файла, второй параметр данные файла.
     * @throws Exception
     */
    public void updateIssue(
        int issueId,
        String userName,
        Map<String, InputStream> files)
            throws Exception;
    
    /**
     * Удаляет задачу.
     * @param id - идентификатор задачи.
     * @throws Exception
     */
    public void removeIssue(
        int id) 
            throws Exception;
        
    /**
     * Добавляет затраченное время.
     * @param issueId - идентификатор задачи.
     * @param userName - имя пользователя.
     * @param activityType - тип деятельности.
     * @param comment - комментарий.
     * @param hours - затраченные часы.
     */
    public TimeEntry addTimeEntry(
        int issueId, 
        String userName, 
        ActivityType activityType, 
        String comment,
        float hours)
            throws Exception;
    
    /**
     * Удаляет запись о затраченном времени.
     * @param id - идентификатор записи.
     * @throws Exception
     */
    public void removeTimeEntry(
        int id) 
            throws Exception;
}
