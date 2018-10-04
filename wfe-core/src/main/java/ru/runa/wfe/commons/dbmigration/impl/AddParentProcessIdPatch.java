package ru.runa.wfe.commons.dbmigration.impl;

import java.sql.Types;
import org.hibernate.SQLQuery;
import org.hibernate.ScrollMode;
import org.hibernate.ScrollableResults;
import org.hibernate.Session;
import ru.runa.wfe.commons.dbmigration.DbMigration;
import ru.runa.wfe.execution.ProcessHierarchyUtils;

public class AddParentProcessIdPatch extends DbMigration {

    @Override
    protected void executeDDLBefore() {
        executeUpdates(getDDLCreateColumn("BPM_PROCESS", new ColumnDef("PARENT_ID", Types.BIGINT)));
    }

    @Override
    public void executeDML(Session session) {
        ScrollableResults scrollableResults = session.createSQLQuery("SELECT ID, TREE_PATH FROM BPM_PROCESS").scroll(ScrollMode.FORWARD_ONLY);
        SQLQuery query = session.createSQLQuery("UPDATE BPM_PROCESS SET PARENT_ID = :parentId WHERE ID = :id");
        while (scrollableResults.next()) {
            Long processId = ((Number) scrollableResults.get(0)).longValue();
            String hierarchyIds = (String) scrollableResults.get(1);
            Long parentId = ProcessHierarchyUtils.getParentProcessId(hierarchyIds);
            if (parentId != null) {
                query.setParameter("id", processId);
                query.setParameter("parentId", parentId);
                query.executeUpdate();
                log.debug("updated process " + processId);
            }
        }
    }
}
