package ru.runa.wfe.commons.dbpatch;

import org.hibernate.Session;

public class UnsupportedPatch extends DBPatch {

    @Override
    public void applyPatch(Session session) throws Exception {
        throw new UnsupportedOperationException("DB update is not supported from your version. Try incremental update. Be sure to make DB backup.");
    }

}
