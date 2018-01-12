package ru.runa.wfe.commons.dbpatch;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;
import ru.runa.wfe.commons.dao.ConstantDAO;

// TODO until DBPatch subclasses are not transacted spring components
public class DbPatchTransactionaExecutor {
    @Autowired
    private ConstantDAO constantDAO;

    @Transactional
    public void execute(DBPatch dbPatch, int databaseVersion) throws Exception {
        dbPatch.execute();
        constantDAO.setDatabaseVersion(databaseVersion);
    }

    @Transactional
    public void postExecute(IDbPatchPostProcessor dbPatch) throws Exception {
        dbPatch.postExecute();
    }

}