package org.commonmark.internal.inline;

// TODO: I'd prefer if this was named InlineParser, but that's already public API, hmm...
public interface InlineContentParser {

    ParsedInline tryParse(InlineParserState inlineParserState);

}
