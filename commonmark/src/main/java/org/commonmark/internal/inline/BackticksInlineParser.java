package org.commonmark.internal.inline;

import org.commonmark.internal.util.Parsing;
import org.commonmark.node.Code;
import org.commonmark.node.Node;
import org.commonmark.node.Text;

/**
 * Attempt to parse backticks, returning either a backtick code span or a literal sequence of backticks.
 */
public class BackticksInlineParser implements InlineContentParser {

    @Override
    public ParsedInline tryParse(InlineParserState inlineParserState, Node previous) {
        Scanner scanner = inlineParserState.scanner();
        Position start = scanner.position();
        int openingTicks = scanner.matchMultiple('`');
        Position afterOpening = scanner.position();

        while (scanner.find('`') > 0) {
            Position beforeClosing = scanner.position();
            int count = scanner.matchMultiple('`');
            if (count == openingTicks) {
                Code node = new Code();

                String content = scanner.textBetween(afterOpening, beforeClosing);
                content = content.replace('\n', ' ');

                // spec: If the resulting string both begins and ends with a space character, but does not consist
                // entirely of space characters, a single space character is removed from the front and back.
                if (content.length() >= 3 &&
                        content.charAt(0) == ' ' &&
                        content.charAt(content.length() - 1) == ' ' &&
                        Parsing.hasNonSpace(content)) {
                    content = content.substring(1, content.length() - 1);
                }

                node.setLiteral(content);
                return ParsedInline.of(node, scanner.position());
            }
        }

        // If we got here, we didn't find a matching closing backtick sequence.
        String ticks = scanner.textBetween(start, afterOpening);
        return ParsedInline.of(new Text(ticks), afterOpening);
    }
}
