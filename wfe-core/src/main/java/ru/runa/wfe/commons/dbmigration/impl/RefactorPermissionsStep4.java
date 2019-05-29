package ru.runa.wfe.commons.dbmigration.impl;

import org.hibernate.Session;
import ru.runa.wfe.commons.dbmigration.DbMigration;

/**
 * Types ACTOR and GROUP are merged into EXECUTOR for rm718
 */
public class RefactorPermissionsStep4 extends DbMigration {

    @Override
    public void executeDML(Session session) {
        // Replace ACTOR and GROUP types with EXECUTOR in permission_mapping
        // Delete ACTOR and GROUP from priveleged_mapping
        {
            session.createSQLQuery("delete from permission_mapping where object_type = 'EXECUTOR'").executeUpdate();
            session.createSQLQuery("update permission_mapping set object_type = 'EXECUTOR' where object_type = 'ACTOR' or object_type = 'GROUP'")
                    .executeUpdate();
            // session.createSQLQuery("delete from priveleged_mapping where type = 'EXECUTOR'").executeUpdate();
            session.createSQLQuery("delete from priveleged_mapping where type = 'GROUP'").executeUpdate();
            session.createSQLQuery("update priveleged_mapping set type = 'EXECUTOR' where type = 'ACTOR'").executeUpdate();
        }
    }
}
