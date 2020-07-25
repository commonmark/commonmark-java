package org.commonmark.internal.inline;

import org.commonmark.node.Node;

// TODO: I'd prefer if this was named InlineParser, but that's already public API, hmm...
public interface InlineContentParser {

    ParsedInline tryParse(InlineParserState inlineParserState, Node previous);

}
