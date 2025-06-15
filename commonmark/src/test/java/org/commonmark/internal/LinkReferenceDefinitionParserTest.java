package org.commonmark.internal;

import org.commonmark.internal.LinkReferenceDefinitionParser.State;
import org.commonmark.node.LinkReferenceDefinition;
import org.commonmark.parser.SourceLine;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class LinkReferenceDefinitionParserTest {

    private final LinkReferenceDefinitionParser parser = new LinkReferenceDefinitionParser();

    @Test
    void testStartLabel() {
        assertState("[", State.LABEL, "[");
    }

    @Test
    void testStartNoLabel() {
        // Not a label
        assertParagraph("a");
        // Can not go back to parsing link reference definitions
        parse("a");
        parse("[");
        assertThat(parser.getState()).isEqualTo(State.PARAGRAPH);
        assertParagraphLines("a\n[", parser);
    }

    @Test
    void testEmptyLabel() {
        assertParagraph("[]: /");
        assertParagraph("[ ]: /");
        assertParagraph("[ \t\n\u000B\f\r ]: /");
    }

    @Test
    void testLabelColon() {
        assertParagraph("[foo] : /");
    }

    @Test
    void testLabel() {
        assertState("[foo]:", State.DESTINATION, "[foo]:");
        assertState("[ foo ]:", State.DESTINATION, "[ foo ]:");
    }

    @Test
    void testLabelInvalid() {
        assertParagraph("[foo[]:");
    }

    @Test
    void testLabelMultiline() {
        parse("[two");
        assertThat(parser.getState()).isEqualTo(State.LABEL);
        parse("lines]:");
        assertThat(parser.getState()).isEqualTo(State.DESTINATION);
        parse("/url");
        assertThat(parser.getState()).isEqualTo(State.START_TITLE);
        assertDef(parser.getDefinitions().get(0), "two\nlines", "/url", null);
    }

    @Test
    void testLabelStartsWithNewline() {
        parse("[");
        assertThat(parser.getState()).isEqualTo(State.LABEL);
        parse("weird]:");
        assertThat(parser.getState()).isEqualTo(State.DESTINATION);
        parse("/url");
        assertThat(parser.getState()).isEqualTo(State.START_TITLE);
        assertDef(parser.getDefinitions().get(0), "\nweird", "/url", null);
    }

    @Test
    void testDestination() {
        parse("[foo]: /url");
        assertThat(parser.getState()).isEqualTo(State.START_TITLE);
        assertParagraphLines("", parser);

        assertThat(parser.getDefinitions()).hasSize(1);
        assertDef(parser.getDefinitions().get(0), "foo", "/url", null);

        parse("[bar]: </url2>");
        assertDef(parser.getDefinitions().get(1), "bar", "/url2", null);
    }

    @Test
    void testDestinationInvalid() {
        assertParagraph("[foo]: <bar<>");
    }

    @Test
    void testTitle() {
        parse("[foo]: /url 'title'");
        assertThat(parser.getState()).isEqualTo(State.START_DEFINITION);
        assertParagraphLines("", parser);

        assertThat(parser.getDefinitions()).hasSize(1);
        assertDef(parser.getDefinitions().get(0), "foo", "/url", "title");
    }

    @Test
    void testTitleStartWhitespace() {
        parse("[foo]: /url");
        assertThat(parser.getState()).isEqualTo(State.START_TITLE);
        assertParagraphLines("", parser);

        parse("   ");

        assertThat(parser.getState()).isEqualTo(State.START_DEFINITION);
        assertParagraphLines("   ", parser);

        assertThat(parser.getDefinitions()).hasSize(1);
        assertDef(parser.getDefinitions().get(0), "foo", "/url", null);
    }

    @Test
    void testTitleMultiline() {
        parse("[foo]: /url 'two");
        assertThat(parser.getState()).isEqualTo(State.TITLE);
        assertParagraphLines("[foo]: /url 'two", parser);
        assertThat(parser.getDefinitions()).isEmpty();

        parse("lines");
        assertThat(parser.getState()).isEqualTo(State.TITLE);
        assertParagraphLines("[foo]: /url 'two\nlines", parser);
        assertThat(parser.getDefinitions()).isEmpty();

        parse("'");
        assertThat(parser.getState()).isEqualTo(State.START_DEFINITION);
        assertParagraphLines("", parser);

        assertThat(parser.getDefinitions()).hasSize(1);
        assertDef(parser.getDefinitions().get(0), "foo", "/url", "two\nlines\n");
    }

    @Test
    void testTitleMultiline2() {
        parse("[foo]: /url '");
        assertThat(parser.getState()).isEqualTo(State.TITLE);
        parse("title'");
        assertThat(parser.getState()).isEqualTo(State.START_DEFINITION);

        assertDef(parser.getDefinitions().get(0), "foo", "/url", "\ntitle");
    }

    @Test
    void testTitleMultiline3() {
        parse("[foo]: /url");
        assertThat(parser.getState()).isEqualTo(State.START_TITLE);
        // Note that this looks like a valid title until we parse "bad", at which point we need to treat the whole line
        // as a paragraph line and discard any already parsed title.
        parse("\"title\" bad");
        assertThat(parser.getState()).isEqualTo(State.PARAGRAPH);

        assertDef(parser.getDefinitions().get(0), "foo", "/url", null);
    }

    @Test
    void testTitleMultiline4() {
        parse("[foo]: /url");
        assertThat(parser.getState()).isEqualTo(State.START_TITLE);
        parse("(title");
        assertThat(parser.getState()).isEqualTo(State.TITLE);
        parse("foo(");
        assertThat(parser.getState()).isEqualTo(State.PARAGRAPH);

        assertDef(parser.getDefinitions().get(0), "foo", "/url", null);
    }

    @Test
    void testTitleInvalid() {
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
        assertThat(parser.getState()).isEqualTo(state);
        assertParagraphLines(paragraphContent, parser);
    }

    private static void assertDef(LinkReferenceDefinition def, String label, String destination, String title) {
        assertThat(def.getLabel()).isEqualTo(label);
        assertThat(def.getDestination()).isEqualTo(destination);
        assertThat(def.getTitle()).isEqualTo(title);
    }

    private static void assertParagraphLines(String expectedContent, LinkReferenceDefinitionParser parser) {
        String actual = parser.getParagraphLines().getContent();
        assertThat(actual).isEqualTo(expectedContent);
    }
}
