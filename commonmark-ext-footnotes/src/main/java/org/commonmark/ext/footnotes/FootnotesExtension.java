package org.commonmark.ext.footnotes;

import org.commonmark.Extension;
import org.commonmark.ext.footnotes.internal.FootnoteBlockParser;
import org.commonmark.parser.Parser;

/**
 * TODO
 */
// TODO: HTML rendering and Markdown rendering
public class FootnotesExtension implements Parser.ParserExtension {

    private FootnotesExtension() {
    }

    public static Extension create() {
        return new FootnotesExtension();
    }

    @Override
    public void extend(Parser.Builder parserBuilder) {
        parserBuilder.customBlockParserFactory(new FootnoteBlockParser.Factory());
    }
}
