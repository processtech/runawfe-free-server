package ru.runa.wfe.var.format;

import java.util.Date;
import java.util.List;

import ru.runa.wfe.commons.CalendarUtil;

import com.google.common.collect.Lists;

public class ListFormatTest {

    public void test1() throws Exception {
        ListFormat listFormat = new ListFormat();
        listFormat.setComponentClassNames(new String[] { DateTimeFormat.class.getName() });
        List<Date> list = Lists.newArrayList(new Date(), CalendarUtil.getZero().getTime());
        String s = listFormat.format(list);
        // System.out.println(s);
        List<Date> dates = (List<Date>) listFormat.parse(s);
        // TODO assert dates == list
    }
}
