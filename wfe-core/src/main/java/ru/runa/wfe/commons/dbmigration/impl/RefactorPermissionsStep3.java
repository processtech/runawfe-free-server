package ru.runa.wfe.commons.dbmigration.impl;

import org.hibernate.Session;
import ru.runa.wfe.commons.dbmigration.DbMigration;

/**
 * Adjusts object_type and permission values for rm660, see https://rm.processtech.ru/attachments/download/1210.
 */
public class RefactorPermissionsStep3 extends DbMigration {

    /**
     * Implementation was moved to RefactorPermissionsBack.executeDML_step3() and edited.
     * See #1586, #1586-10.
     */
    @Override
    public void executeDML(Session session) {
    }
}
