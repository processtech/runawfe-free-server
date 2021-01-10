package ru.runa.ftl.component;

import org.junit.Test;
import ru.runa.wf.web.ftl.component.EditUserTypeList;

import static org.junit.Assert.assertEquals;

public class EditUserTypeListTest {
    @Test
    public void getReturnValueTest1() {
        String input = "Line1\nLine2\nLine3";
        String output = "Line1Line2Line3";

        assertEquals(output, EditUserTypeList.getReturnValue(input));
    }

    @Test
    public void getReturnValueTest2() {
        String input = "str \"str\" str";
        String output = "str 'str' str";

        assertEquals(output, EditUserTypeList.getReturnValue(input));
    }

    @Test
    public void getReturnValueTest3() {
        String input = "[str[]str[]str]";
        String output = "[str{}str{}str]";

        assertEquals(output, EditUserTypeList.getReturnValue(input));
    }

    @Test
    public void getReturnValueTest4() {
        String input = "line1\nLine2\"str\"\n[]";
        String output = "line1Line2'str'{}";

        assertEquals(output, EditUserTypeList.getReturnValue(input));
    }
}
