package ru.runa.wfe.commons.dbpatch.impl;

import java.util.ArrayList;
import java.util.List;
import ru.runa.wfe.commons.dbpatch.DbPatch;

public class AddProcessClientData extends DbPatch {

    @Override
    protected List<String> getDDLQueriesBefore() {
        return new ArrayList<String>() {{
            add(getDDLCreateColumn("bpm_process", new BigintColumnDef("client_data", true)));
            add(getDDLCreateIndex("bpm_process", "ix_process_client_data", "client_data"));
        }};
    }
}
