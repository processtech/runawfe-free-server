package ru.runa.wfe.commons.dbmigration.impl;

import java.sql.Types;
import java.util.List;

import org.hibernate.SQLQuery;
import org.hibernate.Session;

import ru.runa.wfe.commons.dbmigration.DbMigration;

public class AddSequentialFlagToBot extends DbMigration {

    @Override
    protected List<String> getDDLQueriesBefore() {
        List<String> sql = super.getDDLQueriesBefore();
        sql.add(getDDLCreateColumn("BOT_TASK", new ColumnDef("IS_SEQUENTIAL", dialect.getTypeName(Types.BIT))));
        sql.add(getDDLCreateColumn("BOT", new ColumnDef("IS_SEQUENTIAL", dialect.getTypeName(Types.BIT))));
        return sql;
    }

    @Override
    public void executeDML(Session session) throws Exception {
        for (String table : new String[] { "BOT", "BOT_TASK" }) {
            String sql = "UPDATE " + table + " SET IS_SEQUENTIAL=:isSeq WHERE IS_SEQUENTIAL IS NULL";
            SQLQuery query = session.createSQLQuery(sql);
            query.setBoolean("isSeq", false);
            query.executeUpdate();
        }
    }
}
