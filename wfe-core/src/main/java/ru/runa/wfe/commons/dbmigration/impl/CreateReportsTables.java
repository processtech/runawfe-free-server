package ru.runa.wfe.commons.dbmigration.impl;

import com.google.common.collect.Lists;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import ru.runa.wfe.commons.SystemProperties;
import ru.runa.wfe.commons.dbmigration.DbMigration;
import ru.runa.wfe.commons.dbmigration.DbMigrationPostProcessor;
import ru.runa.wfe.report.ReportDefinition;
import ru.runa.wfe.report.ReportParameter;
import ru.runa.wfe.security.SecuredObjectType;
import ru.runa.wfe.security.dao.PermissionDao;
import ru.runa.wfe.user.Actor;
import ru.runa.wfe.user.Executor;
import ru.runa.wfe.user.Group;
import ru.runa.wfe.user.dao.ExecutorDao;

public class CreateReportsTables extends DbMigration implements DbMigrationPostProcessor {

    @Autowired
    protected ExecutorDao executorDao;
    @Autowired
    protected PermissionDao permissionDao;

    @Override
    protected void executeDDLBefore() {
        createReportParametersTable();
        createReportsTable();
        executeUpdates(getDDLCreateForeignKey("REPORT_PARAMETER", "FK_REPORT_PARAMETER_REPORT", "REPORT_ID", "REPORT", "ID"));
    }

    /**
     * Creates table, indexes e.t.c for {@link ReportParameter}.
     */
    private void createReportParametersTable() {
        executeUpdates(
                getDDLCreateTable("REPORT_PARAMETER", list(
                        new BigintColumnDef("ID").primaryKey(),
                        new BigintColumnDef("REPORT_ID").notNull(),
                        new VarcharColumnDef("NAME", 1024).notNull(),
                        new VarcharColumnDef("TYPE", 1024).notNull(),
                        new VarcharColumnDef("INNER_NAME", 1024).notNull(),
                        new BooleanColumnDef("REQUIRED").notNull()
                )),
                getDDLCreateSequence("SEQ_REPORT_PARAMETER"),
                getDDLCreateIndex("REPORT_PARAMETER", "IX_PARAMETER_REPORT_ID", "REPORT_ID")
        );
    }

    /**
     * Creates table, indexes e.t.c for {@link ReportDefinition}.
     * 
     * @return Returns list of sql commands for table creation.
     */
    private void createReportsTable() {
        executeUpdates(
                getDDLCreateTable("REPORT", list(
                        new BigintColumnDef("ID").primaryKey(),
                        new BigintColumnDef("VERSION").notNull(),
                        new VarcharColumnDef("NAME", 1024).notNull(),
                        new VarcharColumnDef("DESCRIPTION", 2048),
                        new BlobColumnDef("COMPILED_REPORT").notNull(),
                        new VarcharColumnDef("CONFIG_TYPE", 1024).notNull(),
                        new VarcharColumnDef("CATEGORY", 1024)
                )),
                getDDLCreateSequence("SEQ_REPORT"),
                getDDLCreateUniqueKey("REPORT", "IX_REPORT_NAME", "NAME")
        );
    }

    @Override
    public void postExecute() {
        if (permissionDao.getPrivilegedExecutors(SecuredObjectType.REPORT).isEmpty()) {
            log.info("Adding " + SecuredObjectType.REPORT + " tokens message hash");
            String administratorName = SystemProperties.getAdministratorName();
            Actor admin = executorDao.getActor(administratorName);
            String administratorsGroupName = SystemProperties.getAdministratorsGroupName();
            Group adminGroup = executorDao.getGroup(administratorsGroupName);
            List<? extends Executor> adminWithGroupExecutors = Lists.newArrayList(adminGroup, admin);
            permissionDao.addType(SecuredObjectType.REPORT, adminWithGroupExecutors);
        }
    }
}
