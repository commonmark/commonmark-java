package org.commonmark.internal.inline;

import org.commonmark.internal.util.Parsing;
import org.commonmark.node.HardLineBreak;
import org.commonmark.node.Node;
import org.commonmark.node.SoftLineBreak;
import org.commonmark.node.Text;

/**
 * Parse a newline. If it was preceded by two spaces, return a hard line break; otherwise a soft line break.
 */
public class LineBreakInlineContentParser implements InlineContentParser {

    @Override
    public ParsedInline tryParse(InlineParserState inlineParserState, Node previous) {
        Scanner scanner = inlineParserState.scanner();
        scanner.skip();

        // Check previous text for trailing spaces.
        // The "endsWith" is an optimization to avoid an RE match in the common case.
        if (previous instanceof Text && ((Text) previous).getLiteral().endsWith(" ")) {
            Text text = (Text) previous;
            String literal = text.getLiteral();
            int last = literal.length() - 1;
            int nonSpace = Parsing.skipBackwards(' ', literal, last, 0);
            int spaces = last - nonSpace;
            if (spaces > 0) {
                text.setLiteral(literal.substring(0, literal.length() - spaces));
            }
            if (spaces >= 2) {
                return ParsedInline.of(new HardLineBreak(), scanner.position());
            } else {
                return ParsedInline.of(new SoftLineBreak(), scanner.position());
            }
        } else {
            return ParsedInline.of(new SoftLineBreak(), scanner.position());
        }
    }
}
