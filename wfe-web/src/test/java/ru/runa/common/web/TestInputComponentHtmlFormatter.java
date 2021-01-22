package ru.runa.common.web;

import org.junit.Assert;
import org.junit.Assert.*;
import org.junit.Test;

import ru.runa.wf.web.ftl.component.ViewUtil;

public class TestInputComponentHtmlFormatter {
	
	private String someHtml = "   [] detail: {opened: false},\r\n" + 
			"  }),\r\n[]|||\"\"" + 
			"  \"open\": new CustomEvent(\"x-spoiler.changed\", {\r\n" + 
			"   \n\n bubbles: true,\r\n";
	
	@Test
	public void testOutputNOT_NULL() {
		Assert.assertNotNull(ViewUtil.inputComponentHtmlFormatter(someHtml));
		Assert.fail("Some String must be passed to the method");
	}
	
	@Test
	public void testOutputContainsExcessChars() {
		if (ViewUtil.inputComponentHtmlFormatter(someHtml).contains("\"") || 
			ViewUtil.inputComponentHtmlFormatter(someHtml).contains("\n") ||
			ViewUtil.inputComponentHtmlFormatter(someHtml).contains("[]")) {
			Assert.fail("Method rules of replacement had been changed");
		}
	}
}
