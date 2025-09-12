package org.commonmark.ext.footnotes;

import org.commonmark.Extension;
import org.commonmark.node.*;
import org.commonmark.parser.IncludeSourceSpans;
import org.commonmark.parser.Parser;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

public class FootnotesTest {

    private static final Set<Extension> EXTENSIONS = Set.of(FootnotesExtension.create());
    private static final Parser PARSER = Parser.builder().extensions(EXTENSIONS).build();

    @Test
    public void testDefBlockStart() {
        for (var s : List.of("1", "a", "^", "*", "\\a", "\uD83D\uDE42", "&0")) {
            var doc = PARSER.parse("[^" + s + "]: footnote\n");
            var def = find(doc, FootnoteDefinition.class);
            assertThat(def.getLabel()).isEqualTo(s);
        }

        for (var s : List.of("", " ", "a b", "]", "\r", "\n", "\t")) {
            var input = "[^" + s + "]: footnote\n";
            var doc = PARSER.parse(input);
            assertThat(tryFind(doc, FootnoteDefinition.class)).as("input: " + input).isNull();
        }
    }

    @Test
    public void testDefBlockStartInterrupts() {
        // This is different from a link reference definition, which can only be at the start of paragraphs.
        var doc = PARSER.parse("test\n[^1]: footnote\n");
        var paragraph = find(doc, Paragraph.class);
        var def = find(doc, FootnoteDefinition.class);
        assertThat(((Text) paragraph.getLastChild()).getLiteral()).isEqualTo("test");
        assertThat(def.getLabel()).isEqualTo("1");
    }

    @Test
    public void testDefBlockStartIndented() {
        var doc1 = PARSER.parse("   [^1]: footnote\n");
        assertThat(find(doc1, FootnoteDefinition.class).getLabel()).isEqualTo("1");
        var doc2 = PARSER.parse("    [^1]: footnote\n");
        assertNone(doc2, FootnoteDefinition.class);
    }

    @Test
    public void testDefMultiple() {
        var doc = PARSER.parse("[^1]: foo\n[^2]: bar\n");
        var defs = findAll(doc, FootnoteDefinition.class);
        assertThat(defs.get(0).getLabel()).isEqualTo("1");
        assertThat(defs.get(1).getLabel()).isEqualTo("2");
    }

    @Test
    public void testDefBlockStartAfterLinkReferenceDefinition() {
        var doc = PARSER.parse("[foo]: /url\n[^1]: footnote\n");
        var linkReferenceDef = find(doc, LinkReferenceDefinition.class);
        var footnotesDef = find(doc, FootnoteDefinition.class);
        assertThat(linkReferenceDef.getLabel()).isEqualTo("foo");
        assertThat(footnotesDef.getLabel()).isEqualTo("1");
    }

    @Test
    public void testDefContainsParagraph() {
        var doc = PARSER.parse("[^1]: footnote\n");
        var def = find(doc, FootnoteDefinition.class);
        var paragraph = (Paragraph) def.getFirstChild();
        assertText("footnote", paragraph.getFirstChild());
    }

    @Test
    public void testDefBlockStartSpacesAfterColon() {
        var doc = PARSER.parse("[^1]:        footnote\n");
        var def = find(doc, FootnoteDefinition.class);
        var paragraph = (Paragraph) def.getFirstChild();
        assertText("footnote", paragraph.getFirstChild());
    }

    @Test
    public void testDefContainsIndentedCodeBlock() {
        var doc = PARSER.parse("[^1]:\n        code\n");
        var def = find(doc, FootnoteDefinition.class);
        var codeBlock = (IndentedCodeBlock) def.getFirstChild();
        assertThat(codeBlock.getLiteral()).isEqualTo("code\n");
    }

    @Test
    public void testDefContainsMultipleLines() {
        var doc = PARSER.parse("[^1]: footnote\nstill\n");
        var def = find(doc, FootnoteDefinition.class);
        assertThat(def.getLabel()).isEqualTo("1");
        var paragraph = (Paragraph) def.getFirstChild();
        assertText("footnote", paragraph.getFirstChild());
        assertText("still", paragraph.getLastChild());
    }

    @Test
    public void testDefContainsMultipleParagraphs() {
        var doc = PARSER.parse("[^1]: footnote p1\n\n    footnote p2\n");
        var def = find(doc, FootnoteDefinition.class);
        assertThat(def.getLabel()).isEqualTo("1");
        var p1 = (Paragraph) def.getFirstChild();
        assertText("footnote p1", p1.getFirstChild());
        var p2 = (Paragraph) p1.getNext();
        assertText("footnote p2", p2.getFirstChild());
    }

    @Test
    public void testDefFollowedByParagraph() {
        var doc = PARSER.parse("[^1]: footnote\n\nnormal paragraph\n");
        var def = find(doc, FootnoteDefinition.class);
        assertThat(def.getLabel()).isEqualTo("1");
        assertText("footnote", def.getFirstChild().getFirstChild());
        assertText("normal paragraph", def.getNext().getFirstChild());
    }

    @Test
    public void testDefContainsList() {
        var doc = PARSER.parse("[^1]: - foo\n    - bar\n");
        var def = find(doc, FootnoteDefinition.class);
        assertThat(def.getLabel()).isEqualTo("1");
        var list = (BulletList) def.getFirstChild();
        var item1 = (ListItem) list.getFirstChild();
        var item2 = (ListItem) list.getLastChild();
        assertText("foo", item1.getFirstChild().getFirstChild());
        assertText("bar", item2.getFirstChild().getFirstChild());
    }

    @Test
    public void testDefInterruptedByOthers() {
        var doc = PARSER.parse("[^1]: footnote\n# Heading\n");
        var def = find(doc, FootnoteDefinition.class);
        var heading = find(doc, Heading.class);
        assertThat(def.getLabel()).isEqualTo("1");
        assertText("Heading", heading.getFirstChild());
    }

    @Test
    public void testReference() {
        var doc = PARSER.parse("Test [^foo]\n\n[^foo]: /url\n");
        var ref = find(doc, FootnoteReference.class);
        assertThat(ref.getLabel()).isEqualTo("foo");
    }

    @Test
    public void testReferenceNoDefinition() {
        var doc = PARSER.parse("Test [^foo]\n");
        assertNone(doc, FootnoteReference.class);
    }

    @Test
    public void testRefWithEmphasisInside() {
        // No emphasis inside footnote reference, should just be treated as text
        var doc = PARSER.parse("Test [^*foo*]\n\n[^*foo*]: def\n");
        var ref = find(doc, FootnoteReference.class);
        assertThat(ref.getLabel()).isEqualTo("*foo*");
        assertThat(ref.getFirstChild()).isNull();
        var paragraph = doc.getFirstChild();
        var text = (Text) paragraph.getFirstChild();
        assertThat(text.getLiteral()).isEqualTo("Test ");
        assertThat(text.getNext()).isEqualTo(ref);
        assertThat(paragraph.getLastChild()).isEqualTo(ref);
    }

    @Test
    public void testRefWithEmphasisAround() {
        // Emphasis around footnote reference, the * inside needs to be removed from emphasis processing
        var doc = PARSER.parse("Test *abc [^foo*] def*\n\n[^foo*]: def\n");
        var ref = find(doc, FootnoteReference.class);
        assertThat(ref.getLabel()).isEqualTo("foo*");
        assertText("abc ", ref.getPrevious());
        assertText(" def", ref.getNext());
        var em = find(doc, Emphasis.class);
        assertThat(ref.getParent()).isEqualTo(em);
    }

    @Test
    public void testRefAfterBang() {
        var doc = PARSER.parse("Test![^foo]\n\n[^foo]: def\n");
        var ref = find(doc, FootnoteReference.class);
        assertThat(ref.getLabel()).isEqualTo("foo");
        var paragraph = doc.getFirstChild();
        assertText("Test!", paragraph.getFirstChild());
    }

    @Test
    public void testRefAsLabelOnly() {
        // [^bar] is a footnote but [foo] is just text, because full reference links (text `foo`, label `^bar`) don't
        // resolve as footnotes. If `[foo][^bar]` fails to parse as a bracket, `[^bar]` by itself needs to be tried.
        var doc = PARSER.parse("Test [foo][^bar]\n\n[^bar]: footnote\n");
        var ref = find(doc, FootnoteReference.class);
        assertThat(ref.getLabel()).isEqualTo("bar");
        var paragraph = doc.getFirstChild();
        assertText("Test [foo]", paragraph.getFirstChild());
    }

    @Test
    public void testRefWithEmptyLabel() {
        // [^bar] is a footnote but [] is just text, because collapsed reference links don't resolve as footnotes
        var doc = PARSER.parse("Test [^bar][]\n\n[^bar]: footnote\n");
        var ref = find(doc, FootnoteReference.class);
        assertThat(ref.getLabel()).isEqualTo("bar");
        var paragraph = doc.getFirstChild();
        assertText("Test ", paragraph.getFirstChild());
        assertText("[]", paragraph.getLastChild());
    }

    @Test
    public void testRefWithBracket() {
        // Not a footnote, [ needs to be escaped
        var doc = PARSER.parse("Test [^f[oo]\n\n[^f[oo]: /url\n");
        assertNone(doc, FootnoteReference.class);
    }

    @Test
    public void testRefWithBackslash() {
        var doc = PARSER.parse("[^\\foo]\n\n[^\\foo]: note\n");
        var ref = find(doc, FootnoteReference.class);
        assertThat(ref.getLabel()).isEqualTo("\\foo");
        var def = find(doc, FootnoteDefinition.class);
        assertThat(def.getLabel()).isEqualTo("\\foo");
    }

    @Test
    public void testPreferInlineLink() {
        var doc = PARSER.parse("Test [^bar](/url)\n\n[^bar]: footnote\n");
        assertNone(doc, FootnoteReference.class);
    }

    @Test
    public void testPreferReferenceLink() {
        // This is tricky because `[^*foo*][foo]` is a valid link already. If `[foo]` was not defined, the first bracket
        // would be a footnote.
        var doc = PARSER.parse("Test [^*foo*][foo]\n\n[^*foo*]: /url\n\n[foo]: /url");
        assertNone(doc, FootnoteReference.class);
    }

    @Test
    public void testReferenceLinkWithoutDefinition() {
        // Similar to previous test but there's no definition
        var doc = PARSER.parse("Test [^*foo*][foo]\n\n[^*foo*]: def\n");
        var ref = find(doc, FootnoteReference.class);
        assertThat(ref.getLabel()).isEqualTo("*foo*");
        var paragraph = (Paragraph) doc.getFirstChild();
        assertText("Test ", paragraph.getFirstChild());
        assertText("[foo]", paragraph.getLastChild());
    }

    @Test
    public void testFootnoteInLink() {
        // Expected to behave the same way as a link within a link, see https://spec.commonmark.org/0.31.2/#example-518
        // i.e. the first (inner) link is parsed, which means the outer one becomes plain text, as nesting links is not
        // allowed.
        var doc = PARSER.parse("[link with footnote ref [^1]](https://example.com)\n\n[^1]: footnote\n");
        var ref = find(doc, FootnoteReference.class);
        assertThat(ref.getLabel()).isEqualTo("1");
        var paragraph = doc.getFirstChild();
        assertText("[link with footnote ref ", paragraph.getFirstChild());
        assertText("](https://example.com)", paragraph.getLastChild());
    }

    @Test
    public void testFootnoteWithMarkerInLink() {
        var doc = PARSER.parse("[link with footnote ref ![^1]](https://example.com)\n\n[^1]: footnote\n");
        var ref = find(doc, FootnoteReference.class);
        assertThat(ref.getLabel()).isEqualTo("1");
        var paragraph = doc.getFirstChild();
        assertText("[link with footnote ref !", paragraph.getFirstChild());
        assertText("](https://example.com)", paragraph.getLastChild());
    }

    @Test
    public void testInlineFootnote() {
        var extension = FootnotesExtension.builder().inlineFootnotes(true).build();
        var parser = Parser.builder().extensions(Set.of(extension)).build();

        {
            var doc = parser.parse("Test ^[inline footnote]");
            assertText("Test ", doc.getFirstChild().getFirstChild());
            var fn = find(doc, InlineFootnote.class);
            assertText("inline footnote", fn.getFirstChild());
        }

        {
            var doc = parser.parse("Test \\^[not inline footnote]");
            assertNone(doc, InlineFootnote.class);
        }

        {
            var doc = parser.parse("Test ^[not inline footnote");
            assertNone(doc, InlineFootnote.class);
            var t = doc.getFirstChild().getFirstChild();
            assertText("Test ^[not inline footnote", t);
        }

        {
            // This is a tricky one because the code span in the link text
            // includes the `]` (and doesn't need to be escaped). Therefore
            // inline footnote parsing has to do full link text parsing/inline parsing.
            // https://spec.commonmark.org/0.31.2/#link-text

            var doc = parser.parse("^[test `bla]`]");
            var fn = find(doc, InlineFootnote.class);
            assertText("test ", fn.getFirstChild());
            var code = fn.getFirstChild().getNext();
            assertThat(((Code) code).getLiteral()).isEqualTo("bla]");
        }

        {
            var doc = parser.parse("^[with a [link](url)]");
            var fn = find(doc, InlineFootnote.class);
            assertText("with a ", fn.getFirstChild());
            var link = fn.getFirstChild().getNext();
            assertThat(((Link) link).getDestination()).isEqualTo("url");
        }
    }

    @Test
    public void testSourcePositions() {
        var parser = Parser.builder().extensions(EXTENSIONS).includeSourceSpans(IncludeSourceSpans.BLOCKS_AND_INLINES).build();

        var doc = parser.parse("Test [^foo]\n\n[^foo]: /url\n");
        var ref = find(doc, FootnoteReference.class);
        assertThat(ref.getSourceSpans()).isEqualTo(List.of(SourceSpan.of(0, 5, 5, 6)));

        var def = find(doc, FootnoteDefinition.class);
        assertThat(def.getSourceSpans()).isEqualTo(List.of(SourceSpan.of(2, 0, 13, 12)));
    }

    private static void assertNone(Node parent, Class<?> nodeClass) {
        assertThat(tryFind(parent, nodeClass)).as(() -> "Node " + parent + " containing " + nodeClass).isNull();
    }

    private static <T> T find(Node parent, Class<T> nodeClass) {
        return Objects.requireNonNull(tryFind(parent, nodeClass), "Could not find a " + nodeClass.getSimpleName() + " node in " + parent);
    }

    private static <T> T tryFind(Node parent, Class<T> nodeClass) {
        return findAll(parent, nodeClass).stream().findFirst().orElse(null);
    }

    private static <T> List<T> findAll(Node parent, Class<T> nodeClass) {
        var nodes = new ArrayList<T>();
        for (var node = parent.getFirstChild(); node != null; node = node.getNext()) {
            if (nodeClass.isInstance(node)) {
                //noinspection unchecked
                nodes.add((T) node);
            }
            nodes.addAll(findAll(node, nodeClass));
        }
        return nodes;
    }

    private static void assertText(String expected, Node node) {
        var text = (Text) node;
        assertThat(text.getLiteral()).isEqualTo(expected);
    }
}
