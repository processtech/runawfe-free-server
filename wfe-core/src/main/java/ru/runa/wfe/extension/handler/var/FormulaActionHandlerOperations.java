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
package ru.runa.wfe.extension.handler.var;

import java.io.InputStream;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;
import java.util.TreeMap;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.dom4j.Document;
import org.dom4j.Element;

import ru.runa.wfe.commons.CalendarUtil;
import ru.runa.wfe.commons.ClassLoaderUtil;
import ru.runa.wfe.commons.TypeConversionUtil;
import ru.runa.wfe.commons.xml.XmlUtils;
import ru.runa.wfe.extension.function.CreateSubListByIndexes;
import ru.runa.wfe.extension.function.DeleteListElementsByIndexes;
import ru.runa.wfe.extension.function.Function;
import ru.runa.wfe.extension.function.GetListMatchedIndexes;
import ru.runa.wfe.extension.function.GetListMismatchedIndexes;
import ru.runa.wfe.extension.function.GetSize;
import ru.runa.wfe.extension.function.ListToString;
import ru.runa.wfe.extension.function.ToList;

import com.google.common.collect.Maps;

public class FormulaActionHandlerOperations {
    private static final Log log = LogFactory.getLog(FormulaActionHandlerOperations.class);
    private static final Map<String, Function<? extends Object>> functions = Maps.newHashMap();
    static {
        registerFunction(new ListToString());
        registerFunction(new GetListMatchedIndexes());
        registerFunction(new GetListMismatchedIndexes());
        registerFunction(new CreateSubListByIndexes());
        registerFunction(new DeleteListElementsByIndexes());
        registerFunction(new ToList());
        registerFunction(new GetSize());
    }

    private static void registerFunction(Function<? extends Object> function) {
        functions.put(function.getName(), function);
    }

    public static Function<? extends Object> getFunction(String name) {
        return functions.get(name);
    }

    public Object sum(Object o1, Object o2) {
        if (Double.class.isInstance(o1) && Number.class.isInstance(o2)) {
            return new Double(((Number) o1).doubleValue() + ((Number) o2).doubleValue());
        }
        if (Number.class.isInstance(o1) && Double.class.isInstance(o2)) {
            return new Double(((Number) o1).doubleValue() + ((Number) o2).doubleValue());
        }
        if (Long.class.isInstance(o1) && Long.class.isInstance(o2)) {
            return new Long((long) (((Long) o1).doubleValue() + ((Long) o2).doubleValue()));
        }
        if (Date.class.isInstance(o1) && Number.class.isInstance(o2)) {
            return new Date(((Date) o1).getTime() + (long) (((Number) o2).doubleValue() * 60 * 1000));
        }
        if (Date.class.isInstance(o1) && Date.class.isInstance(o2)) {
            Date date2 = (Date) o2;
            Calendar calendar = new GregorianCalendar();
            calendar.setTime(date2);
            return new Date(((Date) o1).getTime() + calendar.get(Calendar.HOUR_OF_DAY) * 3600000 + calendar.get(Calendar.MINUTE) * 60000);
        }
        if (String.class.isInstance(o1)) {
            return (String) o1 + translate(o2, String.class);
        }
        if (String.class.isInstance(o2)) {
            return translate(o1, String.class).toString() + (String) o2;
        }
        log.error("Cannot make summation for " + (o1 != null ? o1.getClass() : "null") + " with " + (o2 != null ? o2.getClass() : "null"));
        return null;
    }

    public Object sub(Object o1, Object o2) {
        if (Double.class.isInstance(o1) && Number.class.isInstance(o2)) {
            return new Double(((Number) o1).doubleValue() - ((Number) o2).doubleValue());
        }
        if (Number.class.isInstance(o1) && Double.class.isInstance(o2)) {
            return new Double(((Number) o1).doubleValue() - ((Number) o2).doubleValue());
        }
        if (Long.class.isInstance(o1) && Long.class.isInstance(o2)) {
            return new Long((long) (((Number) o1).doubleValue() - ((Number) o2).doubleValue()));
        }
        if (Date.class.isInstance(o1) && Number.class.isInstance(o2)) {
            return new Date(((Date) o1).getTime() - (long) (((Number) o2).doubleValue() * 60 * 1000));
        }
        if (Date.class.isInstance(o1) && Date.class.isInstance(o2)) {
            return new Long((((Date) o1).getTime() - ((Date) o2).getTime()) / 60000);
        }
        log.error("Cannot make substraction for " + o1.getClass() + " with " + o2.getClass());
        return null;
    }

    public Object mul(Object o1, Object o2) {
        if (Double.class.isInstance(o1) && Number.class.isInstance(o2)) {
            return new Double(((Number) o1).doubleValue() * ((Number) o2).doubleValue());
        }
        if (Number.class.isInstance(o1) && Double.class.isInstance(o2)) {
            return new Double(((Number) o1).doubleValue() * ((Number) o2).doubleValue());
        }
        if (Long.class.isInstance(o1) && Long.class.isInstance(o2)) {
            return new Long((long) (((Number) o1).doubleValue() * ((Number) o2).doubleValue()));
        }
        log.error("Cannot make multiplication for " + (o1 != null ? o1.getClass() : "null") + " with " + (o2 != null ? o2.getClass() : "null"));
        return null;
    }

    public Object div(Object o1, Object o2) {
        if (Double.class.isInstance(o1) && Number.class.isInstance(o2)) {
            return new Double(((Double) o1).doubleValue() / ((Number) o2).doubleValue());
        }
        if (Long.class.isInstance(o1) && Number.class.isInstance(o2)) {
            return new Long((long) (((Long) o1).doubleValue() / ((Number) o2).doubleValue()));
        }
        log.error("Cannot make division for " + o1.getClass() + " with " + o2.getClass());
        return null;
    }

    public Object changeSign(Object o) {
        if (Double.class.isInstance(o)) {
            return new Double(-((Double) o).doubleValue());
        }
        if (Long.class.isInstance(o)) {
            return new Long(-((Long) o).longValue());
        }
        log.error("Cannot make changeSign for " + o.getClass());
        return null;
    }

    public Object not(Object o) {
        if (Boolean.class.isInstance(o)) {
            return new Boolean(!((Boolean) o).booleanValue());
        }
        log.error("Cannot make not for " + o.getClass());
        return null;
    }

    public Object less(Object o1, Object o2) {
        if (Double.class.isInstance(o1) && Double.class.isInstance(o2)) {
            return new Boolean(((Double) o1).doubleValue() < ((Double) o2).doubleValue());
        }
        if (Double.class.isInstance(o1) && Long.class.isInstance(o2)) {
            return new Boolean(((Double) o1).doubleValue() < ((Long) o2).doubleValue());
        }
        if (Long.class.isInstance(o1) && Double.class.isInstance(o2)) {
            return new Boolean(((Long) o1).doubleValue() < ((Double) o2).doubleValue());
        }
        if (Long.class.isInstance(o1) && Long.class.isInstance(o2)) {
            return new Boolean(((Long) o1).longValue() < ((Long) o2).longValue());
        }
        if (String.class.isInstance(o1) && String.class.isInstance(o2)) {
            return new Boolean(((String) o1).compareTo((String) o2) < 0);
        }
        if (Date.class.isInstance(o1) && Date.class.isInstance(o2)) {
            return new Boolean(((Date) o1).compareTo((Date) o2) < 0);
        }
        log.error("Cannot make less for " + o1.getClass() + " with " + o2.getClass());
        return null;
    }

    public Object bigger(Object o1, Object o2) {
        return less(o2, o1);
    }

    public Object equal(Object o1, Object o2) {
        return new Boolean(o1.equals(o2));
    }

    public Object lessOrEqual(Object o1, Object o2) {
        Object less = less(o1, o2);
        Object equal = equal(o1, o2);
        return or(less, equal);
    }

    public Object biggerOrEqual(Object o1, Object o2) {
        Object bigger = bigger(o1, o2);
        Object equal = equal(o1, o2);
        return or(bigger, equal);
    }

    public Object or(Object o1, Object o2) {
        if (Boolean.class.isInstance(o1) && Boolean.class.isInstance(o2)) {
            return new Boolean(((Boolean) o1).booleanValue() || ((Boolean) o2).booleanValue());
        }
        log.error("Cannot make or for " + o1.getClass() + " with " + o2.getClass());
        return null;
    }

    public Object and(Object o1, Object o2) {
        if (Boolean.class.isInstance(o1) && Boolean.class.isInstance(o2)) {
            return new Boolean(((Boolean) o1).booleanValue() && ((Boolean) o2).booleanValue());
        }
        log.error("Cannot make and for " + o1.getClass() + " with " + o2.getClass());
        return null;
    }

    public Object xor(Object o1, Object o2) {
        if (Boolean.class.isInstance(o1) && Boolean.class.isInstance(o2)) {
            return new Boolean(((Boolean) o1).booleanValue() ^ ((Boolean) o2).booleanValue());
        }
        log.error("Cannot make xor for " + o1.getClass() + " with " + o2.getClass());
        return null;
    }

    public Object translate(Object o, Class<?> c) {
        if (c == String.class && Date.class.isInstance(o)) {
            Date date = (Date) o;
            Calendar calendar = new GregorianCalendar();
            calendar.setTime(date);
            if (calendar.get(Calendar.YEAR) == 1970 && calendar.get(Calendar.MONTH) == Calendar.JANUARY && calendar.get(Calendar.DAY_OF_MONTH) == 1) {
                return CalendarUtil.format(date, CalendarUtil.HOURS_MINUTES_FORMAT);
            }
            if (calendar.get(Calendar.HOUR) == 0 && calendar.get(Calendar.MINUTE) == 0 && calendar.get(Calendar.SECOND) == 0) {
                return CalendarUtil.format(date, CalendarUtil.DATE_WITHOUT_TIME_FORMAT);
            }
            return CalendarUtil.format(date, CalendarUtil.DATE_WITH_HOUR_MINUTES_FORMAT);
        }
        if (Date.class.isAssignableFrom(c) && Date.class.isInstance(o)) {
            return o;
        }
        return TypeConversionUtil.convertTo(c, o);
    }

    public Object dateFunction(Object p) {
        if (!Date.class.isInstance(p)) {
            return null;
        }
        Date d = (Date) p;
        try {
            return new SimpleDateFormat("dd.MM.yy").parse(new SimpleDateFormat("dd.MM.yy").format(d));
        } catch (ParseException e) {
            log.warn("Unparseable date", e);
        }
        return null;
    }

    public Object timeFunction(Object p) {
        if (!Date.class.isInstance(p)) {
            return null;
        }
        Date d = (Date) p;
        try {
            String s = CalendarUtil.format(d, CalendarUtil.HOURS_MINUTES_FORMAT);
            return CalendarUtil.convertToDate(s, CalendarUtil.HOURS_MINUTES_FORMAT);
        } catch (Exception e) {
            log.warn("Unparseable time", e);
        }
        return null;
    }

    public Object hoursRoundUpFunction(Object p) {
        Double d = (Double) translate(p, Double.class);
        if (d == null) {
            return null;
        }
        double minutes = d.doubleValue();
        long hours = (long) (minutes / 60);
        if (hours * 60 < minutes) {
            hours++;
        }
        return new Long(hours * 60);
    }

    public Long roundUpFunction(double d) {
        return (long) d + (d == (long) d ? 0 : 1);
    }

    public Long roundDownFunction(double d) {
        return (long) d;
    }

    public Long roundFunction(double d) {
        return Math.round(d);
    }

    public Double roundUpFunction(double d, int num) {
        long st = 1;
        while (num-- > 0) {
            st *= 10;
        }
        return (double) roundUpFunction(d * st) / st;
    }

    public Double roundDownFunction(double d, int num) {
        long st = 1;
        while (num-- > 0) {
            st *= 10;
        }
        return (double) roundDownFunction(d * st) / st;
    }

    public Double roundFunction(double d, int num) {
        long st = 1;
        while (num-- > 0) {
            st *= 10;
        }
        return (double) roundFunction(d * st) / st;
    }

    private String wordCaseRussian(String word, int caseNumber, boolean sex, int wordType, boolean onlyOneChar) {
        // sex : male = true, female = false
        // wordType : 1 = family name, 2 = name, 3 = parent

        // http://sourceforge.net/p/runawfe/bugs/624/
        if (word == null || word.length() == 0) {
            return "";
        }

        if (onlyOneChar) {
            return "" + Character.toUpperCase(word.replaceAll(" ", "").charAt(0)) + '.';
        }

        switch (wordType) {
        case 1:
            if (families.containsKey(word) && families.get(word).containsKey(caseNumber)) {
                return families.get(word).get(caseNumber);
            }
            break;
        case 2:
            if (names.containsKey(word) && names.get(word).containsKey(caseNumber)) {
                return names.get(word).get(caseNumber);
            }
            break;
        case 3:
            if (parents.containsKey(word) && parents.get(word).containsKey(caseNumber)) {
                return parents.get(word).get(caseNumber);
            }
            break;
        }

        int i = word.indexOf("-");
        if (i > 0) {
            String part1 = word.substring(0, i);
            String part2 = word.substring(i + 1);
            return wordCaseRussian(part1, caseNumber, sex, wordType, onlyOneChar) + "-"
                    + wordCaseRussian(part2, caseNumber, sex, wordType, onlyOneChar);
        }

        int len = word.length();
        word = word.toLowerCase();

        String suf3 = word.length() >= 3 ? word.substring(len - 3, len) : "___";
        String suf2 = word.length() >= 2 ? word.substring(len - 2, len) : "__";
        String suf1 = word.length() >= 1 ? word.substring(len - 1, len) : "_";

        if (suf3.equals("кий") && wordType == 1 && !onlyOneChar && word.length() > 4) {
            String prefix = upcaseFirstChar(word.substring(0, len - 3));
            switch (caseNumber) {
            case 1:
                return prefix + "кий";
            case 2:
                return prefix + "кого";
            case 3:
                return prefix + "кому";
            case 4:
                return prefix + "кого";
            case 5:
                return prefix + "ким";
            case 6:
                return prefix + "ком";
            }
        }

        int za = "ая ия ел ок яц ий па да ца ша ба та га ка".indexOf(suf2) + 1;
        int zb = "аеёийоуэюяжнгхкчшщ".indexOf(suf3.charAt(0)) + 1;
        int zd = 5;
        if (za != 4) {
            zd = "айяь".indexOf(suf1) + 1;
        }
        String fs1 = "оиеу" + (sex ? "" : "бвгджзклмнпрстфхцчшщъ");
        String fs2 = "мия мяэ лия кия жая лея";
        boolean b = caseNumber == 1 || suf1.equals(".") || wordType == 2 && fs1.indexOf(suf1) >= 0 || wordType == 1 && fs2.indexOf(suf3) >= 0;
        if (b) {
            zd = 9;
        } else {
            boolean b2 = zd == 4 && sex;
            if (b2) {
                zd = 2;
            } else {
                if (wordType == 1) {
                    if ("оеиую".indexOf(suf1) + "их ых аа еа ёа иа оа уа ыа эа юа яа".indexOf(suf2) + 2 > 0) {
                        zd = 9;
                    } else {
                        if (!sex) {
                            if (za == 1) {
                                zd = 7;
                            } else {
                                if (suf1.equals("а")) {
                                    zd = za > 18 ? 1 : 6;
                                } else {
                                    zd = 9;
                                }
                            }
                        } else {
                            boolean b3 = "ой ый".indexOf(suf2) >= 0 && wordType > 4 && !word.substring(len - 4, len).equals("опой") || zb > 10
                                    && za > 16;
                            if (b3) {
                                zd = 8;
                            }
                        }
                    }
                }
            }
        }

        int ze = "лец вей бей дец пец мец нец рец вец аец иец ыец бер".indexOf(suf3) + 1;

        String zf = zd == 8 && caseNumber != 5 ? zb > 15 || "жий ний".indexOf(suf3) >= 0 ? "е" : "о" : word.equals("лев") ? "ьв" : len - 4 >= 0
                && "аеёийоуэюя".indexOf(word.substring(len - 4, len - 3)) < 0 && (zb > 11 || zb == 0) && ze != 45 ? "" : za == 7 ? "л"
                : za == 10 ? "к" : za == 13 ? "йц" : ze == 0 ? "" : ze < 12 ? "ь" + (ze == 1 ? "ц" : "") : ze < 37 ? "ц" : ze < 49 ? "йц" : "р";

        if (zd != 9) {
            int nm = len;
            if (zd > 6 || zf.length() > 0) {
                nm -= 2;
            } else {
                nm -= zd > 0 ? 1 : 0;
            }
            String ns = word.substring(0, nm);
            ns += zf;
            String ss = "а у а " + "оыые".substring("внч".indexOf(suf1) + 1).charAt(0) + "ме " + ("гжкхш".indexOf(suf2.charAt(0)) > 0 ? "и" : "ы")
                    + " е у ойе я ю я ем" + (za == 16 ? "и" : "е") + " и е ю ейе и и ь ьюи и и ю ейи ойойу ойойойойуюойойгомуго"
                    + (zf.equals("е") || za == 16 || zb > 12 && zb < 16 ? "и" : "ы") + "мм";
            ns += ss.substring(10 * zd + 2 * caseNumber - 3 - 1, 10 * zd + 2 * caseNumber - 3 + 1);
            zf = ns;
        } else {
            zf = word;
        }

        String ans = zf;
        ans = ans.replace(" ", "");
        ans = upcaseFirstChar(ans);
        return ans;
    }

    private String upcaseFirstChar(String ans) {
        if (ans.length() != 0) {
            ans = "" + Character.toUpperCase(ans.charAt(0)) + ans.substring(1);
        }
        return ans;
    }

    private static TreeMap<String, HashMap<Integer, String>> names = new TreeMap<String, HashMap<Integer, String>>();
    private static TreeMap<String, HashMap<Integer, String>> families = new TreeMap<String, HashMap<Integer, String>>();
    private static TreeMap<String, HashMap<Integer, String>> parents = new TreeMap<String, HashMap<Integer, String>>();
    static {
        readNameCaseConfig("nameCaseConf.xml");
    }

    private static void readNameCaseConfig(String path) {
        try {
            InputStream is = ClassLoaderUtil.getAsStreamNotNull(path, FormulaActionHandlerOperations.class);
            Document document = XmlUtils.parseWithoutValidation(is);
            List<Element> childs = document.getRootElement().elements();
            for (Element element : childs) {
                if (element.getName().equals("name")) {
                    names.put(element.attributeValue("value"), parseNameCaseRules(element));
                }
                if (element.getName().equals("family")) {
                    families.put(element.attributeValue("value"), parseNameCaseRules(element));
                }
                if (element.getName().equals("parent")) {
                    parents.put(element.attributeValue("value"), parseNameCaseRules(element));
                }
            }
        } catch (Exception e) {
            log.error("Can`t parse " + path, e);
        }
    }

    private static HashMap<Integer, String> parseNameCaseRules(Element element) {
        HashMap<Integer, String> result = new HashMap<Integer, String>();
        List<Element> childs = element.elements();
        for (Element child : childs) {
            if (child.getName().equals("name")) {
                break;
            }
            int c = Integer.parseInt(child.attributeValue("case"));
            result.put(c, child.getText());
        }
        return result;
    }

    public String nameCaseRussian(String fio, int caseNumber, String mode) {
        boolean sex = false;
        StringTokenizer st = new StringTokenizer(fio);
        if (st.hasMoreElements()) {
            st.nextToken();
        }
        if (st.hasMoreElements()) {
            st.nextToken();
        }
        String parent = st.hasMoreElements() ? st.nextToken() : "   ";
        if (parent.charAt(parent.length() - 1) == 'ч') {
            sex = true;
        }
        return nameCaseRussian(fio, caseNumber, sex, mode);
    }

    public String nameCaseRussian(String fio, int caseNumber, boolean sex, String mode) {
        StringTokenizer st = new StringTokenizer(fio);
        String family = st.hasMoreElements() ? st.nextToken() : "";
        String name = st.hasMoreElements() ? st.nextToken() : "";
        String parent = st.hasMoreElements() ? st.nextToken() : "";

        String answer = "";
        for (char c : mode.toCharArray()) {
            switch (c) {
            case 'F':
                answer += wordCaseRussian(family, caseNumber, sex, 1, false);
                break;
            case 'I':
                answer += wordCaseRussian(name, caseNumber, sex, 2, false);
                break;
            case 'O':
                answer += wordCaseRussian(parent, caseNumber, sex, 3, false);
                break;
            case 'f':
                answer += wordCaseRussian(family, caseNumber, sex, 1, true);
                break;
            case 'i':
                answer += wordCaseRussian(name, caseNumber, sex, 2, true);
                break;
            case 'o':
                answer += wordCaseRussian(parent, caseNumber, sex, 3, true);
                break;
            default:
                answer += c;
            }
        }

        return answer;
    }

}
