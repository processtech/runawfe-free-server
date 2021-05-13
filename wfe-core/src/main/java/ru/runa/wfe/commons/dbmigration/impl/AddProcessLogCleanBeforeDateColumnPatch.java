package ru.runa.wfe.commons.dbmigration.impl;

import com.google.common.base.Strings;
import java.util.HashMap;
import org.hibernate.ScrollMode;
import org.hibernate.ScrollableResults;
import org.hibernate.Session;
import ru.runa.wfe.commons.dbmigration.DbMigration;
import ru.runa.wfe.commons.xml.XmlUtils;

/**
 * Adds SYSTEM_LOG.PROCESS_LOG_CLEAN_BEFORE_DATE column
 * 
 * @since 4.4.3
 * @author vromav
 */
public class AddProcessLogCleanBeforeDateColumnPatch extends DbMigration {

    @Override
    protected void executeDDLBefore() {
        executeUpdates(getDDLCreateColumn("SYSTEM_LOG", new TimestampColumnDef("PROCESS_LOG_CLEAN_BEFORE_DATE")));
    }
}
