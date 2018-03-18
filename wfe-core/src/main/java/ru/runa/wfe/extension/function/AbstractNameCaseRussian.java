package ru.runa.wfe.extension.function;

import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.StringTokenizer;
import java.util.TreeMap;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.dom4j.Document;
import org.dom4j.Element;

import ru.runa.wfe.commons.ClassLoaderUtil;
import ru.runa.wfe.commons.xml.XmlUtils;

/**
 *
 * @author Dmitry Kononov
 * @since 18.03.2018
 *
 */
public abstract class AbstractNameCaseRussian extends Function<String> {

    protected static final Log log = LogFactory.getLog(AbstractNameCaseRussian.class);

    protected static TreeMap<String, HashMap<Integer, String>> names = new TreeMap<String, HashMap<Integer, String>>();
    protected static TreeMap<String, HashMap<Integer, String>> families = new TreeMap<String, HashMap<Integer, String>>();
    protected static TreeMap<String, HashMap<Integer, String>> parents = new TreeMap<String, HashMap<Integer, String>>();

    static {
        readNameCaseConfig("nameCaseConf.xml");
    }

    public AbstractNameCaseRussian(Param... params) {
        super(params);
    }

    protected boolean isMale(String fio) {
        boolean male = false;
        StringTokenizer st = new StringTokenizer(fio);
        if (st.hasMoreElements()) {
            st.nextToken();
        }
        if (st.hasMoreElements()) {
            st.nextToken();
        }
        String parent = st.hasMoreElements() ? st.nextToken() : "   ";
        if (parent.charAt(parent.length() - 1) == 'ч') {
            male = true;
        }
        return male;
    }

    protected String nameCaseRussian(String fio, int caseNumber, boolean sex, String mode) {
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
                            boolean b3 = "ой ый".indexOf(suf2) >= 0 && wordType > 4 && !word.substring(len - 4, len).equals("опой")
                                    || zb > 10 && za > 16;
                            if (b3) {
                                zd = 8;
                            }
                        }
                    }
                }
            }
        }

        int ze = "лец вей бей дец пец мец нец рец вец аец иец ыец бер".indexOf(suf3) + 1;

        String zf = zd == 8 && caseNumber != 5 ? zb > 15 || "жий ний".indexOf(suf3) >= 0 ? "е" : "о"
                : word.equals("лев") ? "ьв"
                        : len - 4 >= 0 && "аеёийоуэюя".indexOf(word.substring(len - 4, len - 3)) < 0 && (zb > 11 || zb == 0) && ze != 45 ? ""
                                : za == 7 ? "л"
                                        : za == 10 ? "к"
                                                : za == 13 ? "йц"
                                                        : ze == 0 ? "" : ze < 12 ? "ь" + (ze == 1 ? "ц" : "") : ze < 37 ? "ц" : ze < 49 ? "йц" : "р";

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

    private static void readNameCaseConfig(String path) {
        try {
            InputStream is = ClassLoaderUtil.getAsStream(path, NameCaseRussian.class);
            if (is == null) {
                log.warn("No " + path + " found");
                return;
            }
            Document document = XmlUtils.parseWithoutValidation(is);
            @SuppressWarnings("unchecked")
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
        @SuppressWarnings("unchecked")
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

}
