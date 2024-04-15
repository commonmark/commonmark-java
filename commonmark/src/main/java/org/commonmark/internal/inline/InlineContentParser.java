package org.commonmark.internal.inline;

public interface InlineContentParser {

    /**
     * An inline content parser needs to have a special "trigger" character which activates it. If this character is
     * encountered during inline parsing, {@link #tryParse} is called with the current parser state.
     */
    char getTriggerCharacter();

    /**
     * Try to parse the inline content. Note that the character at the current position is the
     * {@link #getTriggerCharacter()}.
     *
     * @param inlineParserState the current state of the inline parser
     * @return the result of parsing; can indicate that this parser is not interested, or that parsing was successful
     */
    ParsedInline tryParse(InlineParserState inlineParserState);
}
