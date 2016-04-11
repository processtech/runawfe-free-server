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
package ru.runa.common.web;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Properties;

/**
 * Created on 28.07.2005
 * 
 * Prints keys that exist in one bundle and do not exist in the other.
 * 
 * @author Semochkin_v@runa.ru
 * @author Gordienko_m@runa.ru
 */
public class ResourceBundleKeyComparator {

    private static final String[] BUNDEL_LANGS = { "de", "es", "fr", "nl", "pt", "ru", "uk", "zh" };

    private static final String BUNDLE_PATH = "D:\\work\\projects\\wf\\wfe\\trunk\\resources\\web\\WEB-INF\\classes\\struts";

    private static final String PROPERTIES_ENDING = ".properties";

    private static final boolean PRINT_KEY_DIFF = true;

    private static final boolean PRINT_KEY_VALUE_DIFF = true;

    public static void main(String[] args) throws Exception {
        if (args.length == 2) {
            printPropertiesDiff(args[0], args[1], PRINT_KEY_DIFF, PRINT_KEY_VALUE_DIFF);
        } else {

            String enPropFileName = BUNDLE_PATH + PROPERTIES_ENDING;
            for (int i = 0; i < BUNDEL_LANGS.length; i++) {
                System.out.println("=====" + BUNDEL_LANGS[i] + "========");
                String otherPropFileName = BUNDLE_PATH + "_" + BUNDEL_LANGS[i] + PROPERTIES_ENDING;
                printPropertiesDiff(enPropFileName, otherPropFileName, PRINT_KEY_DIFF, PRINT_KEY_VALUE_DIFF);
            }
        }
    }

    private static void printPropertiesDiff(String propFile0, String propFile1, boolean printKeyDiff, boolean printKeyValueDiff) throws IOException {
        Properties properties0 = load(new File(propFile0));
        Properties properties1 = load(new File(propFile1));
        HashSet keySet0 = new HashSet(properties0.keySet());
        HashSet keySet1 = new HashSet(properties1.keySet());

        HashSet set0MinSet1 = (HashSet) keySet0.clone();
        set0MinSet1.removeAll(keySet1);

        HashSet set1MinSet0 = (HashSet) keySet1.clone();
        set1MinSet0.removeAll(keySet0);

        printKeyDiff(set0MinSet1, set1MinSet0);
        printKeyDiffWithValues(propFile0, propFile1, properties0, properties1, set0MinSet1, set1MinSet0);
    }

    public static Properties load(File file) throws IOException {
        FileInputStream fis = null;
        try {
            fis = new FileInputStream(file);
            Properties properties = new Properties();
            properties.load(fis);
            return properties;
        } finally {
            if (fis != null) {
                fis.close();
            }
        }
    }

    private static void printKeyDiffWithValues(String propFile0, String propFile1, Properties properties0, Properties properties1,
            HashSet set0MinSet1, HashSet set1MinSet0) {
        System.out.println(propFile0);
        printProperties(properties0, set0MinSet1);
        System.out.println(propFile1);
        printProperties(properties1, set1MinSet0);
    }

    private static void printKeyDiff(HashSet set0MinSet1, HashSet set1MinSet0) {
        System.out.println(set0MinSet1);
        System.out.println(set1MinSet0);
    }

    private static void printProperties(Properties properties, HashSet keySet) {
        for (Iterator iter = keySet.iterator(); iter.hasNext();) {
            Object key = iter.next();
            System.out.println("	" + key + "=" + properties.get(key));
        }
    }

}
