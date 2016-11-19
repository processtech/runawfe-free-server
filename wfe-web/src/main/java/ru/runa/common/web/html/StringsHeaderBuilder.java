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

import java.util.List;

import org.apache.ecs.html.TH;
import org.apache.ecs.html.TR;

import ru.runa.common.web.Resources;

/**
 */
public class StringsHeaderBuilder implements HeaderBuilder {
    private String[] headers;

    public StringsHeaderBuilder(String[] headers) {
        setHeaders(headers);
    }

    public StringsHeaderBuilder(List<String> headers) {
        setHeaders(headers.toArray(new String[headers.size()]));
    }

    protected StringsHeaderBuilder() {
    }

    protected void setHeaders(String[] headers) {
        this.headers = headers.clone();
    }

    @Override
    public TR build() {
        if (headers == null) {
            throw new IllegalStateException("headers weren't initialized");
        }
        TR tr = new TR();
        for (int i = 0; i < headers.length; i++) {
            tr.addElement(new TH(headers[i]).setClass(Resources.CLASS_LIST_TABLE_TH));
        }
        return tr;
    }
}
