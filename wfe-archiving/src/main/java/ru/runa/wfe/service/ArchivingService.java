package ru.runa.wfe.service;

import javax.ejb.Remote;

import ru.runa.wfe.user.User;

/**
 * Service for archiving and restoring processes and definitions to other database
 * 
 * @author m.koshutin
 * 
 */
@Remote
public interface ArchivingService {

    /**
     * Backup process by id.
     * 
     * @param user
     * @param processId
     */
    public void backupProcess(User user, Long processId);

    /**
     * Backup process definition by definition name and version
     * 
     * @param user
     * @param definitionName
     * @param version
     */
    public void backupProcessDefinition(User user, String definitionName, Long version);

    /**
     * Restore process by process id.
     * 
     * @param user
     * @param processId
     */
    public void restoreProcess(User user, Long processId);

    /**
     * Restore process definition by definition name and version
     * 
     * @param user
     * @param definitionName
     * @param version
     */
    public void restoreProcessDefinition(User user, String definitionName, Long version);
}
