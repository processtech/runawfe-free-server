package ru.runa.common.web;

import org.junit.Test;
import ru.runa.wf.web.ftl.component.ViewUtil;

import static org.junit.Assert.*;

public class InputComponentHtmlReplacerTest {
    private final static String testString = "Test\n[]asd.\"Test2\"[sdsdg].\"\".[\"s\"]Test\n[]asd.\"Test2\"[sdsdg].\"\".[\"s\"]Test\n[]asd.\"Test2\"[sdsdg].\"\".[\"s\"]Test\n[]asd.\"Test2\"[sdsdg].\"\".[\"s\"]Test\n[]asd.\"Test2\"[sdsdg].\"\".[\"s\"]Test\n[]asd.\"Test2\"[sdsdg].\"\".[\"s\"]Test\n[]asd.\"Test2\"[sdsdg].\"\".[\"s\"]Test\n[]asd.\"Test2\"[sdsdg].\"\".[\"s\"]Test\n[]asd.\"Test2\"[sdsdg].\"\".[\"s\"]";
    private String result;

    @Test
    public void replaceTest() {
        result = ViewUtil.convertComponentInputToTemplateValue(testString);
        assertFalse(result.contains("\n"));
        assertFalse(result.contains("\""));
        assertFalse(result.contains("[]"));
        assertTrue(result.contains("'"));
        assertTrue(result.contains("{}"));
    }

    @Test
    public void compareReplaceAllWithReplace() {
        assertEquals(testString
                        .replaceAll("\"", "'")
                        .replaceAll("\n", "")
                        .replace("[]", "{}"),
                ViewUtil.convertComponentInputToTemplateValue(testString));
    }


}