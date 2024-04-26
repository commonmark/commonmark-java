package org.commonmark.parser;

/**
 * Factory for custom inline parser.
 */
public interface InlineParserFactory {

    /**
     * Create an {@link InlineParser} to use for parsing inlines. This is called once per parsed document.
     */
    InlineParser create(InlineParserContext inlineParserContext);
}
