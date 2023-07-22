package ru.runa.wfe.office.shared;

import org.dom4j.Element;
import org.testng.annotations.BeforeClass;
import org.mockito.Mockito;
import org.mockito.MockedStatic;
import org.testng.annotations.Test;
import ru.runa.wfe.commons.SystemProperties;

public class FilesSupplierConfigParserTest {

    private static FilesSupplierConfigParser<FilesSupplierConfig> parser;

    @BeforeClass
    public static void init() {
        FilesSupplierConfig config = Mockito.mock(FilesSupplierConfig.class);
        parser = new FilesSupplierConfigParser<FilesSupplierConfig>() {
            @Override
            protected FilesSupplierConfig instantiate() {
                return config;
            }

            @Override
            protected void parseCustom(Element root, FilesSupplierConfig config) throws Exception {
                // do nothing
            }
        };
    }

    private void runParseWhenAccessProhibited(String config) throws Exception {
        try (MockedStatic<SystemProperties> systemProperties = Mockito.mockStatic(SystemProperties.class)) {
            systemProperties.when(SystemProperties::isFileSystemAccessAllowed).thenReturn(false);
            parser.parse(config);
        }
    }

    private void runParseWhenAccessAllowed(String config) throws Exception {
        try (MockedStatic<SystemProperties> systemProperties = Mockito.mockStatic(SystemProperties.class)) {
            systemProperties.when(SystemProperties::isFileSystemAccessAllowed).thenReturn(true);
            parser.parse(config);
        }
    }

    // ExcelSaveHandlerConfig
    @Test(expectedExceptions = Exception.class, expectedExceptionsMessageRegExp = "Access to server file system is not allowed")
    public void throwsException_parsingExcelSaveHandlerConfig_whenAccessProhibited_andAccessNeeded() throws Exception {
        runParseWhenAccessProhibited("<config>\n" +
                "  <input variable=\"fileVar\"/>\n" +
                "  <output dir=\"C:\\temp\\excelFile.xlsx\" fileName=\"file2.xlsx\"/>\n" +
                "  <binding variable=\"str\" class=\"ru.runa.wfe.office.excel.CellConstraints\">\n" +
                "    <config sheet=\"1\" row=\"1\" column=\"1\"/>\n" +
                "  </binding>\n" +
                "</config>");
    }

    @Test
    public void notThrowsException_parsingExcelSaveHandlerConfig_whenAccessAllowed_andAccessNeeded() throws Exception {
        runParseWhenAccessAllowed("<config>\n" +
                "  <input variable=\"fileVar\"/>\n" +
                "  <output dir=\"C:\\temp\\excelFile.xlsx\" fileName=\"file2.xlsx\"/>\n" +
                "  <binding variable=\"str\" class=\"ru.runa.wfe.office.excel.CellConstraints\">\n" +
                "    <config sheet=\"1\" row=\"1\" column=\"1\"/>\n" +
                "  </binding>\n" +
                "</config>");
    }

    @Test
    public void notThrowsException_parsingExcelSaveHandlerConfig_whenAccessProhibited_andAccessNotNeededDueFileVariable() throws Exception {
        runParseWhenAccessProhibited("<config>\n" +
                "  <input variable=\"fileVar\"/>\n" +
                "  <output variable=\"fileVar2\" fileName=\"file2.xlsx\"/>\n" +
                "  <binding variable=\"str\" class=\"ru.runa.wfe.office.excel.CellConstraints\">\n" +
                "    <config sheet=\"1\" row=\"1\" column=\"1\"/>\n" +
                "  </binding>\n" +
                "</config>");
    }

    @Test
    public void notThrowsException_parsingExcelSaveHandlerConfig_whenAccessProhibited_andAccessNotNeededDueSystemFile() throws Exception {
        runParseWhenAccessProhibited("<config>\n" +
                "  <input path=\"processfile://ID4.template.xlsx\"/>\n" +
                "  <output variable=\"fileVar2\" fileName=\"file2.xlsx\"/>\n" +
                "  <binding variable=\"str\" class=\"ru.runa.wfe.office.excel.CellConstraints\">\n" +
                "    <config sheet=\"1\" row=\"1\" column=\"1\"/>\n" +
                "  </binding>\n" +
                "</config>");
    }

    // MergeDocxHandlerConfig
    @Test(expectedExceptions = Exception.class, expectedExceptionsMessageRegExp = "Access to server file system is not allowed")
    public void throwsException_parsingMergeDocxHandlerConfig_whenAccessProhibited_andAccessNeeded() throws Exception {
        runParseWhenAccessProhibited("<config strict=\"false\">\n" +
                "  <input path=\"C:\\temp\\doc1.docx\" addBreak=\"true\"/>\n" +
                "  <input path=\"C:\\temp\\doc3.docx\" addBreak=\"true\"/>\n" +
                "  <output dir=\"C:\\temp\" fileName=\"doc2.docx\"/>\n" +
                "</config>");
    }

    @Test
    public void notThrowsException_parsingMergeDocxHandlerConfig_whenAccessAllowed_andAccessNeeded() throws Exception {
        runParseWhenAccessAllowed("<config strict=\"false\">\n" +
                "  <input path=\"C:\\temp\\doc1.docx\" addBreak=\"true\"/>\n" +
                "  <input path=\"C:\\temp\\doc3.docx\" addBreak=\"true\"/>\n" +
                "  <output dir=\"C:\\temp\" fileName=\"doc2.docx\"/>\n" +
                "</config>");
    }

    @Test
    public void notThrowsException_parsingMergeDocxHandlerConfig_whenAccessProhibited_andAccessNotNeededDueFileVariable() throws Exception {
        runParseWhenAccessProhibited("<config strict=\"false\">\n" +
                "  <input variable=\"fileVar1\" addBreak=\"true\"/>\n" +
                "  <input variable=\"fileVar2\" addBreak=\"true\"/>\n" +
                "  <output variable=\"fileVar3\" fileName=\"doc2.docx\"/>\n" +
                "</config>");
    }

    @Test
    public void notThrowsException_parsingMergeDocxHandlerConfig_whenAccessProhibited_andAccessNotNeededDueSystemFile() throws Exception {
        runParseWhenAccessProhibited("<config strict=\"false\">\n" +
                "  <input path=\"processfile://ID1.template0.docx\" addBreak=\"true\"/>\n" +
                "  <input path=\"processfile://ID1.template1.docx\" addBreak=\"true\"/>\n" +
                "  <output variable=\"fileVar3\" fileName=\"doc2.docx\"/>\n" +
                "</config>");
    }

    // ExternalStorageHandlerConfig
    @Test(expectedExceptions = Exception.class, expectedExceptionsMessageRegExp = "Access to server file system is not allowed")
    public void throwsException_parsingExternalStorageHandlerConfig_whenAccessProhibited_andAccessNeeded() throws Exception {
        runParseWhenAccessProhibited("<config>\n" +
                "  <input path=\"C:\\temp\\doc1.docx\"/>\n" +
                "  <binding class=\"ru.runa.wfe.office.excel.AttributeConstraints\">\n" +
                "    <config sheet=\"1\" column=\"1\"/>\n" +
                "    <condition query=\"*\"/>\n" +
                "    <conditions type=\"SELECT\"/>\n" +
                "  </binding>\n" +
                "  <binding class=\"ru.runa.wfe.office.excel.AttributeConstraints\">\n" +
                "    <config sheet=\"1\" column=\"1\"/>\n" +
                "    <condition query=\"*\"/>\n" +
                "    <conditions type=\"SELECT\"/>\n" +
                "  </binding>\n" +
                "</config>");
    }

    @Test
    public void notThrowsException_parsingExternalStorageHandlerConfig_whenAccessAllowed_andAccessNeeded() throws Exception {
        runParseWhenAccessAllowed("<config>\n" +
                "  <input path=\"C:\\temp\\doc1.docx\"/>\n" +
                "  <binding class=\"ru.runa.wfe.office.excel.AttributeConstraints\">\n" +
                "    <config sheet=\"1\" column=\"1\"/>\n" +
                "    <condition query=\"*\"/>\n" +
                "    <conditions type=\"SELECT\"/>\n" +
                "  </binding>\n" +
                "  <binding class=\"ru.runa.wfe.office.excel.AttributeConstraints\">\n" +
                "    <config sheet=\"1\" column=\"1\"/>\n" +
                "    <condition query=\"*\"/>\n" +
                "    <conditions type=\"SELECT\"/>\n" +
                "  </binding>\n" +
                "</config>");
    }

    @Test
    public void notThrowsException_parsingExternalStorageHandlerConfig_whenAccessProhibited_andAccessNotNeededDueFileVariable() throws Exception {
        runParseWhenAccessProhibited("<config>\n" +
                "  <input variable=\"fileVar1\"/>\n" +
                "  <binding class=\"ru.runa.wfe.office.excel.AttributeConstraints\">\n" +
                "    <config sheet=\"1\" column=\"1\"/>\n" +
                "    <condition query=\"*\"/>\n" +
                "    <conditions type=\"SELECT\"/>\n" +
                "  </binding>\n" +
                "  <binding class=\"ru.runa.wfe.office.excel.AttributeConstraints\">\n" +
                "    <config sheet=\"1\" column=\"1\"/>\n" +
                "    <condition query=\"*\"/>\n" +
                "    <conditions type=\"SELECT\"/>\n" +
                "  </binding>\n" +
                "</config>");
    }

    @Test
    public void notThrowsException_parsingExternalStorageHandlerConfig_whenAccessProhibited_andAccessNotNeededDueSystemFile() throws Exception {
        runParseWhenAccessProhibited("<config>\n" +
                "  <input path=\"processfile://ID3.template.xlsx\"/>\n" +
                "  <binding class=\"ru.runa.wfe.office.excel.AttributeConstraints\">\n" +
                "    <config sheet=\"1\" column=\"1\"/>\n" +
                "    <condition query=\"*\"/>\n" +
                "    <conditions type=\"SELECT\"/>\n" +
                "  </binding>\n" +
                "  <binding class=\"ru.runa.wfe.office.excel.AttributeConstraints\">\n" +
                "    <config sheet=\"1\" column=\"1\"/>\n" +
                "    <condition query=\"*\"/>\n" +
                "    <conditions type=\"SELECT\"/>\n" +
                "  </binding>\n" +
                "</config>");
    }

    @Test
    public void notThrowsException_parsingExternalStorageHandlerConfig_whenAccessProhibited_andAccessNotNeededDueDatasource() throws Exception {
        runParseWhenAccessProhibited("<config>\n" +
                "  <input path=\"datasource:hellosource\"/>\n" +
                "  <binding class=\"ru.runa.wfe.office.excel.AttributeConstraints\">\n" +
                "    <config sheet=\"1\" column=\"1\"/>\n" +
                "    <condition query=\"*\"/>\n" +
                "    <conditions type=\"SELECT\"/>\n" +
                "  </binding>\n" +
                "  <binding class=\"ru.runa.wfe.office.excel.AttributeConstraints\">\n" +
                "    <config sheet=\"1\" column=\"1\"/>\n" +
                "    <condition query=\"*\"/>\n" +
                "    <conditions type=\"SELECT\"/>\n" +
                "  </binding>\n" +
                "</config>");
    }

    @Test
    public void notThrowsException_parsingExternalStorageHandlerConfig_whenAccessProhibited_andAccessNotNeededDueDatasourceVariable() throws Exception {
        runParseWhenAccessProhibited("<config>\n" +
                "  <input path=\"datasource-variable:stringVar\"/>\n" +
                "  <binding class=\"ru.runa.wfe.office.excel.AttributeConstraints\">\n" +
                "    <config sheet=\"1\" column=\"1\"/>\n" +
                "    <condition query=\"*\"/>\n" +
                "    <conditions type=\"SELECT\"/>\n" +
                "  </binding>\n" +
                "  <binding class=\"ru.runa.wfe.office.excel.AttributeConstraints\">\n" +
                "    <config sheet=\"1\" column=\"1\"/>\n" +
                "    <condition query=\"*\"/>\n" +
                "    <conditions type=\"SELECT\"/>\n" +
                "  </binding>\n" +
                "</config>");
    }

    @Test(expectedExceptions = Exception.class, expectedExceptionsMessageRegExp = "Access to server file system is not allowed")
    public void throwsException_parsingExternalStorageHandlerUnicodeConfig_whenAccessProhibited_andAccessNeeded() throws Exception {
        runParseWhenAccessProhibited("<config>\n" +
                "  <input \u0070\u0061\u0074\u0068\u003d\u005c\u0022\u0043\u003a\u005c\u005c\u0074\u0065\u006d\u0070\u005c\u005c\u0064\u006f\u0063\u0031\u002e\u0064\u006f\u0063\u0078\u005c\u0022/>\n" +
                "  <binding class=\"ru.runa.wfe.office.excel.AttributeConstraints\">\n" +
                "    <config sheet=\"1\" column=\"1\"/>\n" +
                "    <condition query=\"*\"/>\n" +
                "    <conditions type=\"SELECT\"/>\n" +
                "  </binding>\n" +
                "  <binding class=\"ru.runa.wfe.office.excel.AttributeConstraints\">\n" +
                "    <config sheet=\"1\" column=\"1\"/>\n" +
                "    <condition query=\"*\"/>\n" +
                "    <conditions type=\"SELECT\"/>\n" +
                "  </binding>\n" +
                "</config>");
    }
}
