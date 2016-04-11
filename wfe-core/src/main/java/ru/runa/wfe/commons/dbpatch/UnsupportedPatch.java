package ru.runa.wfe.commons.dbpatch;

import org.hibernate.Session;

public class UnsupportedPatch extends DBPatch {
    private final String message;

    public UnsupportedPatch(String message) {
        this.message = message;
    }

    public UnsupportedPatch() {
        this("DB update is not supported from your version. Try incremental update. Be sure to make DB backup.");
    }

    @Override
    public void applyPatch(Session session) throws Exception {
        throw new UnsupportedOperationException(message);
    }

}
