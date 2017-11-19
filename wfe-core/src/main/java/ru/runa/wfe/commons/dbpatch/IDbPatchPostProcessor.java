package ru.runa.wfe.commons.dbpatch;

import org.hibernate.Session;

/**
 * Extension interface for {@link DBPatch}.
 * 
 * @author Dofs
 * 
 */
public interface IDbPatchPostProcessor {

    /**
     * Called after all patches are applied.
     * 
     * Hibernate usage allowed here.
     * 
     * Error in this method may leave database in inconsistent state, use with caution.
     */
    void postExecute(Session session) throws Exception;

}
