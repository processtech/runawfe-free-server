package ru.runa.wfe.audit.dao;

import org.springframework.stereotype.Component;
import ru.runa.wfe.audit.ArchivedProcessLog;
import ru.runa.wfe.audit.BaseProcessLog;
import ru.runa.wfe.audit.IProcessLog;

@Component
public class ArchivedProcessLogDao extends BaseProcessLogDao<ArchivedProcessLog> {

    @Override
    protected Class<? extends BaseProcessLog> typeToClass(IProcessLog.Type type) {
        return type.archivedRootClass;
    }
}
