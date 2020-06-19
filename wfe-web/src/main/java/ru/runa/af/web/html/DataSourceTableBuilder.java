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
package ru.runa.af.web.html;

import java.util.stream.Collectors;
import java.util.stream.Stream;
import javax.servlet.jsp.PageContext;
import org.apache.ecs.html.IMG;
import org.apache.ecs.html.Input;
import org.apache.ecs.html.TD;
import org.apache.ecs.html.TH;
import org.apache.ecs.html.TR;
import org.apache.ecs.html.Table;
import org.apache.ecs.wml.A;
import ru.runa.common.web.Commons;
import ru.runa.common.web.HTMLUtils;
import ru.runa.common.web.MessagesCommon;
import ru.runa.common.web.Resources;
import ru.runa.common.web.form.StrIdsForm;
import ru.runa.wf.web.MessagesDataSource;
import ru.runa.wfe.commons.web.PortletUrlType;
import ru.runa.wfe.datasource.DataSource;
import ru.runa.wfe.datasource.DataSourceStorage;
import ru.runa.wfe.datasource.DataSourceType;
import ru.runa.wfe.datasource.ExcelDataSource;
import ru.runa.wfe.datasource.JdbcDataSource;
import ru.runa.wfe.datasource.JndiDataSource;

public class DataSourceTableBuilder {

    private final PageContext pageContext;

    public DataSourceTableBuilder(PageContext pageContext) {
        this.pageContext = pageContext;
    }

    public Table build() {
        Table table = new Table();
        table.setClass(Resources.CLASS_LIST_TABLE);
        table.setWidth("100%");
        table.addElement(createTableHeaderTR());
        for (DataSource ds : DataSourceStorage.getAllDataSources()) {
            table.addElement(createTR(ds));
        }
        return table;
    }

    private TR createTableHeaderTR() {
        TR tr = new TR();
        tr.setClass(Resources.CLASS_LIST_TABLE_TH);
        tr.addElement(new TH(HTMLUtils.createSelectionStatusPropagator()).setWidth("20").setClass(Resources.CLASS_LIST_TABLE_TD));
        tr.addElement(new TH(MessagesDataSource.LABEL_DATA_SOURCE_NAME.message(pageContext)).setWidth("25%").setClass(Resources.CLASS_LIST_TABLE_TD));
        tr.addElement(new TH("").setClass(Resources.CLASS_LIST_TABLE_TD));
        tr.addElement(new TH(MessagesDataSource.LABEL_DATA_SOURCE_ATTRIBUTES.message(pageContext)).setClass(Resources.CLASS_LIST_TABLE_TD));
        tr.addElement(new TH("").setClass(Resources.CLASS_LIST_TABLE_TD));
        return tr;
    }

    private TR createTR(DataSource ds) {
        TR tr = new TR();
        tr.setClass(Resources.CLASS_LIST_TABLE_TH);
        Input input = new Input(Input.CHECKBOX, StrIdsForm.IDS_INPUT_NAME, ds.getName());
        input.setChecked(false);
        tr.addElement(new TD(input).setClass(Resources.CLASS_LIST_TABLE_TD));
        tr.addElement(new TD(icon(ds.getType()) + "&nbsp" + ds.getName()).setClass(Resources.CLASS_LIST_TABLE_TD));
        tr.addElement(new TD(info(ds)).setAlign("center").setClass(Resources.CLASS_LIST_TABLE_TD));
        tr.addElement(new TD(attributes(ds)).setClass(Resources.CLASS_LIST_TABLE_TD));
        tr.addElement(new TD(password(ds)).setWidth(10).setClass(Resources.CLASS_LIST_TABLE_TD));
        return tr;
    }

    private String icon(DataSourceType dst) {
        String icon;
        switch (dst) {
        case Excel:
            icon = Resources.ICON_DATA_SOURCE_EXCEL;
            break;
        case JNDI:
            icon = Resources.ICON_DATA_SOURCE_JNDI;
            break;
        default:
            icon = Resources.ICON_DATA_SOURCE_JDBC;
        }
        return new IMG(Commons.getUrl(icon, pageContext, PortletUrlType.Action)).setTitle(dst.name()).toString();
    }

    private String info(DataSource ds) {
        if (ds.getType().equals(DataSourceType.JDBC)) {
            A a = new A();
            a.addElement(new IMG(Commons.getUrl(Resources.ICON_DATA_SOURCE_SERVER_VERSION, pageContext, PortletUrlType.Action)).toString());
            a.setTitle(MessagesDataSource.LABEL_DATA_SOURCE_SERVER_VERSION.message(pageContext));
            a.setHref("javascript:void(0)");
            a.addAttribute("onClick", "javascript:getServerVersion(\"" + ds.getName() + "\", \""
                    + MessagesDataSource.LABEL_DATA_SOURCE_SERVER_VERSION.message(pageContext) + "\");");
            return a.toString();
        } else {
            return "";
        }
    }

    private String password(DataSource ds) {
        if (ds.getType().equals(DataSourceType.JDBC)) {
            A a = new A();
            a.addElement(new IMG(Commons.getUrl(Resources.ICON_DATA_SOURCE_PASSWORD, pageContext, PortletUrlType.Action)).toString());
            a.setTitle(MessagesDataSource.LABEL_DATA_SOURCE_CHANGE_PASSWORD.message(pageContext));
            a.setHref("javascript:void(0)");
            a.addAttribute("onClick", "javascript:newDataSourcePassword(\"" + ds.getName() + "\", \""
                    + MessagesDataSource.LABEL_DATA_SOURCE_NEW_PASSWORD.message(pageContext) + "\", \""
                    + MessagesCommon.BUTTON_SAVE.message(pageContext) + "\", \"" + MessagesCommon.BUTTON_CANCEL.message(pageContext) + "\");");
            return a.toString();
        } else {
            return "";
        }
    }

    private String attributes(DataSource ds) {
        String attributes = "";
        switch (ds.getType()) {
        case Excel:
            ExcelDataSource eds = (ExcelDataSource) ds;
            attributes += "dirPath: " + eds.getFilePath();
            break;
        case JNDI:
            attributes += "jndiName: " + ((JndiDataSource) ds).getJndiName();
            break;
        default:
            JdbcDataSource jds = (JdbcDataSource) ds;
            attributes += Stream.of(
                    (jds.getDbType() == null ? "" : "dbType: " + jds.getDbType()),
                    (jds.getUrl() == null ? "" : "dbUrl: " + jds.getUrl()),
                    (jds.getDbName() == null ? "" : "dbName: " + jds.getDbName()),
                    (jds.getUserName() == null ? "" : "userName: " + jds.getUserName())
                    ).filter(f -> !f.isEmpty()).collect(Collectors.joining(", "));
            break;
        }
        return attributes;
    }

}
