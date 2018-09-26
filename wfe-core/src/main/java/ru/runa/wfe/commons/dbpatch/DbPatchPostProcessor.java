package ru.runa.wfe.commons.dbpatch;


/**
 * Extension interface for {@link DbPatch}.
 * 
 * @author Dofs
 * 
 */
public interface DbPatchPostProcessor {

    /**
     * Called after all patches are applied.
     * 
     * Hibernate usage allowed here.
     * 
     * Error in this method may leave database in inconsistent state, use with caution.
     */
    void postExecute() throws Exception;

}
