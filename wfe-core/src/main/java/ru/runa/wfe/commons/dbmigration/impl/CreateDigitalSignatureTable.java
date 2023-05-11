package ru.runa.wfe.commons.dbmigration.impl;

import ru.runa.wfe.commons.dbmigration.DbMigration;

public class CreateDigitalSignatureTable  extends DbMigration {

    @Override
    protected void executeDDLBefore() {
        executeUpdates(
            getDDLCreateTable("digital_signature", list(
                    new BigintColumnDef("ID").primaryKey(),
                    new BigintColumnDef("ACTOR_ID"),
                    new BlobColumnDef("CONTAINER"))
            ), getDDLCreateSequence("SEQ_DIGITAL_SIGNATURE"),
                getDDLCreateIndex("digital_signature", "ix_digital_signature_actor_id", "actor_id")
        );
    }
}