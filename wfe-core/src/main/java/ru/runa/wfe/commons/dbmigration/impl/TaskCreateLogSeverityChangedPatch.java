package ru.runa.wfe.commons.dbmigration.impl;

import org.hibernate.SQLQuery;
import org.hibernate.Session;
import ru.runa.wfe.audit.Severity;
import ru.runa.wfe.commons.dbmigration.DbMigration;

public class TaskCreateLogSeverityChangedPatch extends DbMigration {

    @Override
    public void executeDML(Session session) {
        SQLQuery updateQuery = session.createSQLQuery("UPDATE BPM_LOG SET SEVERITY=:severity WHERE DISCRIMINATOR='1'");
        updateQuery.setString("severity", Severity.INFO.name());
        updateQuery.executeUpdate();
    }
}
