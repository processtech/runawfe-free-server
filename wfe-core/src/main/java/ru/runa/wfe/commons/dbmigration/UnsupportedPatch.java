package ru.runa.wfe.commons.dbmigration;

import org.hibernate.Session;

/**
 * This patch cannot be executed. Used to boundary allowed patches.
 * 
 * @author Dofs
 * 
 */
public class UnsupportedPatch extends DbPatch {

    @Override
    public void executeDML(Session session) throws Exception {
        throw new UnsupportedOperationException("DB update is not supported from your version. Try incremental update. Be sure to make DB backup.");
    }

}
