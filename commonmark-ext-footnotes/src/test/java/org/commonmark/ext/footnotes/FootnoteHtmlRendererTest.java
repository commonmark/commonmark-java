package org.commonmark.ext.footnotes;

import java.util.List;
import java.util.Set;
import org.commonmark.Extension;
import org.commonmark.node.Document;
import org.commonmark.node.Paragraph;
import org.commonmark.node.Text;
import org.commonmark.parser.Parser;
import org.commonmark.renderer.html.HtmlRenderer;
import org.commonmark.testutil.Asserts;
import org.commonmark.testutil.RenderingTestCase;
import org.junit.jupiter.api.Test;

public class FootnoteHtmlRendererTest extends RenderingTestCase {
    private static final Set<Extension> EXTENSIONS = Set.of(FootnotesExtension.create());
    private static final Parser PARSER = Parser.builder().extensions(EXTENSIONS).build();
    private static final HtmlRenderer RENDERER =
            HtmlRenderer.builder().extensions(EXTENSIONS).build();

    @Test
    public void testOne() {
        assertRendering(
                """
                Test [^foo]

                [^foo]: note
                """,
                """
                <p>Test <sup class="footnote-ref"><a href="#fn-foo" id="fnref-foo" data-footnote-ref>1</a></sup></p>
                <section class="footnotes" data-footnotes>
                <ol>
                <li id="fn-foo">
                <p>note <a href="#fnref-foo" class="footnote-backref" data-footnote-backref data-footnote-backref-idx="1" aria-label="Back to reference 1">↩</a></p>
                </li>
                </ol>
                </section>
                """);
    }

    @Test
    public void testLabelNormalization() {
        // Labels match via their normalized form. For the href and IDs to match, rendering needs to
        // use the label from the definition consistently.
        assertRendering(
                """
                Test [^bar]

                [^BAR]: note
                """,
                """
                <p>Test <sup class="footnote-ref"><a href="#fn-BAR" id="fnref-BAR" data-footnote-ref>1</a></sup></p>
                <section class="footnotes" data-footnotes>
                <ol>
                <li id="fn-BAR">
                <p>note <a href="#fnref-BAR" class="footnote-backref" data-footnote-backref data-footnote-backref-idx="1" aria-label="Back to reference 1">↩</a></p>
                </li>
                </ol>
                </section>
                """);
    }

    @Test
    public void testMultipleReferences() {
        // Tests a few things:
        // - Numbering is based on the reference order, not the definition order
        // - The same number is used when a definition is referenced multiple times
        // - Multiple backrefs are rendered
        assertRendering(
                """
                First [^foo]

                Then [^bar]

                Then [^foo] again

                [^bar]: b
                [^foo]: f
                """,
                """
                <p>First <sup class="footnote-ref"><a href="#fn-foo" id="fnref-foo" data-footnote-ref>1</a></sup></p>
                <p>Then <sup class="footnote-ref"><a href="#fn-bar" id="fnref-bar" data-footnote-ref>2</a></sup></p>
                <p>Then <sup class="footnote-ref"><a href="#fn-foo" id="fnref-foo-2" data-footnote-ref>1</a></sup> again</p>
                <section class="footnotes" data-footnotes>
                <ol>
                <li id="fn-foo">
                <p>f <a href="#fnref-foo" class="footnote-backref" data-footnote-backref data-footnote-backref-idx="1" aria-label="Back to reference 1">↩</a> <a href="#fnref-foo-2" class="footnote-backref" data-footnote-backref data-footnote-backref-idx="1-2" aria-label="Back to reference 1-2"><sup class="footnote-ref">2</sup>↩</a></p>
                </li>
                <li id="fn-bar">
                <p>b <a href="#fnref-bar" class="footnote-backref" data-footnote-backref data-footnote-backref-idx="2" aria-label="Back to reference 2">↩</a></p>
                </li>
                </ol>
                </section>
                """);
    }

    @Test
    public void testDefinitionWithTwoParagraphs() {
        // With two paragraphs, the backref <a> should be added to the second one
        assertRendering(
                """
                Test [^foo]

                [^foo]: one
                   \s
                    two
                """,
                """
                <p>Test <sup class="footnote-ref"><a href="#fn-foo" id="fnref-foo" data-footnote-ref>1</a></sup></p>
                <section class="footnotes" data-footnotes>
                <ol>
                <li id="fn-foo">
                <p>one</p>
                <p>two <a href="#fnref-foo" class="footnote-backref" data-footnote-backref data-footnote-backref-idx="1" aria-label="Back to reference 1">↩</a></p>
                </li>
                </ol>
                </section>
                """);
    }

    @Test
    public void testDefinitionWithList() {
        assertRendering(
                """
                Test [^foo]

                [^foo]:
                    - one
                    - two
                """,
                """
                <p>Test <sup class="footnote-ref"><a href="#fn-foo" id="fnref-foo" data-footnote-ref>1</a></sup></p>
                <section class="footnotes" data-footnotes>
                <ol>
                <li id="fn-foo">
                <ul>
                <li>one</li>
                <li>two</li>
                </ul>
                <a href="#fnref-foo" class="footnote-backref" data-footnote-backref data-footnote-backref-idx="1" aria-label="Back to reference 1">↩</a></li>
                </ol>
                </section>
                """);
    }

    // See docs on FootnoteHtmlNodeRenderer about nested footnotes.

    @Test
    public void testNestedFootnotesSimple() {
        assertRendering(
                """
                [^foo1]

                [^foo1]: one [^foo2]
                [^foo2]: two
                """,
                """
                <p><sup class="footnote-ref"><a href="#fn-foo1" id="fnref-foo1" data-footnote-ref>1</a></sup></p>
                <section class="footnotes" data-footnotes>
                <ol>
                <li id="fn-foo1">
                <p>one <sup class="footnote-ref"><a href="#fn-foo2" id="fnref-foo2" data-footnote-ref>2</a></sup> <a href="#fnref-foo1" class="footnote-backref" data-footnote-backref data-footnote-backref-idx="1" aria-label="Back to reference 1">↩</a></p>
                </li>
                <li id="fn-foo2">
                <p>two <a href="#fnref-foo2" class="footnote-backref" data-footnote-backref data-footnote-backref-idx="2" aria-label="Back to reference 2">↩</a></p>
                </li>
                </ol>
                </section>
                """);
    }

    @Test
    public void testNestedFootnotesOrder() {
        // GitHub has a strange result here, the definitions are in order: 1. bar, 2. foo.
        // The reason is that the number is done based on all references in document order,
        // including references in definitions. So [^bar] from the first line is first.
        assertRendering(
                """
                [^foo]: foo [^bar]

                [^foo]

                [^bar]: bar
                """,
                """
                <p><sup class="footnote-ref"><a href="#fn-foo" id="fnref-foo" data-footnote-ref>1</a></sup></p>
                <section class="footnotes" data-footnotes>
                <ol>
                <li id="fn-foo">
                <p>foo <sup class="footnote-ref"><a href="#fn-bar" id="fnref-bar" data-footnote-ref>2</a></sup> <a href="#fnref-foo" class="footnote-backref" data-footnote-backref data-footnote-backref-idx="1" aria-label="Back to reference 1">↩</a></p>
                </li>
                <li id="fn-bar">
                <p>bar <a href="#fnref-bar" class="footnote-backref" data-footnote-backref data-footnote-backref-idx="2" aria-label="Back to reference 2">↩</a></p>
                </li>
                </ol>
                </section>
                """);
    }

    @Test
    public void testNestedFootnotesOrder2() {
        assertRendering(
                """
                [^1]

                [^4]: four
                [^3]: three [^4]
                [^2]: two [^4]
                [^1]: one [^2][^3]
                """,
                """
                <p><sup class="footnote-ref"><a href="#fn-1" id="fnref-1" data-footnote-ref>1</a></sup></p>
                <section class="footnotes" data-footnotes>
                <ol>
                <li id="fn-1">
                <p>one <sup class="footnote-ref"><a href="#fn-2" id="fnref-2" data-footnote-ref>2</a></sup><sup class="footnote-ref"><a href="#fn-3" id="fnref-3" data-footnote-ref>3</a></sup> <a href="#fnref-1" class="footnote-backref" data-footnote-backref data-footnote-backref-idx="1" aria-label="Back to reference 1">↩</a></p>
                </li>
                <li id="fn-2">
                <p>two <sup class="footnote-ref"><a href="#fn-4" id="fnref-4" data-footnote-ref>4</a></sup> <a href="#fnref-2" class="footnote-backref" data-footnote-backref data-footnote-backref-idx="2" aria-label="Back to reference 2">↩</a></p>
                </li>
                <li id="fn-3">
                <p>three <sup class="footnote-ref"><a href="#fn-4" id="fnref-4-2" data-footnote-ref>4</a></sup> <a href="#fnref-3" class="footnote-backref" data-footnote-backref data-footnote-backref-idx="3" aria-label="Back to reference 3">↩</a></p>
                </li>
                <li id="fn-4">
                <p>four <a href="#fnref-4" class="footnote-backref" data-footnote-backref data-footnote-backref-idx="4" aria-label="Back to reference 4">↩</a> <a href="#fnref-4-2" class="footnote-backref" data-footnote-backref data-footnote-backref-idx="4-2" aria-label="Back to reference 4-2"><sup class="footnote-ref">2</sup>↩</a></p>
                </li>
                </ol>
                </section>
                """);
    }

    @Test
    public void testNestedFootnotesCycle() {
        // Footnotes can contain cycles, lol.
        assertRendering(
                """
                [^foo1]

                [^foo1]: one [^foo2]
                [^foo2]: two [^foo1]
                """,
                """
                <p><sup class="footnote-ref"><a href="#fn-foo1" id="fnref-foo1" data-footnote-ref>1</a></sup></p>
                <section class="footnotes" data-footnotes>
                <ol>
                <li id="fn-foo1">
                <p>one <sup class="footnote-ref"><a href="#fn-foo2" id="fnref-foo2" data-footnote-ref>2</a></sup> <a href="#fnref-foo1" class="footnote-backref" data-footnote-backref data-footnote-backref-idx="1" aria-label="Back to reference 1">↩</a> <a href="#fnref-foo1-2" class="footnote-backref" data-footnote-backref data-footnote-backref-idx="1-2" aria-label="Back to reference 1-2"><sup class="footnote-ref">2</sup>↩</a></p>
                </li>
                <li id="fn-foo2">
                <p>two <sup class="footnote-ref"><a href="#fn-foo1" id="fnref-foo1-2" data-footnote-ref>1</a></sup> <a href="#fnref-foo2" class="footnote-backref" data-footnote-backref data-footnote-backref-idx="2" aria-label="Back to reference 2">↩</a></p>
                </li>
                </ol>
                </section>
                """);
    }

    @Test
    public void testNestedFootnotesUnreferenced() {
        // This should not result in any footnotes, as baz itself isn't referenced.
        // But GitHub renders bar only, with a broken backref, because bar is referenced from foo.
        assertRendering(
                """
                [^foo]: foo[^bar]
                [^bar]: bar
                """,
                "");

        // And here only 1 is rendered.
        assertRendering(
                """
                [^1]

                [^1]: one
                [^foo]: foo[^bar]
                [^bar]: bar
                """,
                """
                <p><sup class="footnote-ref"><a href="#fn-1" id="fnref-1" data-footnote-ref>1</a></sup></p>
                <section class="footnotes" data-footnotes>
                <ol>
                <li id="fn-1">
                <p>one <a href="#fnref-1" class="footnote-backref" data-footnote-backref data-footnote-backref-idx="1" aria-label="Back to reference 1">↩</a></p>
                </li>
                </ol>
                </section>
                """);
    }

    @Test
    public void testInlineFootnotes() {
        assertRenderingInline(
                "Test ^[inline *footnote*]",
                """
                <p>Test <sup class="footnote-ref"><a href="#fn1" id="fnref1" data-footnote-ref>1</a></sup></p>
                <section class="footnotes" data-footnotes>
                <ol>
                <li id="fn1">
                <p>inline <em>footnote</em> <a href="#fnref1" class="footnote-backref" data-footnote-backref data-footnote-backref-idx="1" aria-label="Back to reference 1">↩</a></p>
                </li>
                </ol>
                </section>
                """);
    }

    @Test
    public void testInlineFootnotesNested() {
        assertRenderingInline(
                "Test ^[inline ^[nested]]",
                """
                <p>Test <sup class="footnote-ref"><a href="#fn1" id="fnref1" data-footnote-ref>1</a></sup></p>
                <section class="footnotes" data-footnotes>
                <ol>
                <li id="fn1">
                <p>inline <sup class="footnote-ref"><a href="#fn2" id="fnref2" data-footnote-ref>2</a></sup> <a href="#fnref1" class="footnote-backref" data-footnote-backref data-footnote-backref-idx="1" aria-label="Back to reference 1">↩</a></p>
                </li>
                <li id="fn2">
                <p>nested <a href="#fnref2" class="footnote-backref" data-footnote-backref data-footnote-backref-idx="2" aria-label="Back to reference 2">↩</a></p>
                </li>
                </ol>
                </section>
                """);
    }

    @Test
    public void testInlineFootnoteWithReference() {
        // This is a bit tricky because the IDs need to be unique.
        assertRenderingInline(
                """
                Test ^[inline [^1]]

                [^1]: normal""",
                """
                <p>Test <sup class="footnote-ref"><a href="#fn1" id="fnref1" data-footnote-ref>1</a></sup></p>
                <section class="footnotes" data-footnotes>
                <ol>
                <li id="fn1">
                <p>inline <sup class="footnote-ref"><a href="#fn-1" id="fnref-1" data-footnote-ref>2</a></sup> <a href="#fnref1" class="footnote-backref" data-footnote-backref data-footnote-backref-idx="1" aria-label="Back to reference 1">↩</a></p>
                </li>
                <li id="fn-1">
                <p>normal <a href="#fnref-1" class="footnote-backref" data-footnote-backref data-footnote-backref-idx="2" aria-label="Back to reference 2">↩</a></p>
                </li>
                </ol>
                </section>
                """);
    }

    @Test
    public void testInlineFootnoteInsideDefinition() {
        assertRenderingInline(
                """
                Test [^1]

                [^1]: Definition ^[inline]
                """,
                """
                <p>Test <sup class="footnote-ref"><a href="#fn-1" id="fnref-1" data-footnote-ref>1</a></sup></p>
                <section class="footnotes" data-footnotes>
                <ol>
                <li id="fn-1">
                <p>Definition <sup class="footnote-ref"><a href="#fn2" id="fnref2" data-footnote-ref>2</a></sup> <a href="#fnref-1" class="footnote-backref" data-footnote-backref data-footnote-backref-idx="1" aria-label="Back to reference 1">↩</a></p>
                </li>
                <li id="fn2">
                <p>inline <a href="#fnref2" class="footnote-backref" data-footnote-backref data-footnote-backref-idx="2" aria-label="Back to reference 2">↩</a></p>
                </li>
                </ol>
                </section>
                """);
    }

    @Test
    public void testInlineFootnoteInsideDefinition2() {
        // Tricky because of the nested inline footnote which we want to visit after foo
        // (breadth-first).
        assertRenderingInline(
                """
                Test [^1]

                [^1]: Definition ^[inline ^[nested]] ^[foo]
                """,
                """
                <p>Test <sup class="footnote-ref"><a href="#fn-1" id="fnref-1" data-footnote-ref>1</a></sup></p>
                <section class="footnotes" data-footnotes>
                <ol>
                <li id="fn-1">
                <p>Definition <sup class="footnote-ref"><a href="#fn2" id="fnref2" data-footnote-ref>2</a></sup> <sup class="footnote-ref"><a href="#fn3" id="fnref3" data-footnote-ref>3</a></sup> <a href="#fnref-1" class="footnote-backref" data-footnote-backref data-footnote-backref-idx="1" aria-label="Back to reference 1">↩</a></p>
                </li>
                <li id="fn2">
                <p>inline <sup class="footnote-ref"><a href="#fn4" id="fnref4" data-footnote-ref>4</a></sup> <a href="#fnref2" class="footnote-backref" data-footnote-backref data-footnote-backref-idx="2" aria-label="Back to reference 2">↩</a></p>
                </li>
                <li id="fn3">
                <p>foo <a href="#fnref3" class="footnote-backref" data-footnote-backref data-footnote-backref-idx="3" aria-label="Back to reference 3">↩</a></p>
                </li>
                <li id="fn4">
                <p>nested <a href="#fnref4" class="footnote-backref" data-footnote-backref data-footnote-backref-idx="4" aria-label="Back to reference 4">↩</a></p>
                </li>
                </ol>
                </section>
                """);
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

        var expected =
                """
                <p>Test <sup class="footnote-ref"><a href="#fn-foo" id="fnref-foo" data-footnote-ref>1</a></sup></p>
                <section class="footnotes" data-footnotes>
                <ol>
                <li id="fn-foo">
                <p>note! <a href="#fnref-foo" class="footnote-backref" data-footnote-backref data-footnote-backref-idx="1" aria-label="Back to reference 1">↩</a></p>
                </li>
                </ol>
                </section>
                """;
        Asserts.assertRendering("", expected, RENDERER.render(doc));
    }

    @Override
    protected String render(String source) {
        return RENDERER.render(PARSER.parse(source));
    }

    private static void assertRenderingInline(String source, String expected) {
        var extension = FootnotesExtension.builder().inlineFootnotes(true).build();
        var parser = Parser.builder().extensions(List.of(extension)).build();
        var renderer = HtmlRenderer.builder().extensions(List.of(extension)).build();
        Asserts.assertRendering(source, expected, renderer.render(parser.parse(source)));
    }
}
