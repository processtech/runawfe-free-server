package ru.runa.wfe.presentation.hibernate;

import java.util.Collection;

import ru.runa.wfe.InternalApplicationException;
import ru.runa.wfe.task.Task;

/**
 * Restrictions for loaded objects owner's. For example we want to load only our tasks or only tasks by other user.
 */
public class RestrictionsToOwners {
    /**
     * Collection of owners id (Long for example). If this collection and ownersDBPath is set, when only objects, which has specified owners will be
     * queried.
     */
    private final Collection<?> owners;

    /**
     * HQL path from root object to calculate object owner (actorId for {@link Task} for example).
     */
    private final String ownersDBPath;

    public RestrictionsToOwners(Collection<?> owners, String ownersDBPath) {
        super();
        this.owners = owners;
        this.ownersDBPath = ownersDBPath;
        if (ownersDBPath != null && owners == null) {
            throw new InternalApplicationException("No owners supplied to query with owner restrictions.");
        }
    }

    public Collection<?> getOwners() {
        return owners;
    }

    public String getOwnersDBPath() {
        return ownersDBPath;
    }
}
