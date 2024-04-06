package org.commonmark.internal;

import org.commonmark.internal.LinkReferenceDefinitionParser.State;
import org.commonmark.node.LinkReferenceDefinition;
import org.commonmark.parser.SourceLine;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

public class LinkReferenceDefinitionParserTest {

    private final LinkReferenceDefinitionParser parser = new LinkReferenceDefinitionParser();

    @Test
    public void testStartLabel() {
        assertState("[", State.LABEL, "[");
    }

    @Test
    public void testStartNoLabel() {
        // Not a label
        assertParagraph("a");
        // Can not go back to parsing link reference definitions
        parse("a");
        parse("[");
        assertEquals(State.PARAGRAPH, parser.getState());
        assertParagraphLines("a\n[", parser);
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
        parse("[two");
        assertEquals(State.LABEL, parser.getState());
        parse("lines]:");
        assertEquals(State.DESTINATION, parser.getState());
        parse("/url");
        assertEquals(State.START_TITLE, parser.getState());
        assertDef(parser.getDefinitions().get(0), "two\nlines", "/url", null);
    }

    @Test
    public void testLabelStartsWithNewline() {
        parse("[");
        assertEquals(State.LABEL, parser.getState());
        parse("weird]:");
        assertEquals(State.DESTINATION, parser.getState());
        parse("/url");
        assertEquals(State.START_TITLE, parser.getState());
        assertDef(parser.getDefinitions().get(0), "\nweird", "/url", null);
    }

    @Test
    public void testDestination() {
        parse("[foo]: /url");
        assertEquals(State.START_TITLE, parser.getState());
        assertParagraphLines("", parser);

        assertEquals(1, parser.getDefinitions().size());
        assertDef(parser.getDefinitions().get(0), "foo", "/url", null);

        parse("[bar]: </url2>");
        assertDef(parser.getDefinitions().get(1), "bar", "/url2", null);
    }

    @Test
    public void testDestinationInvalid() {
        assertParagraph("[foo]: <bar<>");
    }

    @Test
    public void testTitle() {
        parse("[foo]: /url 'title'");
        assertEquals(State.START_DEFINITION, parser.getState());
        assertParagraphLines("", parser);

        assertEquals(1, parser.getDefinitions().size());
        assertDef(parser.getDefinitions().get(0), "foo", "/url", "title");
    }

    @Test
    public void testTitleStartWhitespace() {
        parse("[foo]: /url");
        assertEquals(State.START_TITLE, parser.getState());
        assertParagraphLines("", parser);

        parse("   ");

        assertEquals(State.START_DEFINITION, parser.getState());
        assertParagraphLines("   ", parser);

        assertEquals(1, parser.getDefinitions().size());
        assertDef(parser.getDefinitions().get(0), "foo", "/url", null);
    }

    @Test
    public void testTitleMultiline() {
        parse("[foo]: /url 'two");
        assertEquals(State.TITLE, parser.getState());
        assertParagraphLines("[foo]: /url 'two", parser);
        assertEquals(0, parser.getDefinitions().size());

        parse("lines");
        assertEquals(State.TITLE, parser.getState());
        assertParagraphLines("[foo]: /url 'two\nlines", parser);
        assertEquals(0, parser.getDefinitions().size());

        parse("'");
        assertEquals(State.START_DEFINITION, parser.getState());
        assertParagraphLines("", parser);

        assertEquals(1, parser.getDefinitions().size());
        assertDef(parser.getDefinitions().get(0), "foo", "/url", "two\nlines\n");
    }

    @Test
    public void testTitleMultiline2() {
        parse("[foo]: /url '");
        assertEquals(State.TITLE, parser.getState());
        parse("title'");
        assertEquals(State.START_DEFINITION, parser.getState());

        assertDef(parser.getDefinitions().get(0), "foo", "/url", "\ntitle");
    }

    @Test
    public void testTitleMultiline3() {
        parse("[foo]: /url");
        assertEquals(State.START_TITLE, parser.getState());
        // Note that this looks like a valid title until we parse "bad", at which point we need to treat the whole line
        // as a paragraph line and discard any already parsed title.
        parse("\"title\" bad");
        assertEquals(State.PARAGRAPH, parser.getState());

        assertDef(parser.getDefinitions().get(0), "foo", "/url", null);
    }

    @Test
    public void testTitleMultiline4() {
        parse("[foo]: /url");
        assertEquals(State.START_TITLE, parser.getState());
        parse("(title");
        assertEquals(State.TITLE, parser.getState());
        parse("foo(");
        assertEquals(State.PARAGRAPH, parser.getState());

        assertDef(parser.getDefinitions().get(0), "foo", "/url", null);
    }

    @Test
    public void testTitleInvalid() {
        assertParagraph("[foo]: /url (invalid(");
        assertParagraph("[foo]: </url>'title'");
        assertParagraph("[foo]: /url 'title' INVALID");
    }

    private void parse(String content) {
        parser.parse(SourceLine.of(content, null));
    }

    private static void assertParagraph(String input) {
        assertState(input, State.PARAGRAPH, input);
    }

    private static void assertState(String input, State state, String paragraphContent) {
        LinkReferenceDefinitionParser parser = new LinkReferenceDefinitionParser();
        // TODO: Should we check things with source spans here?
        parser.parse(SourceLine.of(input, null));
        assertEquals(state, parser.getState());
        assertParagraphLines(paragraphContent, parser);
    }

    private static void assertDef(LinkReferenceDefinition def, String label, String destination, String title) {
        assertEquals(label, def.getLabel());
        assertEquals(destination, def.getDestination());
        assertEquals(title, def.getTitle());
    }

    private static void assertParagraphLines(String expectedContent, LinkReferenceDefinitionParser parser) {
        String actual = parser.getParagraphLines().getContent();
        assertEquals(expectedContent, actual);
    }
}
