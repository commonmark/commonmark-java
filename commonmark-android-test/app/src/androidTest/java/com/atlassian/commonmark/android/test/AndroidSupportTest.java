package com.atlassian.commonmark.android.test;

import org.commonmark.Extension;
import org.commonmark.ext.autolink.AutolinkExtension;
import org.commonmark.ext.front.matter.YamlFrontMatterExtension;
import org.commonmark.ext.gfm.strikethrough.StrikethroughExtension;
import org.commonmark.ext.gfm.tables.TablesExtension;
import org.commonmark.ext.heading.anchor.HeadingAnchorExtension;
import org.commonmark.ext.ins.InsExtension;
import org.commonmark.node.Node;
import org.commonmark.parser.Parser;
import org.commonmark.renderer.html.HtmlRenderer;
import org.commonmark.testutil.TestResources;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;

import android.support.test.runner.AndroidJUnit4;
import android.test.suitebuilder.annotation.SmallTest;

import java.util.Collections;

import static org.junit.Assert.assertNotNull;

@RunWith(AndroidJUnit4.class)
@SmallTest
public class AndroidSupportTest {

    private String spec;

    @Before
    public void setUp() throws Exception {
        spec = TestResources.readAsString(TestResources.getSpec());
    }

    @Test
    public void parseTest() throws Exception {
        Parser parser = new Parser.Builder().build();

        Node document = parser.parse(spec);

        assertNotNull(document);
    }

    @Test
    public void autolinkExtensionTest() throws Exception {
        parseWithExtensionsTest(AutolinkExtension.create());
    }

    @Test
    public void strikethroughExtensionTest() throws Exception {
        parseWithExtensionsTest(StrikethroughExtension.create());
    }

    @Test
    public void tablesExtensionTest() throws Exception {
        parseWithExtensionsTest(TablesExtension.create());
    }

    @Test
    public void headingAnchorExtensionTest() throws Exception {
        parseWithExtensionsTest(HeadingAnchorExtension.create());
    }

    @Test
    public void insExtensionTest() throws Exception {
        parseWithExtensionsTest(InsExtension.create());
    }

    @Test
    public void yamlFrontMatterExtensionTest() throws Exception {
        parseWithExtensionsTest(YamlFrontMatterExtension.create());
    }

    @Test
    public void htmlRendererTest() throws Exception {
        Parser parser = Parser.builder().build();
        HtmlRenderer renderer = HtmlRenderer.builder().build();

        String renderedString = renderer.render(parser.parse(spec));

        assertNotNull(renderedString);
    }

    private void parseWithExtensionsTest(Extension extension) throws Exception {
        Parser parser = Parser.builder()
                .extensions(Collections.singletonList(extension))
                .build();

        Node document = parser.parse(spec);
        assertNotNull(document);

        HtmlRenderer renderer = HtmlRenderer.builder()
                .extensions(Collections.singletonList(extension))
                .build();

        String renderedString = renderer.render(document);
        assertNotNull(renderedString);
    }
}
