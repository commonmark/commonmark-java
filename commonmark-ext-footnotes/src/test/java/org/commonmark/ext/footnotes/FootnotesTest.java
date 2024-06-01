package org.commonmark.ext.footnotes;

import org.commonmark.Extension;
import org.commonmark.node.*;
import org.commonmark.parser.Parser;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Set;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

public class FootnotesTest {

    private static final Set<Extension> EXTENSIONS = Set.of(FootnotesExtension.create());
    private static final Parser PARSER = Parser.builder().extensions(EXTENSIONS).build();

    @Test
    public void testDefBlockStart() {
        for (var s : List.of("1", "a")) {
            var doc = PARSER.parse("[^" + s + "]: footnote\n");
            var def = find(doc, FootnoteDefinition.class);
            // TODO: Should label be "^1" instead?
            assertEquals(s, def.getLabel());
        }

        for (var s : List.of("", " ", "a b")) {
            var doc = PARSER.parse("[^" + s + "]: footnote\n");
            assertNull(tryFind(doc, FootnoteDefinition.class));
        }

        // TODO: Test what characters are allowed for the label, e.g.
        //  [^], [^ ], [^^], [^[], [^*], [^\], [^\a], [^🙂], tab?, [^&], [^&amp;]
    }

    @Test
    public void testDefBlockStartInterrupts() {
        // This is different from a link reference definition, which can only be at the start of paragraphs.
        var doc = PARSER.parse("test\n[^1]: footnote\n");
        var paragraph = find(doc, Paragraph.class);
        var def = find(doc, FootnoteDefinition.class);
        assertEquals("test", ((Text) paragraph.getLastChild()).getLiteral());
        assertEquals("1", def.getLabel());
    }

    @Test
    public void testDefMultiple() {
        var doc = PARSER.parse("[^1]: foo\n[^2]: bar\n");
        var defs = findAll(doc, FootnoteDefinition.class);
        assertEquals("1", defs.get(0).getLabel());
        assertEquals("2", defs.get(1).getLabel());
    }

    @Test
    public void testDefBlockStartAfterLinkReferenceDefinition() {
        var doc = PARSER.parse("[foo]: /url\n[^1]: footnote\n");
        var linkReferenceDef = find(doc, LinkReferenceDefinition.class);
        var footnotesDef = find(doc, FootnoteDefinition.class);
        assertEquals("foo", linkReferenceDef.getLabel());
        assertEquals("1", footnotesDef.getLabel());
    }

    @Test
    public void testDefContainsParagraph() {
        var doc = PARSER.parse("[^1]: footnote\n");
        var def = find(doc, FootnoteDefinition.class);
        var paragraph = (Paragraph) def.getFirstChild();
        var text = (Text) paragraph.getFirstChild();
        assertEquals("footnote", text.getLiteral());
    }

    @Test
    public void testDefContainsMultipleLines() {
        var doc = PARSER.parse("[^1]: footnote\nstill\n");
        var def = find(doc, FootnoteDefinition.class);
        assertEquals("1", def.getLabel());
        var paragraph = (Paragraph) def.getFirstChild();
        var text1 = (Text) paragraph.getFirstChild();
        var text2 = (Text) paragraph.getLastChild();
        assertEquals("footnote", text1.getLiteral());
        assertEquals("still", text2.getLiteral());
    }

    @Test
    public void testDefContainsList() {
        var doc = PARSER.parse("[^1]: - foo\n    - bar\n");
        var def = find(doc, FootnoteDefinition.class);
        assertEquals("1", def.getLabel());
        var list = (BulletList) def.getFirstChild();
        var item1 = (ListItem) list.getFirstChild();
        var item2 = (ListItem) list.getLastChild();
        var text1 = (Text) item1.getFirstChild().getFirstChild();
        var text2 = (Text) item2.getFirstChild().getFirstChild();
        assertEquals("foo", text1.getLiteral());
        assertEquals("bar", text2.getLiteral());
    }

    @Test
    public void testDefInterruptedByOthers() {
        var doc = PARSER.parse("[^1]: footnote\n# Heading\n");
        var def = find(doc, FootnoteDefinition.class);
        var heading = find(doc, Heading.class);
        assertEquals("1", def.getLabel());
        assertEquals("Heading", ((Text) heading.getFirstChild()).getLiteral());
    }

    @Test
    public void testReference() {
        var doc = PARSER.parse("Test [^foo]\n\n[^foo]: /url\n");
        var ref = find(doc, FootnoteReference.class);
        assertEquals("foo", ref.getLabel());
    }

    @Test
    public void testReferenceNoDefinition() {
        var doc = PARSER.parse("Test [^foo]\n");
        assertNull(tryFind(doc, FootnoteReference.class));
    }

    @Test
    public void testRefWithEmphasis() {
        var doc = PARSER.parse("Test [^*foo*]\n\n[^*foo*]: def\n");
        var ref = find(doc, FootnoteReference.class);
        assertEquals("*foo*", ref.getLabel());
        assertNull(ref.getFirstChild());
        var paragraph = doc.getFirstChild();
        var text = (Text) paragraph.getFirstChild();
        assertEquals("Test ", text.getLiteral());
        assertEquals(ref, text.getNext());
        assertEquals(ref, paragraph.getLastChild());
    }

    @Test
    public void testRefAfterBang() {
        var doc = PARSER.parse("Test![^foo]\n\n[^foo]: def\n");
        var ref = find(doc, FootnoteReference.class);
        assertEquals("foo", ref.getLabel());
        var paragraph = doc.getFirstChild();
        var text = (Text) paragraph.getFirstChild();
        assertEquals("Test!", text.getLiteral());
    }

    // Interesting test cases:

    // Test [foo][^bar]
    //
    // [^bar]: footnote
    //
    // -> [^bar] is a footnote but [foo] is just text, because full reference links don't resolve as footnotes

    // Test [^bar][]
    //
    // [^bar]: footnote
    //
    // -> [^bar] is a footnote but [] is just text, because collapsed reference links don't resolve as footnotes

    // Test [^f[oo]
    //
    // [^f[oo]: /url
    //
    // -> Not a footnote, [ needs to be escaped

    // Test [^*foo*]
    //
    // [^*foo*]: /url
    //
    // -> No emphasis inside footnote reference

    // Test *abc [^foo*] def*
    //
    // [^foo*]: /url
    //
    // -> Emphasis around footnote reference

    // Test [^*foo*][foo]
    //
    // [^*foo*]: /url
    //
    // [foo]: /url
    //
    // vs
    //
    // Test [^*foo*][foo]
    //
    // [^*foo*]: /url

    // Test ![^foo]
    //
    // [^foo]: note
    //
    // -> Not an image

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
}
