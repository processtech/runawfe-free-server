package ru.runa.wfe.commons.dbmigration.impl;

import com.google.common.collect.Lists;
import java.sql.Types;
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
        executeDDL(getDDLCreateForeignKey("REPORT_PARAMETER", "FK_REPORT_PARAMETER_REPORT", "REPORT_ID", "REPORT", "ID"));
    }

    /**
     * Creates table, indexes e.t.c for {@link ReportParameter}.
     */
    private void createReportParametersTable() {
        executeDDL(
                getDDLCreateTable("REPORT_PARAMETER", list(
                        new ColumnDef("ID", Types.BIGINT, false).setPrimaryKey(),
                        new ColumnDef("REPORT_ID", Types.BIGINT, false),
                        new ColumnDef("NAME", dialect.getTypeName(Types.VARCHAR, 1024, 1024, 1024), false),
                        new ColumnDef("TYPE", dialect.getTypeName(Types.VARCHAR, 1024, 1024, 1024), false),
                        new ColumnDef("INNER_NAME", dialect.getTypeName(Types.VARCHAR, 1024, 1024, 1024), false),
                        new ColumnDef("REQUIRED", dialect.getTypeName(Types.BIT), false)
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
        executeDDL(
                getDDLCreateTable("REPORT", list(
                        new ColumnDef("ID", Types.BIGINT, false).setPrimaryKey(),
                        new ColumnDef("VERSION", Types.BIGINT, false),
                        new ColumnDef("NAME", dialect.getTypeName(Types.VARCHAR, 1024, 1024, 1024), false),
                        new ColumnDef("DESCRIPTION", dialect.getTypeName(Types.VARCHAR, 2048, 2048, 2048), true),
                        new ColumnDef("COMPILED_REPORT", dialect.getTypeName(Types.BLOB), false),
                        new ColumnDef("CONFIG_TYPE", dialect.getTypeName(Types.VARCHAR, 1024, 1024, 1024), false),
                        new ColumnDef("CATEGORY", dialect.getTypeName(Types.VARCHAR, 1024, 1024, 1024), true)
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
