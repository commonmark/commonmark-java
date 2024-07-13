package org.commonmark.ext.footnotes.internal;

import org.commonmark.parser.beta.InlineContentParser;
import org.commonmark.parser.beta.InlineContentParserFactory;
import org.commonmark.parser.beta.InlineParserState;
import org.commonmark.parser.beta.ParsedInline;

import java.util.Set;

/**
 * Parses any potential inline footnote markers (any `^` before `[`). Later we'll either use it in
 * {@link FootnoteLinkProcessor}, or remove any unused ones in {@link InlineFootnoteMarkerRemover}.
 */
public class InlineFootnoteMarkerParser implements InlineContentParser {
    @Override
    public ParsedInline tryParse(InlineParserState inlineParserState) {
        var scanner = inlineParserState.scanner();
        // Skip ^
        scanner.next();

        if (scanner.peek() == '[') {
            return ParsedInline.of(new InlineFootnoteMarker(), scanner.position());
        } else {
            return ParsedInline.none();
        }
    }

    public static class Factory implements InlineContentParserFactory {

        @Override
        public Set<Character> getTriggerCharacters() {
            return Set.of('^');
        }

        @Override
        public InlineContentParser create() {
            return new InlineFootnoteMarkerParser();
        }
    }
}
