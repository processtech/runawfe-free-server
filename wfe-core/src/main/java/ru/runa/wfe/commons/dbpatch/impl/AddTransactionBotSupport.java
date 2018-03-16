package ru.runa.wfe.commons.dbpatch.impl;

import java.sql.Types;
import java.util.List;

import com.google.common.collect.Lists;

import ru.runa.wfe.commons.dbpatch.DBPatch;

public class AddTransactionBotSupport extends DBPatch {
    private static final String TABLE_NAME = "BOT";
    private static final List<Pair<String, Integer>> COLUMNS_LIST = Lists.newArrayList();

    static {
        COLUMNS_LIST.add(new Pair<>("IS_TRANSACTIONAL", Types.BOOLEAN));
        COLUMNS_LIST.add(new Pair<>("TIMEOUT", Types.BIGINT));
        COLUMNS_LIST.add(new Pair<>("BOT_TIMEOUT", Types.TIMESTAMP));
        COLUMNS_LIST.add(new Pair<>("PROCESS_ID", Types.BIGINT));
        COLUMNS_LIST.add(new Pair<>("SUBPROCESS_ID", Types.VARCHAR));
    }

    @Override
    protected List<String> getDDLQueriesBefore() {
        List<String> sql = super.getDDLQueriesAfter();
        for (Pair<String, Integer> item : COLUMNS_LIST) {
            sql.add(getDDLCreateColumn(TABLE_NAME, new ColumnDef(item.getFirst(), dialect.getTypeName(item.getSecond()))));
        }

        return sql;
    }

    private static class Pair<X, Y> {
        private X first;
        private Y second;

        Pair(X first, Y second) {
            this.first = first;
            this.second = second;
        }

        public X getFirst() {
            return first;
        }

        public Y getSecond() {
            return second;
        }

    }
}
