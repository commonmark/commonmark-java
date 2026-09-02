package org.commonmark.internal.inline;

import java.util.Set;
import java.util.regex.Pattern;
import org.commonmark.node.AutoLink;
import org.commonmark.parser.SourceLines;
import org.commonmark.parser.beta.*;
import org.commonmark.text.AsciiMatcher;
import org.commonmark.text.CharMatcher;

/** Attempt to parse an autolink (URL or email in pointy brackets). */
public class AutolinkInlineParser implements InlineContentParser {

    private static final CharMatcher ANGLE_BRACKETS = AsciiMatcher.builder().c('<').c('>').build();

    private static final Pattern URI =
            Pattern.compile("^[a-zA-Z][a-zA-Z0-9.+-]{1,31}:[^<>\u0000-\u0020]*$");

    private static final Pattern EMAIL =
            Pattern.compile(
                    "^[a-zA-Z0-9.!#$%&'*+/=?^_`{|}~-]+@[a-zA-Z0-9](?:[a-zA-Z0-9-]{0,61}[a-zA-Z0-9])?(?:\\.[a-zA-Z0-9](?:[a-zA-Z0-9-]{0,61}[a-zA-Z0-9])?)*+$");

    @Override
    public ParsedInline tryParse(InlineParserState inlineParserState) {
        Scanner scanner = inlineParserState.scanner();
        Position openBracket = scanner.position();
        scanner.next();
        Position textStart = scanner.position();
        if (scanner.find(ANGLE_BRACKETS) > 0) {
            if (scanner.peek() != '>') {
                return ParsedInline.none();
            }

            String content = scanner.getSource(textStart, scanner.position()).getContent();
            scanner.next(); // consume '>'
            Position afterCloseBracket = scanner.position();

            boolean uri = URI.matcher(content).matches();
            boolean email = !uri && EMAIL.matcher(content).matches();

            if (uri || email) {
                AutoLink autoLink = new AutoLink(content, email);
                autoLink.setSourceSpans(scanner.getSource(openBracket, afterCloseBracket).getSourceSpans());
                return ParsedInline.of(autoLink, afterCloseBracket);
            }
        }
        return ParsedInline.none();
    }

    public static class Factory implements InlineContentParserFactory {
        @Override
        public Set<Character> getTriggerCharacters() {
            return Set.of('<');
        }

        @Override
        public InlineContentParser create() {
            return new AutolinkInlineParser();
        }
    }
}
