package org.commonmark.internal;

import org.commonmark.internal.LinkReferenceDefinitionParser.State;
import org.commonmark.node.LinkReferenceDefinition;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class LinkReferenceDefinitionParserTest {

    private LinkReferenceDefinitionParser parser = new LinkReferenceDefinitionParser();

    @Test
    public void testStartLabel() {
        parser.parse("[");
        assertEquals(State.LABEL, parser.getState());
        assertEquals("[", parser.getParagraphContent().toString());
    }

    @Test
    public void testStartNoLabel() {
        // Not a label
        assertParagraph("a");
        // Can not go back to parsing link reference definitions
        parser.parse("a");
        parser.parse("[");
        assertEquals(State.PARAGRAPH, parser.getState());
        assertEquals("a\n[", parser.getParagraphContent().toString());
    }

    @Test
    public void testEmptyLabel() {
        assertParagraph("[]: /");
        assertParagraph("[ ]: /");
        assertParagraph("[ \t\n\u000B\f\r ]: /");
    }

    @Test
    public void testLabelColon() {
        assertParagraph("[foo] : /");
    }

    @Test
    public void testLabel() {
        assertState("[foo]:", State.DESTINATION, "[foo]:");
        assertState("[ foo ]:", State.DESTINATION, "[ foo ]:");
    }

    @Test
    public void testLabelInvalid() {
        assertParagraph("[foo[]:");
    }

    @Test
    public void testLabelMultiline() {
        LinkReferenceDefinitionParser parser = new LinkReferenceDefinitionParser();
        parser.parse("[two");
        assertEquals(State.LABEL, parser.getState());
        parser.parse("lines]:");
        assertEquals(State.DESTINATION, parser.getState());
        parser.parse("/url");
        assertEquals(State.START_TITLE, parser.getState());
        assertDef(parser.getDefinitions().get(0), "two lines", "/url", null);
    }

    @Test
    public void testLabelStartsWithNewline() {
        LinkReferenceDefinitionParser parser = new LinkReferenceDefinitionParser();
        parser.parse("[");
        assertEquals(State.LABEL, parser.getState());
        parser.parse("weird]:");
        assertEquals(State.DESTINATION, parser.getState());
        parser.parse("/url");
        assertEquals(State.START_TITLE, parser.getState());
        assertDef(parser.getDefinitions().get(0), "weird", "/url", null);
    }

    @Test
    public void testDestination() {
        LinkReferenceDefinitionParser parser = new LinkReferenceDefinitionParser();
        parser.parse("[foo]: /url");
        assertEquals(State.START_TITLE, parser.getState());
        assertEquals("", parser.getParagraphContent().toString());

        assertEquals(1, parser.getDefinitions().size());
        assertDef(parser.getDefinitions().get(0), "foo", "/url", null);

        parser.parse("[bar]: </url2>");
        assertDef(parser.getDefinitions().get(1), "bar", "/url2", null);
    }

    @Test
    public void testDestinationInvalid() {
        assertParagraph("[foo]: <bar<>");
    }

    @Test
    public void testTitle() {
        LinkReferenceDefinitionParser parser = new LinkReferenceDefinitionParser();
        parser.parse("[foo]: /url 'title'");
        assertEquals(State.START_DEFINITION, parser.getState());
        assertEquals("", parser.getParagraphContent().toString());

        assertEquals(1, parser.getDefinitions().size());
        assertDef(parser.getDefinitions().get(0), "foo", "/url", "title");
    }

    @Test
    public void testTitleStartWhitespace() {
        LinkReferenceDefinitionParser parser = new LinkReferenceDefinitionParser();
        parser.parse("[foo]: /url");
        assertEquals(State.START_TITLE, parser.getState());
        assertEquals("", parser.getParagraphContent().toString());

        parser.parse("   ");

        assertEquals(State.START_DEFINITION, parser.getState());
        assertEquals("   ", parser.getParagraphContent().toString());

        assertEquals(1, parser.getDefinitions().size());
        assertDef(parser.getDefinitions().get(0), "foo", "/url", null);
    }

    @Test
    public void testTitleMultiline() {
        LinkReferenceDefinitionParser parser = new LinkReferenceDefinitionParser();
        parser.parse("[foo]: /url 'two");
        assertEquals(State.TITLE, parser.getState());
        assertEquals("[foo]: /url 'two", parser.getParagraphContent().toString());
        assertEquals(0, parser.getDefinitions().size());

        parser.parse("lines");
        assertEquals(State.TITLE, parser.getState());
        assertEquals("[foo]: /url 'two\nlines", parser.getParagraphContent().toString());
        assertEquals(0, parser.getDefinitions().size());

        parser.parse("'");
        assertEquals(State.START_DEFINITION, parser.getState());
        assertEquals("", parser.getParagraphContent().toString());

        assertEquals(1, parser.getDefinitions().size());
        assertDef(parser.getDefinitions().get(0), "foo", "/url", "two\nlines\n");
    }

    @Test
    public void testTitleMultiline2() {
        LinkReferenceDefinitionParser parser = new LinkReferenceDefinitionParser();
        parser.parse("[foo]: /url '");
        assertEquals(State.TITLE, parser.getState());
        parser.parse("title'");
        assertEquals(State.START_DEFINITION, parser.getState());

        assertDef(parser.getDefinitions().get(0), "foo", "/url", "\ntitle");
    }

    @Test
    public void testTitleInvalid() {
        assertParagraph("[foo]: /url (invalid(");
        assertParagraph("[foo]: </url>'title'");
        assertParagraph("[foo]: /url 'title' INVALID");
    }

    private static void assertParagraph(String input) {
        assertState(input, State.PARAGRAPH, input);
    }

    private static void assertState(String input, State state, String paragraphContent) {
        LinkReferenceDefinitionParser parser = new LinkReferenceDefinitionParser();
        parser.parse(input);
        assertEquals(state, parser.getState());
        assertEquals(paragraphContent, parser.getParagraphContent().toString());
    }

    private static void assertDef(LinkReferenceDefinition def, String label, String destination, String title) {
        assertEquals(label, def.getLabel());
        assertEquals(destination, def.getDestination());
        assertEquals(title, def.getTitle());
    }
}
