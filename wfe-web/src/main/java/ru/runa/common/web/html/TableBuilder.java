package ru.runa.common.web.html;

import org.apache.ecs.html.TR;
import org.apache.ecs.html.Table;

import ru.runa.common.web.Resources;
import ru.runa.wfe.InternalApplicationException;

/**
 * Created on 10.11.2004
 * 
 */
public class TableBuilder {
    static final int indefiniteLoopCheckerMaxRows = 100000;

    public Table build(HeaderBuilder headerBuilder, RowBuilder rowBuilder) {
        return build(headerBuilder, rowBuilder, false);
    }

    public Table build(HeaderBuilder headerBuilder, RowBuilder rowBuilder, boolean buildArray) {
        Table table = new Table();
        table.setClass(Resources.CLASS_LIST_TABLE);
        table.addElement(headerBuilder.build());
        int rowsCount = 0;
        if (buildArray) {
            while (rowBuilder.hasNext()) {
                rowsCount++;
                for (TR tr : rowBuilder.buildNextArray()) {
                    table.addElement(tr);
                    rowsCount++;
                }
                if (rowsCount > indefiniteLoopCheckerMaxRows) {
                    throw new InternalApplicationException("Indefinite loop detected");
                }
            }
        } else {
            while (rowBuilder.hasNext()) {
                rowsCount++;
                table.addElement(rowBuilder.buildNext());
                if (rowsCount > indefiniteLoopCheckerMaxRows) {
                    throw new InternalApplicationException("Indefinite loop detected");
                }
            }
        }
        return table;
    }
}
