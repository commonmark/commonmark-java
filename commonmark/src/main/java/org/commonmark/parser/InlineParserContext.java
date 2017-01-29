package org.commonmark.parser;

import org.commonmark.parser.delimiter.DelimiterProcessor;

import java.util.List;
import java.util.Map;

/**
 * Parameter context for custom inline parser.
 */
public interface InlineParserContext {
    List<DelimiterProcessor> getCustomDelimiterProcessors();
}
