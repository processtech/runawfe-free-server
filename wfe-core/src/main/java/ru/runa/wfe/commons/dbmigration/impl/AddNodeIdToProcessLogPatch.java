package ru.runa.wfe.commons.dbmigration.impl;

import java.sql.Types;
import java.util.HashMap;
import java.util.List;

import org.hibernate.ScrollMode;
import org.hibernate.ScrollableResults;
import org.hibernate.Session;

import ru.runa.wfe.commons.dbmigration.DbMigration;
import ru.runa.wfe.commons.xml.XmlUtils;

import com.google.common.base.Strings;

/**
 * Adds BPM_LOG.NODE_ID column and fill it from attributes for old records.
 * 
 * @since 4.1.0
 * @author Dofs
 */
public class AddNodeIdToProcessLogPatch extends DbMigration {

    @Override
    protected List<String> getDDLQueriesBefore() {
        List<String> sql = super.getDDLQueriesAfter();
        sql.add(getDDLCreateColumn("BPM_LOG", new ColumnDef("NODE_ID", dialect.getTypeName(Types.VARCHAR, 255, 255, 255), true)));
        return sql;
    }

    @Override
    public void executeDML(Session session) throws Exception {
        String q;
        String inCollection = "('1','2','3','5','9','S','6','N','L','B','Z','7','8')";
        q = "SELECT COUNT(*) FROM BPM_LOG WHERE DISCRIMINATOR IN " + inCollection + " AND CONTENT IS NOT NULL";
        Number oldRecordsCount = (Number) session.createSQLQuery(q).uniqueResult();
        log.info("Processing old logs: " + oldRecordsCount);
        q = "SELECT ID, CONTENT FROM BPM_LOG WHERE DISCRIMINATOR IN " + inCollection + " AND CONTENT IS NOT NULL ORDER BY ID";
        ScrollableResults scrollableResults = session.createSQLQuery(q).scroll(ScrollMode.FORWARD_ONLY);
        int processed = 0;
        int deltaToPrintStatus = 1000;
        while (scrollableResults.next()) {
            Long logId = ((Number) scrollableResults.get(0)).longValue();
            try {
                String content = (String) scrollableResults.get(1);
                HashMap<String, String> attributes = XmlUtils.deserialize(content);
                String nodeId = attributes.get("nodeId");
                if (!Strings.isNullOrEmpty(nodeId)) {
                    q = "UPDATE BPM_LOG SET NODE_ID='" + nodeId + "' WHERE ID=" + logId;
                    session.createSQLQuery(q).executeUpdate();
                }
            } catch (Exception e) {
                log.warn("For " + logId + ": " + e);
            }
            processed++;
            if (processed % deltaToPrintStatus == 0) {
                log.info("Processed " + (100 * processed / oldRecordsCount.intValue()) + "%");
            }
        }
    }

}
