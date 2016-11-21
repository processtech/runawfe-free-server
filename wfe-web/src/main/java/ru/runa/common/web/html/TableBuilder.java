/*
 * This file is part of the RUNA WFE project.
 * 
 * This program is free software; you can redistribute it and/or 
 * modify it under the terms of the GNU Lesser General Public License 
 * as published by the Free Software Foundation; version 2.1 
 * of the License. 
 * 
 * This program is distributed in the hope that it will be useful, 
 * but WITHOUT ANY WARRANTY; without even the implied warranty of 
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the 
 * GNU Lesser General Public License for more details. 
 * 
 * You should have received a copy of the GNU Lesser General Public License 
 * along with this program; if not, write to the Free Software 
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA.
 */
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
