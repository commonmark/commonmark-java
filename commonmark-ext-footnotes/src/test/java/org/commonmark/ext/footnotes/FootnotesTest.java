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
    public void testBlockStart() {
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
    public void testBlockStartInterrupts() {
        var doc = PARSER.parse("test\n[^1]: footnote\n");
        var paragraph = find(doc, Paragraph.class);
        var def = find(doc, FootnoteDefinition.class);
        assertEquals("test", ((Text) paragraph.getLastChild()).getLiteral());
        assertEquals("1", def.getLabel());
    }

    @Test
    public void testMultiple() {
        var doc = PARSER.parse("[^1]: foo\n[^2]: bar\n");
        var defs = findAll(doc, FootnoteDefinition.class);
        assertEquals("1", defs.get(0).getLabel());
        assertEquals("2", defs.get(1).getLabel());
    }

    @Test
    public void testBlockStartAfterLinkReferenceDefinition() {
        var doc = PARSER.parse("[foo]: /url\n[^1]: footnote\n");
        var linkReferenceDef = find(doc, LinkReferenceDefinition.class);
        var footnotesDef = find(doc, FootnoteDefinition.class);
        assertEquals("foo", linkReferenceDef.getLabel());
        assertEquals("1", footnotesDef.getLabel());
    }

    @Test
    public void testBlockContinue() {
        var doc = PARSER.parse("[^1]: footnote\nstill\n");
        var def = find(doc, FootnoteDefinition.class);
        assertEquals("1", def.getLabel());
        assertNull(tryFind(doc, Paragraph.class));
    }

    @Test
    public void testFootnotesDefinitionInterruptedByOthers() {
        var doc = PARSER.parse("[^1]: footnote\n# Heading\n");
        var def = find(doc, FootnoteDefinition.class);
        var heading = find(doc, Heading.class);
        assertEquals("1", def.getLabel());
        assertEquals("Heading", ((Text) heading.getFirstChild()).getLiteral());
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
}
