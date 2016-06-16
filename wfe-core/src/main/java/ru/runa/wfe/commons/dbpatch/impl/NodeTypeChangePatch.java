package ru.runa.wfe.commons.dbpatch.impl;

import java.util.Map;

import org.hibernate.SQLQuery;
import org.hibernate.ScrollMode;
import org.hibernate.ScrollableResults;
import org.hibernate.Session;

import ru.runa.wfe.commons.dbpatch.DBPatch;
import ru.runa.wfe.lang.NodeType;

import com.google.common.collect.Maps;

public class NodeTypeChangePatch extends DBPatch {
    private static final Map<String, String> CHANGES = Maps.newHashMap();
    static {
        CHANGES.put("StartNode", NodeType.START_EVENT.name());
        CHANGES.put("ActionNode", NodeType.ACTION_NODE.name());
        CHANGES.put("End", NodeType.END_PROCESS.name());
        CHANGES.put("WaitNode", NodeType.WAIT_STATE.name());
        CHANGES.put("TAST_STATE", NodeType.TASK_STATE.name());
        CHANGES.put("TaskNode", NodeType.TASK_STATE.name());
        CHANGES.put("Fork", NodeType.FORK.name());
        CHANGES.put("Join", NodeType.JOIN.name());
        CHANGES.put("Decision", NodeType.DECISION.name());
        CHANGES.put("Subprocess", NodeType.SUBPROCESS.name());
        CHANGES.put("MultiSubprocess", NodeType.MULTI_SUBPROCESS.name());
        CHANGES.put("SendMessageNode", NodeType.SEND_MESSAGE.name());
        CHANGES.put("ReceiveMessageNode", NodeType.RECEIVE_MESSAGE.name());
        CHANGES.put("EndToken", NodeType.END_TOKEN.name());
        CHANGES.put("MultiTaskNode", NodeType.MULTI_TASK_STATE.name());
        CHANGES.put("Merge", NodeType.MERGE.name());
    }

    @Override
    protected void applyPatch(Session session) throws Exception {
        for (Map.Entry<String, String> entry : CHANGES.entrySet()) {
            SQLQuery updateQuery = session.createSQLQuery("UPDATE BPM_TOKEN SET NODE_TYPE=:newNodeType WHERE NODE_TYPE=:oldNodeType");
            updateQuery.setString("oldNodeType", entry.getKey());
            updateQuery.setString("newNodeType", entry.getValue());
            updateQuery.executeUpdate();
        }
        for (Map.Entry<String, String> entry : CHANGES.entrySet()) {
            String needle = "<nodeType>" + entry.getKey() + "</nodeType>";
            String replacement = "<nodeType>" + entry.getValue() + "</nodeType>";
            String query = "SELECT ID, CONTENT FROM BPM_LOG WHERE CONTENT LIKE '%" + needle + "%'";
            ScrollableResults scrollableResults = session.createSQLQuery(query).scroll(ScrollMode.FORWARD_ONLY);
            while (scrollableResults.next()) {
                String xml = (String) scrollableResults.get(1);
                xml = xml.replaceAll(needle, replacement);
                SQLQuery updateQuery = session.createSQLQuery("UPDATE BPM_LOG SET CONTENT=:xml WHERE ID=:id");
                updateQuery.setParameter("id", scrollableResults.get(0));
                updateQuery.setString("xml", xml);
                updateQuery.executeUpdate();
            }
        }
    }

}
