package ru.runa.wfe.commons.dbmigration.impl;

import com.google.common.collect.Lists;
import java.sql.Types;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import org.hibernate.SQLQuery;
import org.hibernate.Session;
import ru.runa.wfe.commons.CalendarUtil;
import ru.runa.wfe.commons.dbmigration.DbMigration;

public class AddCreateDateColumns extends DbMigration {
    private static final String COLUMN_CREATE_DATE = "CREATE_DATE";
    private static final List<String> TABLES_TO_ADD_COLUMN = Lists.newArrayList();
    static {
        TABLES_TO_ADD_COLUMN.add("BATCH_PRESENTATION");
        TABLES_TO_ADD_COLUMN.add("BOT");
        TABLES_TO_ADD_COLUMN.add("BOT_STATION");
        TABLES_TO_ADD_COLUMN.add("BOT_TASK");
        TABLES_TO_ADD_COLUMN.add("BPM_JOB");
        TABLES_TO_ADD_COLUMN.add("BPM_SUBPROCESS");
        TABLES_TO_ADD_COLUMN.add("BPM_SWIMLANE");
        TABLES_TO_ADD_COLUMN.add("BPM_VARIABLE");
        TABLES_TO_ADD_COLUMN.add("EXECUTOR");
        TABLES_TO_ADD_COLUMN.add("EXECUTOR_GROUP_MEMBER");
        TABLES_TO_ADD_COLUMN.add("EXECUTOR_RELATION");
        TABLES_TO_ADD_COLUMN.add("EXECUTOR_RELATION_PAIR");
        TABLES_TO_ADD_COLUMN.add("LOCALIZATION");
        TABLES_TO_ADD_COLUMN.add("PROFILE");
        TABLES_TO_ADD_COLUMN.add("SUBSTITUTION");
        TABLES_TO_ADD_COLUMN.add("SUBSTITUTION_CRITERIA");
        // TABLES_TO_ADD_COLUMN.add("WFE_CONSTANTS");
    }

    @Override
    protected void executeDDLBefore() {
        executeUpdates(
                getDDLRenameColumn("BPM_LOG", "LOG_DATE", new ColumnDef(COLUMN_CREATE_DATE, Types.TIMESTAMP)),
                getDDLRenameColumn("BPM_PROCESS_DEFINITION", "DEPLOYED", new ColumnDef(COLUMN_CREATE_DATE, Types.TIMESTAMP)),
                getDDLRenameColumn("SYSTEM_LOG", "TIME", new ColumnDef(COLUMN_CREATE_DATE, Types.TIMESTAMP))
        );
        for (String tableName : TABLES_TO_ADD_COLUMN) {
            executeUpdates(getDDLCreateColumn(tableName, new ColumnDef(COLUMN_CREATE_DATE, Types.TIMESTAMP)));
        }
    }

    @Override
    public void executeDML(Session session) {
        Calendar fakeCreateCalendar = CalendarUtil.getZeroTimeCalendar(Calendar.getInstance());
        fakeCreateCalendar.set(Calendar.DAY_OF_YEAR, 1);
        Date fakeCreateDate = fakeCreateCalendar.getTime();
        List<String> tablesToUpdateNullCreateDate = Lists.newArrayList(TABLES_TO_ADD_COLUMN);
        tablesToUpdateNullCreateDate.add("BPM_PROCESS_DEFINITION");
        for (String tableName : tablesToUpdateNullCreateDate) {
            String sql = "UPDATE " + tableName + " SET " + COLUMN_CREATE_DATE + "=:createDate WHERE " + COLUMN_CREATE_DATE + " IS NULL";
            SQLQuery query = session.createSQLQuery(sql);
            query.setTimestamp("createDate", fakeCreateDate);
            int rowsUpdated = query.executeUpdate();
            log.info("'" + sql + "' executed for " + rowsUpdated + " rows");
        }
    }
}
