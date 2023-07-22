package ru.runa.common.web.html;

import java.util.Iterator;
import java.util.List;

import org.apache.ecs.html.TR;

public class TrRowBuilder implements RowBuilder {
    private final Iterator<TR> rows;

    public TrRowBuilder(List<TR> rows) {
        this.rows = rows.iterator();
    }

    @Override
    public TR buildNext() {
        return rows.next();
    }

    @Override
    public boolean hasNext() {
        return rows.hasNext();
    }

    @Override
    public List<TR> buildNextArray() {
        return null;
    }

}
