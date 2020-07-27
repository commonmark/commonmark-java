package org.commonmark.internal.inline;

import org.commonmark.internal.util.AsciiMatcher;
import org.commonmark.internal.util.Html5Entities;
import org.commonmark.node.Node;
import org.commonmark.node.Text;

/**
 * Attempts to parse a HTML entity or numeric character reference.
 */
public class EntityInlineParser implements InlineContentParser {

    private static final AsciiMatcher hex = AsciiMatcher.builder().range('0', '9').range('A', 'F').range('a', 'f').build();
    private static final AsciiMatcher dec = AsciiMatcher.builder().range('0', '9').build();
    private static final AsciiMatcher entityStart = AsciiMatcher.builder().range('A', 'Z').range('a', 'z').build();
    private static final AsciiMatcher entityContinue = entityStart.newBuilder().range('0', '9').build();

    @Override
    public ParsedInline tryParse(InlineParserState inlineParserState, Node previous) {
        Scanner scanner = inlineParserState.scanner();
        Position start = scanner.position();
        // Skip `&`
        scanner.skip();

        char c = scanner.peek();
        if (c == '#') {
            // Numeric
            scanner.skip();
            if (scanner.skipOne('x') || scanner.skipOne('X')) {
                int digits = scanner.skip(hex);
                if (1 <= digits && digits <= 6 && scanner.skipOne(';')) {
                    return entity(scanner, start);
                }
            } else {
                int digits = scanner.skip(dec);
                if (1 <= digits && digits <= 7 && scanner.skipOne(';')) {
                    return entity(scanner, start);
                }
            }
        } else if (entityStart.matches(c)) {
            scanner.skip(entityContinue);
            if (scanner.skipOne(';')) {
                return entity(scanner, start);
            }
        }

        return ParsedInline.none();
    }

    private ParsedInline entity(Scanner scanner, Position start) {
        return ParsedInline.of(new Text(Html5Entities.entityToString(scanner.textBetween(start, scanner.position()))), scanner.position());
    }
}
