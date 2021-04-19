package ru.runa.wfe.commons.dbmigration.impl;

import ru.runa.wfe.commons.dbmigration.DbMigration;

/**
 * @since 04.11.2020
 * @author rumpel.sh
 */
public class CreateStatisticReportTable extends DbMigration {

    @Override
    protected void executeDDLBefore() {
        executeUpdates(
                getDDLCreateTable("STATISTIC_REPORT_LOG", list(
                        new BigintColumnDef("ID").primaryKey(),
                        new VarcharColumnDef("VERSION", 1024).notNull(),
                        new VarcharColumnDef("UUID", 1024).notNull(),
                        new TimestampColumnDef("CREATE_DATE").notNull(),
                        new BooleanColumnDef("IS_SUCCESS_EXECUTION").notNull()
                )),
                getDDLCreateSequence("SEQ_STATISTIC_REPORT_LOG")
        );
    }
}
