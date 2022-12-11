package org.commonmark.ext.gfm.tables;

import org.commonmark.Extension;
import org.commonmark.node.Node;
import org.commonmark.parser.Parser;
import org.commonmark.renderer.text.TextContentRenderer;
import org.junit.Test;

import java.util.Collections;
import java.util.Set;

import static org.junit.Assert.assertEquals;

public class BugReproductionTest {

    private static final Set<Extension> EXTENSIONS = Collections.singleton(TablesExtension.create());
    private static final Parser PARSER = Parser.builder().extensions(EXTENSIONS).build();
    private static final TextContentRenderer RENDERER = TextContentRenderer.builder().extensions(EXTENSIONS).build();

    @Test
    public void test() {
        String source = "| column |\n| --- |\n| value |";
        Node node = PARSER.parse(source);
        assertIsTableBlock(node);

        String rendered = RENDERER.render(node);
        Node fromRender = PARSER.parse(rendered);
        assertIsTableBlock(fromRender); //fails here
    }

    private static void assertIsTableBlock(Node node) {
        //root node is always document
        Node child = node.getFirstChild();
        assertEquals(node.toString(), TableBlock.class, child.getClass());
    }
}
