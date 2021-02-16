package ru.runa.wfe.commons.dbmigration.impl;

import org.hibernate.Session;
import ru.runa.wfe.commons.dbmigration.DbMigration;

/**
 * Types ACTOR and GROUP are merged into EXECUTOR for rm718
 */
public class RefactorPermissionsStep4 extends DbMigration {

    /**
     * Implementation was moved to RefactorPermissionsBack.executeDML_step4() unchanged.
     * See #1586, #1586-10.
     */
    @Override
    public void executeDML(Session session) {
    }
}
