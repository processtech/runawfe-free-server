package ru.runa.wfe.office.doc;

import java.io.IOException;
import java.util.List;
import java.util.Map;
import lombok.extern.apachecommons.CommonsLog;
import org.testng.annotations.DataProvider;
import org.testng.annotations.Test;
import org.testng.collections.Lists;
import org.testng.collections.Maps;

/**
 * @author Alekseev Vitaly
 */
@CommonsLog
public class DocxFileChangerTablesWithIndexesAndItemsTest extends DocxFileChangerTest {

    private Map<String, Object> buildListEntry(String field1, String value1, String field2, String value2, String field3, String value3) {
        Map<String, Object> result = Maps.newHashMap();
        result.put(field1, value1);
        result.put(field2, value2);
        result.put(field3, value3);
        return result;
    }

    @SuppressWarnings("unchecked")
    @DataProvider(name = "successData")
    protected Object[][] successData() {
        return new Object[][] {{
                "table_usertype_indexes_with_items.docx",
                "Комплексная переменная",
                "Поле-список",
                Lists.newArrayList(
                        buildListEntry("Поле 1", "значение поля 1 - 1", "Поле 2", null, "Поле 3", null),
                        buildListEntry("Поле 1", "значение поля 1 - 2", "Поле 2", "", "Поле 3", null),
                        buildListEntry("Поле 1", "значение поля 1 - 3", "Поле 2", "значение поля 2 - 3", "Поле 3", "")
                )
        }};
    }

    @Test(dataProvider = "successData")
    public void testChangeSuccess(String templateFilename, String rootVariable, String listVariable, List<Map<String, Object>> list)
            throws IOException {
        Map<String, Object> data = Maps.newHashMap();
        data.put(rootVariable + "." + listVariable, list);
        log.info(String.format("testChangeSuccess: data: %s", data));
        createDocx(true, templateFilename, data);
    }
}
