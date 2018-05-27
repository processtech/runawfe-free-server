package ru.runa.wfe.office.doc;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.google.common.io.ByteStreams;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Date;
import java.util.Map;
import org.apache.commons.logging.LogFactory;
import org.apache.poi.xwpf.usermodel.XWPFDocument;
import org.testng.Assert;
import org.testng.annotations.Test;
import ru.runa.wfe.commons.CalendarUtil;
import ru.runa.wfe.commons.ClassLoaderUtil;
import ru.runa.wfe.user.Actor;
import ru.runa.wfe.var.VariableDefinition;
import ru.runa.wfe.var.dto.WfVariable;
import ru.runa.wfe.var.file.FileVariable;
import ru.runa.wfe.var.format.ActorFormat;
import ru.runa.wfe.var.format.DateFormat;
import ru.runa.wfe.var.format.DateTimeFormat;
import ru.runa.wfe.var.format.ListFormat;
import ru.runa.wfe.var.format.MapFormat;
import ru.runa.wfe.var.format.StringFormat;
import ru.runa.wfe.var.format.TimeFormat;
import ru.runa.wfe.var.format.VariableFormat;

public class DocxFileChangerTest extends Assert {
    private static final String[] prefixes = { "lo_", "ms_" };

    @Test
    public void testSimpleChange() throws IOException {
        Map<String, Object> data = Maps.newHashMap();
        data.put("contractNumber", "2");
        data.put("listIndex", 2L);
        data.put("currentDate", createVariable("currentDate", new DateFormat(), new Date()));
        data.put("currentDateTime", createVariable("currentDateTime", new DateTimeFormat(), new Date()));
        data.put("currentTime", createVariable("currentTime", new TimeFormat(), new Date()));
        data.put(
                "actorList",
                createVariable("actorList", createListFormat(ActorFormat.class),
                        Lists.newArrayList(new Actor("ACTOR1", "address"), new Actor("Ivanov", "Pervomayskaya str 30a"))));
        Map<Object, Actor> actors = Maps.newHashMap();
        actors.put("2", new Actor("adamov_a", "", "Adamov A.A.", 444L));
        actors.put("borisov", new Actor("borisov", "", "Borisov B.B", 333L));
        actors.put(2L, new Actor("denisov", "", "Denisov Alexey", 555L));
        data.put("actorMap", createVariable("actorMap", createMapFormat(StringFormat.class, ActorFormat.class), actors));
        testDocx(true, "simple_change.docx", data);
    }

    @Test
    public void testTableColumns() throws IOException {
        Map<String, Object> data = Maps.newHashMap();
        data.put(
                "actorList",
                createVariable("actorList", createListFormat(ActorFormat.class),
                        Lists.newArrayList(new Actor("name", "address"), new Actor("Ivanov", "Pervomayskaya str 30a"))));
        data.put("stringList", createVariable("stringList", createListFormat(StringFormat.class), Lists.newArrayList("Ivanov", "Petrov", "Sidorov")));
        data.put(
                "dateList",
                createVariable(
                        "dateList",
                        createListFormat(DateFormat.class),
                        Lists.newArrayList(new Date(), CalendarUtil.convertToDate("01.01.2013", CalendarUtil.DATE_WITHOUT_TIME_FORMAT),
                                CalendarUtil.convertToDate("17.02.1982", CalendarUtil.DATE_WITHOUT_TIME_FORMAT))));
        Map<String, Actor> actors = Maps.newHashMap();
        actors.put("1", new Actor("adamov_a", "", "Adamov A.A.", 444L));
        actors.put("2", new Actor("borisov", "", "Borisov B.B", 333L));
        actors.put("3", new Actor("denisov", "", "Denisov Alexey", 555L));
        data.put("actorMap", createVariable("actorMap", createMapFormat(StringFormat.class, ActorFormat.class), actors));
        testDocx(true, "tables.docx", data);
    }

    @Test
    public void testTableColumnsFormat() throws IOException {
        Map<String, Object> data = Maps.newHashMap();
        data.put(
                "ArrayUser",
                createVariable("ArrayUser", createListFormat(ActorFormat.class), Lists.newArrayList(new Actor("Petrov", "", "", 1L, "test@email",
                        null, null, null), new Actor("Ivanov", "Pervomayskaya str 30a"))));
        testDocx(true, "tables_format.docx", data);
    }

    @Test
    public void testImages() throws IOException {
        Map<String, Object> data = Maps.newHashMap();
        data.put("image1",
                new FileVariable("image1.jpg", ByteStreams.toByteArray(ClassLoaderUtil.getAsStreamNotNull("image1.jpg", getClass())), null));
        data.put("image2",
                new FileVariable("image2.png", ByteStreams.toByteArray(ClassLoaderUtil.getAsStreamNotNull("image2.png", getClass())), null));
        testDocx(true, "images.docx", data);
    }

    @Test
    public void testLoops() throws IOException {
        Map<String, Object> data = Maps.newHashMap();
        data.put(
                "actorList",
                createVariable("actorList", createListFormat(ActorFormat.class),
                        Lists.newArrayList(new Actor("name", "address"), new Actor("Ivanov", "Pervomayskaya str 30a"))));
        data.put("stringList", createVariable("stringList", createListFormat(ActorFormat.class), Lists.newArrayList("Ivanov", "Petrov", "Sidorov")));
        data.put(
                "dateList",
                createVariable(
                        "dateList",
                        createListFormat(DateFormat.class),
                        Lists.newArrayList(new Date(), CalendarUtil.convertToDate("01.01.2013", CalendarUtil.DATE_WITHOUT_TIME_FORMAT),
                                CalendarUtil.convertToDate("17.02.1982", CalendarUtil.DATE_WITHOUT_TIME_FORMAT))));
        Map<String, Actor> actors = Maps.newHashMap();
        actors.put("1", new Actor("adamov_a", "", "Adamov A.A.", 444L));
        actors.put("2", new Actor("borisov", "", "Borisov B.B", 333L));
        actors.put("3", new Actor("denisov", "", "Denisov Alexey", 555L));
        data.put("actorMap", createVariable("actorMap", createMapFormat(StringFormat.class, ActorFormat.class), actors));
        data.put("currentDateTime", createVariable("currentDateTime", new DateTimeFormat(), new Date()));
        testDocx(true, "loops.docx", data);
    }

    @Test
    public void testTableSummary() throws IOException {
        Map<String, Object> data = Maps.newHashMap();
        data.put("list", createVariable("list", createListFormat(StringFormat.class), Lists.newArrayList("test1", "test2")));
        testDocx(true, "tables_with_summary.docx", data);
    }

    @Test
    public void testTableVMergeColumns() throws IOException {
        Map<String, Object> data = Maps.newHashMap();
        data.put("list",
                createVariable("list", createListFormat(StringFormat.class), Lists.newArrayList("test", "-||-", "test2", "-||-", "-||-", "test3")));
        testDocx(true, "tables_with_vmerge.docx", data);
    }

    @Test
    public void testTableSummaryAndVMergeColumns() throws IOException {
        Map<String, Object> data = Maps.newHashMap();
        data.put("list", createVariable("list", createListFormat(StringFormat.class), Lists.newArrayList("test", "-||-", "test2")));
        testDocx(true, "tables_with_summary_and_vmerge.docx", data);
    }

    // @Test
    public void testIf() throws IOException {
        Map<String, Object> data = Maps.newHashMap();
        data.put("contractNumber", "2");
        data.put("listIndex", 2L);
        data.put("actorList", Lists.newArrayList(new Actor("name", "address"), new Actor("Ivanov", "Pervomayskaya str 30a")));
        data.put("stringList", Lists.newArrayList("Ivanov", "Petrov", "Sidorov"));
        data.put(
                "dateList",
                Lists.newArrayList(new Date(), CalendarUtil.convertToDate("01.01.2013", CalendarUtil.DATE_WITHOUT_TIME_FORMAT),
                        CalendarUtil.convertToDate("17.02.1982", CalendarUtil.DATE_WITHOUT_TIME_FORMAT)));
        data.put("currentDate", createVariable("currentDate", new DateFormat(), new Date()));
        data.put("currentDateTime", createVariable("currentDateTime", new DateTimeFormat(), new Date()));
        data.put("currentTime", createVariable("currentTime", new TimeFormat(), new Date()));
        testDocx(true, "decisions.docx", data);
    }

    private WfVariable createVariable(String name, VariableFormat variableFormat, Object value) {
        VariableDefinition definition = new VariableDefinition(name, null, variableFormat);
        return new WfVariable(definition, value);
    }

    private ListFormat createListFormat(Class<? extends VariableFormat> componentFormat) {
        ListFormat listFormat = new ListFormat();
        listFormat.setComponentClassNames(new String[] { componentFormat.getName() });
        return listFormat;
    }

    private MapFormat createMapFormat(Class<? extends VariableFormat> keyFormat, Class<? extends VariableFormat> valueFormat) {
        MapFormat mapFormat = new MapFormat();
        mapFormat.setComponentClassNames(new String[] { keyFormat.getName(), valueFormat.getName() });
        return mapFormat;
    }

    private void testDocx(boolean strictMode, String templateFileName, Map<String, Object> data) throws IOException {
        for (String appPrefix : prefixes) {
            String appTemplateFileName = appPrefix + templateFileName;
            InputStream templateInputStream = ClassLoaderUtil.getAsStream(appTemplateFileName, getClass());
            if (templateInputStream == null) {
                LogFactory.getLog(getClass()).warn("No docx template found by name " + appTemplateFileName);
                continue;
            }
            DocxConfig config = new DocxConfig();
            config.setStrictMode(strictMode);
            DocxFileChanger changer = new DocxFileChanger(config, new TestVariableProvider(data), templateInputStream);
            XWPFDocument document = changer.changeAll();
            try {
                document.write(new FileOutputStream("target/result_" + appTemplateFileName));
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
