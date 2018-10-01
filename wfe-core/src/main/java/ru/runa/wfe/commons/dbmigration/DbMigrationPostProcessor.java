package ru.runa.wfe.commons.dbmigration;


/**
 * Extension interface for {@link DbMigration}.
 * 
 * @author Dofs
 *
 * @deprecated Both execution after other (arbitrary, unknown) migrations and using app logic make migration non-deterministic.
 */
@Deprecated
public interface DbMigrationPostProcessor {

    /**
     * Called after all migrations are applied.
     * 
     * Hibernate usage allowed here.
     * 
     * Error in this method may leave database in inconsistent state, use with caution.
     */
    void postExecute() throws Exception;
}
