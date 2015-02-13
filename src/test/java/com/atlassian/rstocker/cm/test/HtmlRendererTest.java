package com.atlassian.rstocker.cm.test;

import static org.junit.Assert.*;

import org.junit.Test;

import com.atlassian.rstocker.cm.DocParser;
import com.atlassian.rstocker.cm.HtmlRenderer;
import com.atlassian.rstocker.cm.Node;

public class HtmlRendererTest {

	@Test
	public void foo() {
		DocParser parser = new DocParser();
		Node node = parser.parse("foo *bar*");

		HtmlRenderer renderer = HtmlRenderer.builder().build();
		String result = renderer.render(node);

		assertEquals("\n<p>foo <em>bar</em></p>\n", result);
	}

}
