package org.commonmark.ext.footnotes;

import org.commonmark.Extension;
import org.commonmark.node.Document;
import org.commonmark.node.Paragraph;
import org.commonmark.node.Text;
import org.commonmark.parser.Parser;
import org.commonmark.renderer.html.HtmlRenderer;
import org.commonmark.testutil.Asserts;
import org.commonmark.testutil.RenderingTestCase;
import org.junit.Test;

import java.util.Set;

public class FootnoteHtmlRendererTest extends RenderingTestCase {
    private static final Set<Extension> EXTENSIONS = Set.of(FootnotesExtension.create());
    private static final Parser PARSER = Parser.builder().extensions(EXTENSIONS).build();
    private static final HtmlRenderer RENDERER = HtmlRenderer.builder().extensions(EXTENSIONS).build();

    @Test
    public void testOne() {
        assertRendering("Test [^foo]\n\n[^foo]: note\n",
                "<p>Test <sup class=\"footnote-ref\"><a href=\"#fn-foo\" id=\"fnref-foo\" data-footnote-ref>1</a></sup></p>\n" +
                        "<section class=\"footnotes\" data-footnotes>\n" +
                        "<ol>\n" +
                        "<li id=\"fn-foo\">\n" +
                        "<p>note <a href=\"#fnref-foo\" class=\"footnote-backref\" data-footnote-backref data-footnote-backref-idx=\"1\" aria-label=\"Back to reference 1\">↩</a></p>\n" +
                        "</li>\n" +
                        "</ol>\n" +
                        "</section>\n");
    }

    @Test
    public void testLabelNormalization() {
        // Labels match via their normalized form. For the href and IDs to match, rendering needs to use the
        // label from the definition consistently.
        assertRendering("Test [^bar]\n\n[^BAR]: note\n",
                "<p>Test <sup class=\"footnote-ref\"><a href=\"#fn-BAR\" id=\"fnref-BAR\" data-footnote-ref>1</a></sup></p>\n" +
                        "<section class=\"footnotes\" data-footnotes>\n" +
                        "<ol>\n" +
                        "<li id=\"fn-BAR\">\n" +
                        "<p>note <a href=\"#fnref-BAR\" class=\"footnote-backref\" data-footnote-backref data-footnote-backref-idx=\"1\" aria-label=\"Back to reference 1\">↩</a></p>\n" +
                        "</li>\n" +
                        "</ol>\n" +
                        "</section>\n");
    }

    @Test
    public void testMultipleReferences() {
        // Tests a few things:
        // - Numbering is based on the reference order, not the definition order
        // - The same number is used when a definition is referenced multiple times
        // - Multiple backrefs are rendered
        assertRendering("First [^foo]\n\nThen [^bar]\n\nThen [^foo] again\n\n[^bar]: b\n[^foo]: f\n",
                "<p>First <sup class=\"footnote-ref\"><a href=\"#fn-foo\" id=\"fnref-foo\" data-footnote-ref>1</a></sup></p>\n" +
                        "<p>Then <sup class=\"footnote-ref\"><a href=\"#fn-bar\" id=\"fnref-bar\" data-footnote-ref>2</a></sup></p>\n" +
                        "<p>Then <sup class=\"footnote-ref\"><a href=\"#fn-foo\" id=\"fnref-foo-2\" data-footnote-ref>1</a></sup> again</p>\n" +
                        "<section class=\"footnotes\" data-footnotes>\n" +
                        "<ol>\n" +
                        "<li id=\"fn-foo\">\n" +
                        "<p>f <a href=\"#fnref-foo\" class=\"footnote-backref\" data-footnote-backref data-footnote-backref-idx=\"1\" aria-label=\"Back to reference 1\">↩</a> <a href=\"#fnref-foo-2\" class=\"footnote-backref\" data-footnote-backref data-footnote-backref-idx=\"1-2\" aria-label=\"Back to reference 1-2\"><sup class=\"footnote-ref\">2</sup>↩</a></p>\n" +
                        "</li>\n" +
                        "<li id=\"fn-bar\">\n" +
                        "<p>b <a href=\"#fnref-bar\" class=\"footnote-backref\" data-footnote-backref data-footnote-backref-idx=\"2\" aria-label=\"Back to reference 2\">↩</a></p>\n" +
                        "</li>\n" +
                        "</ol>\n" +
                        "</section>\n");
    }

    @Test
    public void testDefinitionWithTwoParagraphs() {
        // With two paragraphs, the backref <a> should be added to the second one
        assertRendering("Test [^foo]\n\n[^foo]: one\n    \n    two\n",
                "<p>Test <sup class=\"footnote-ref\"><a href=\"#fn-foo\" id=\"fnref-foo\" data-footnote-ref>1</a></sup></p>\n" +
                        "<section class=\"footnotes\" data-footnotes>\n" +
                        "<ol>\n" +
                        "<li id=\"fn-foo\">\n" +
                        "<p>one</p>\n" +
                        "<p>two <a href=\"#fnref-foo\" class=\"footnote-backref\" data-footnote-backref data-footnote-backref-idx=\"1\" aria-label=\"Back to reference 1\">↩</a></p>\n" +
                        "</li>\n" +
                        "</ol>\n" +
                        "</section>\n");
    }

    @Test
    public void testDefinitionWithList() {
        assertRendering("Test [^foo]\n\n[^foo]:\n    - one\n    - two\n",
                "<p>Test <sup class=\"footnote-ref\"><a href=\"#fn-foo\" id=\"fnref-foo\" data-footnote-ref>1</a></sup></p>\n" +
                        "<section class=\"footnotes\" data-footnotes>\n" +
                        "<ol>\n" +
                        "<li id=\"fn-foo\">\n" +
                        "<ul>\n" +
                        "<li>one</li>\n" +
                        "<li>two</li>\n" +
                        "</ul>\n" +
                        "<a href=\"#fnref-foo\" class=\"footnote-backref\" data-footnote-backref data-footnote-backref-idx=\"1\" aria-label=\"Back to reference 1\">↩</a></li>\n" +
                        "</ol>\n" +
                        "</section>\n");
    }

    @Test
    public void testRenderNodesDirectly() {
        // Everything should work as expected when rendering from nodes directly (no parsing step).
        var doc = new Document();
        var p = new Paragraph();
        p.appendChild(new Text("Test "));
        p.appendChild(new FootnoteReference("foo"));
        var def = new FootnoteDefinition("foo");
        var note = new Paragraph();
        note.appendChild(new Text("note!"));
        def.appendChild(note);
        doc.appendChild(p);
        doc.appendChild(def);

        var expected = "<p>Test <sup class=\"footnote-ref\"><a href=\"#fn-foo\" id=\"fnref-foo\" data-footnote-ref>1</a></sup></p>\n" +
                "<section class=\"footnotes\" data-footnotes>\n" +
                "<ol>\n" +
                "<li id=\"fn-foo\">\n" +
                "<p>note! <a href=\"#fnref-foo\" class=\"footnote-backref\" data-footnote-backref data-footnote-backref-idx=\"1\" aria-label=\"Back to reference 1\">↩</a></p>\n" +
                "</li>\n" +
                "</ol>\n" +
                "</section>\n";
        Asserts.assertRendering("", expected, RENDERER.render(doc));
    }

    @Override
    protected String render(String source) {
        return RENDERER.render(PARSER.parse(source));
    }
}
