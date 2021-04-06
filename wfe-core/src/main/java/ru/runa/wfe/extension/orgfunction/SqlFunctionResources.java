package ru.runa.wfe.extension.orgfunction;

import ru.runa.wfe.commons.PropertyResources;

/**
 * Created on 03.01.2006
 * 
 * @author Vitaliy S aka Yilativs
 * @author Gordienko_m modified by miheev_a 19.05.2006
 */
public class SqlFunctionResources {
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
