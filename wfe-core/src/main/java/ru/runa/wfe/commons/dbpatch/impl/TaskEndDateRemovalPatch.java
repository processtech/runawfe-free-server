package ru.runa.wfe.commons.dbpatch.impl;

import java.util.List;

import org.hibernate.Session;

import ru.runa.wfe.commons.dbpatch.DBPatch;

import com.google.common.collect.Lists;

public class TaskEndDateRemovalPatch extends DBPatch {

    @Override
    protected void applyPatch(Session session) throws Exception {
        log.info("Deleted completed tasks: " + session.createSQLQuery("DELETE FROM BPM_TASK WHERE END_DATE IS NOT NULL").executeUpdate());
    }

    @Override
    protected List<String> getDDLQueriesAfter() {
        List<String> sql = Lists.newArrayList();
        sql.add(getDDLRemoveColumn("BPM_TASK", "END_DATE"));
        return sql;
    }
}
