package ru.runa.wfe.commons.dbmigration;

import org.hibernate.Session;

/**
 * This migration cannot be executed. Used to boundary allowed migrations.
 * 
 * @author Dofs
 *
 * @deprecated TODO Remove in WFE 5, not needed with DB_MIGRATION table.
 */
@Deprecated
public class UnsupportedPatch extends DbMigration {

    @Override
    public void executeDML(Session session) {
        throw new UnsupportedOperationException("DB update is not supported from your version. Try incremental update. Be sure to make DB backup.");
    }
}
