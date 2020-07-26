package org.commonmark.internal.inline;

import org.commonmark.node.Link;
import org.commonmark.node.Node;
import org.commonmark.node.Text;

import java.util.regex.Pattern;

/**
 * Attempt to parse an autolink (URL or email in pointy brackets).
 */
public class AutolinkInlineParser implements InlineContentParser {

    private static final Pattern URI = Pattern
            .compile("^[a-zA-Z][a-zA-Z0-9.+-]{1,31}:[^<>\u0000-\u0020]*$");

    private static final Pattern EMAIL = Pattern
            .compile("^([a-zA-Z0-9.!#$%&'*+/=?^_`{|}~-]+@[a-zA-Z0-9](?:[a-zA-Z0-9-]{0,61}[a-zA-Z0-9])?(?:\\.[a-zA-Z0-9](?:[a-zA-Z0-9-]{0,61}[a-zA-Z0-9])?)*)$");

    @Override
    public ParsedInline tryParse(InlineParserState inlineParserState, Node previous) {
        Scanner scanner = inlineParserState.scanner();
        scanner.skip();
        Position start = scanner.position();
        if (scanner.find('>')) {
            String text = scanner.textBetween(start, scanner.position());
            scanner.skip();
            if (URI.matcher(text).matches()) {
                Link node = new Link(text, null);
                node.appendChild(new Text(text));
                return ParsedInline.of(node, scanner.position());
            } else if (EMAIL.matcher(text).matches()) {
                Link node = new Link("mailto:" + text, null);
                node.appendChild(new Text(text));
                return ParsedInline.of(node, scanner.position());
            }
        }
        return ParsedInline.none();
    }
}
