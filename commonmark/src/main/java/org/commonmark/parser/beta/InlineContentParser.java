package org.commonmark.parser.beta;

/**
 * Parser for a type of inline content. Registered via a {@link InlineContentParserFactory} and created by its
 * {@link InlineContentParserFactory#create() create} method. The lifetime of this is tied to each inline content
 * snippet that is parsed, as a new instance is created for each.
 */
public interface InlineContentParser {

    /**
     * Try to parse inline content starting from the current position. Note that the character at the current position
     * is one of {@link InlineContentParserFactory#getTriggerCharacters()} of the factory that created this parser.
     * <p>
     * For a given inline content snippet that is being parsed, this method can be called multiple times: each time a
     * trigger character is encountered.
     *
     * @param inlineParserState the current state of the inline parser
     * @return the result of parsing; can indicate that this parser is not interested, or that parsing was successful
     */
    ParsedInline tryParse(InlineParserState inlineParserState);
}
