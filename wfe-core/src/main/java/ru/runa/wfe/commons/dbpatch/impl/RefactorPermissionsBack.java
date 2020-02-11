package ru.runa.wfe.commons.dbpatch.impl;

import ru.runa.wfe.commons.dbpatch.DbPatch;

/**
 * #1586.
 */
public class RefactorPermissionsBack extends DbPatch {

    // TODO
    //      DEFINITIONS.CREATE ---> SYSTEM.CREATE_DEFINITION
    //      DEFINITIONS.* ---> delete
    //      EXECUTORS.CREATE ---> SYSTEM.CREATE_EXECUTOR
    //      EXECUTORS.LOGIN ---> SYSTEM.LOGIN
    //      EXECUTORS.* ---> delete
    //      LOGS.ALL, LIST ---> SYSTEM.READ_LOGS
    //      LOGS.* ---> delete
    //      SUBSTITUTION_CRITERIAS.* ---> delete
    //      SYSTEM.ALL ---> SYSTEM.READ
    //      *.LIST ---> READ
}
