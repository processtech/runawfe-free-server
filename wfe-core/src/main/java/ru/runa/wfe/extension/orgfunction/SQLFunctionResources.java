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
package ru.runa.wfe.extension.orgfunction;

import ru.runa.wfe.commons.PropertyResources;

/**
 * Created on 03.01.2006
 * 
 * @author Vitaliy S aka Yilativs
 * @author Gordienko_m modified by miheev_a 19.05.2006
 */
public class SQLFunctionResources {
    private static final PropertyResources RESOURCES = new PropertyResources("sql.orgfunction.properties");

    public static String getDataSourceName() {
        return RESOURCES.getStringPropertyNotNull("datasource");
    }

    public static String getChiefCodeBySubordinateCodeSQL() {
        return RESOURCES.getStringPropertyNotNull("chief.code.by.subordinate.code.sql");
    }

    public static String getAllDirectorsCodes() {
        return RESOURCES.getStringPropertyNotNull("get.all.directors.codes.sql");
    }

    public static String getSubordinateCodesByChiefCodeSQL() {
        return RESOURCES.getStringPropertyNotNull("subordinate.codes.by.chief.code.sql");
    }

}
