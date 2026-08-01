package org.commonmark.internal.inline;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import org.commonmark.node.Code;
import org.commonmark.node.Text;
import org.commonmark.parser.beta.*;
import org.commonmark.text.Characters;

/**
 * Attempt to parse backticks, returning either a backtick code span or a literal sequence of
 * backticks.
 */
public class BackticksInlineParser implements InlineContentParser {

    // Keep track of positions of backtick runs to avoid repeated scanning with pathological inputs
    private final Map<Integer, Position> backtickPositions = new HashMap<>();
    private boolean scannedToEnd = false;

    @Override
    public ParsedInline tryParse(InlineParserState inlineParserState) {
        var scanner = inlineParserState.scanner();
        var start = scanner.position();
        var openingLength = scanner.matchMultiple('`');
        var afterOpening = scanner.position();

        if (scannedToEnd) {
            var potentialCloser = backtickPositions.get(openingLength);
            if (potentialCloser == null || potentialCloser.compareTo(start) <= 0) {
                // We have already scanned the whole input before and know there's no matching
                // closer, so no need to scan again; this is plain text.
                var text = new Text(scanner.getSource(start, afterOpening).getContent());
                return ParsedInline.of(text, afterOpening);
            }
        }

        while (scanner.find('`') > 0) {
            var beforeClosing = scanner.position();
            var length = scanner.matchMultiple('`');
            // Keep track of last position of backticks run of this length
            backtickPositions.put(length, beforeClosing);
            if (length == openingLength) {
                Code node = new Code();

                String content = scanner.getSource(afterOpening, beforeClosing).getContent();
                content = content.replace('\n', ' ');

                // spec: If the resulting string both begins and ends with a space character, but
                // does not consist entirely of space characters, a single space character is
                // removed from the front and back.
                if (content.length() >= 3
                        && content.charAt(0) == ' '
                        && content.charAt(content.length() - 1) == ' '
                        && Characters.hasNonSpace(content)) {
                    content = content.substring(1, content.length() - 1);
                }

                node.setLiteral(content);
                return ParsedInline.of(node, scanner.position());
            }
        }

        // Once we've scanned through the whole input, we have positions of any potential closing
        // backticks (of any length).
        scannedToEnd = true;

        // If we got here, we didn't find a matching closing backtick sequence.
        var text = new Text(scanner.getSource(start, afterOpening).getContent());
        return ParsedInline.of(text, afterOpening);
    }

    public static class Factory implements InlineContentParserFactory {
        @Override
        public Set<Character> getTriggerCharacters() {
            return Set.of('`');
        }

        @Override
        public InlineContentParser create() {
            return new BackticksInlineParser();
        }
    }
}
