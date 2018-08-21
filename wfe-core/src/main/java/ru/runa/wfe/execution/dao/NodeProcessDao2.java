package ru.runa.wfe.execution.dao;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import ru.runa.wfe.commons.dao.GenericDao2;
import ru.runa.wfe.execution.ArchivedNodeProcess;
import ru.runa.wfe.execution.BaseNodeProcess;
import ru.runa.wfe.execution.NodeProcess;

@Component
public class NodeProcessDao2 extends GenericDao2<BaseNodeProcess, NodeProcess, NodeProcessDao, ArchivedNodeProcess, ArchivedNodeProcessDao> {

    @Autowired
    protected NodeProcessDao2(NodeProcessDao dao1, ArchivedNodeProcessDao dao2) {
        super(dao1, dao2);
    }
}
