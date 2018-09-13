package ru.runa.wfe.commons.dbpatch.impl;

import com.google.common.collect.Lists;
import java.sql.Types;
import java.util.LinkedList;
import java.util.List;
import org.springframework.beans.factory.annotation.Autowired;
import ru.runa.wfe.commons.SystemProperties;
import ru.runa.wfe.commons.dbpatch.DBPatch;
import ru.runa.wfe.commons.dbpatch.IDbPatchPostProcessor;
import ru.runa.wfe.report.ReportDefinition;
import ru.runa.wfe.report.ReportParameter;
import ru.runa.wfe.security.SecuredObjectType;
import ru.runa.wfe.security.dao.PermissionDAO;
import ru.runa.wfe.user.Actor;
import ru.runa.wfe.user.Executor;
import ru.runa.wfe.user.Group;
import ru.runa.wfe.user.dao.ExecutorDao;

public class CreateReportsTables extends DBPatch implements IDbPatchPostProcessor {

    @Autowired
    protected ExecutorDao executorDao;
    @Autowired
    protected PermissionDAO permissionDAO;

    @Override
    protected List<String> getDDLQueriesBefore() {
        List<String> sql = super.getDDLQueriesBefore();
        sql.addAll(createReportParametersTable());
        sql.addAll(createReportsTable());
        sql.add(getDDLCreateForeignKey("REPORT_PARAMETER", "FK_REPORT_PARAMETER_REPORT", "REPORT_ID", "REPORT", "ID"));
        return sql;
    }

    /**
     * Creates table, indexes e.t.c for {@link ReportParameter}.
     * 
     * @return Returns list of sql commands for table creation.
     */
    private List<String> createReportParametersTable() {
        List<String> sql = new LinkedList<>();
        List<ColumnDef> columns = new LinkedList<>();
        ColumnDef id = new ColumnDef("ID", Types.BIGINT, false);
        id.setPrimaryKey();
        columns.add(id);
        columns.add(new ColumnDef("REPORT_ID", Types.BIGINT, false));
        columns.add(new ColumnDef("NAME", dialect.getTypeName(Types.VARCHAR, 1024, 1024, 1024), false));
        columns.add(new ColumnDef("TYPE", dialect.getTypeName(Types.VARCHAR, 1024, 1024, 1024), false));
        columns.add(new ColumnDef("INNER_NAME", dialect.getTypeName(Types.VARCHAR, 1024, 1024, 1024), false));
        columns.add(new ColumnDef("REQUIRED", dialect.getTypeName(Types.BIT), false));
        sql.add(getDDLCreateTable("REPORT_PARAMETER", columns, null));
        sql.add(getDDLCreateSequence("SEQ_REPORT_PARAMETER"));
        sql.add(getDDLCreateIndex("REPORT_PARAMETER", "IX_PARAMETER_REPORT_ID", "REPORT_ID"));
        return sql;
    }

    /**
     * Creates table, indexes e.t.c for {@link ReportDefinition}.
     * 
     * @return Returns list of sql commands for table creation.
     */
    private List<String> createReportsTable() {
        List<String> sql = new LinkedList<>();
        List<ColumnDef> columns = new LinkedList<>();
        ColumnDef id = new ColumnDef("ID", Types.BIGINT, false);
        id.setPrimaryKey();
        columns.add(id);
        columns.add(new ColumnDef("VERSION", Types.BIGINT, false));
        columns.add(new ColumnDef("NAME", dialect.getTypeName(Types.VARCHAR, 1024, 1024, 1024), false));
        columns.add(new ColumnDef("DESCRIPTION", dialect.getTypeName(Types.VARCHAR, 2048, 2048, 2048), true));
        columns.add(new ColumnDef("COMPILED_REPORT", dialect.getTypeName(Types.BLOB), false));
        columns.add(new ColumnDef("CONFIG_TYPE", dialect.getTypeName(Types.VARCHAR, 1024, 1024, 1024), false));
        columns.add(new ColumnDef("CATEGORY", dialect.getTypeName(Types.VARCHAR, 1024, 1024, 1024), true));
        sql.add(getDDLCreateTable("REPORT", columns, null));
        sql.add(getDDLCreateSequence("SEQ_REPORT"));
        sql.add(getDDLCreateUniqueKey("REPORT", "IX_REPORT_NAME", "NAME"));
        return sql;
    }

    @Override
    public void postExecute() {
        if (permissionDAO.getPrivilegedExecutors(SecuredObjectType.REPORT).isEmpty()) {
            log.info("Adding " + SecuredObjectType.REPORT + " tokens message hash");
            String administratorName = SystemProperties.getAdministratorName();
            Actor admin = executorDao.getActor(administratorName);
            String administratorsGroupName = SystemProperties.getAdministratorsGroupName();
            Group adminGroup = executorDao.getGroup(administratorsGroupName);
            List<? extends Executor> adminWithGroupExecutors = Lists.newArrayList(adminGroup, admin);
            permissionDAO.addType(SecuredObjectType.REPORT, adminWithGroupExecutors);
        }
    }
}
