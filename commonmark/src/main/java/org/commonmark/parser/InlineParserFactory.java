package org.commonmark.parser;

import org.commonmark.parser.delimiter.DelimiterProcessor;

import java.util.List;

/**
 * Factory for custom inline parser.
 */
public interface InlineParserFactory {
    InlineParser create(List<DelimiterProcessor> customDelimiterProcessors);
}
