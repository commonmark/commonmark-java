package org.commonmark.ext.gfm.tables;

import org.commonmark.parser.Parser;
import org.commonmark.html.HtmlRenderer;

public class TablesExtension implements Parser.ParserExtension, HtmlRenderer.HtmlRendererExtension {

    private TablesExtension() {
    }

    public static TablesExtension create() {
        return new TablesExtension();
    }

    @Override
    public void extend(Parser.Builder parserBuilder) {
        parserBuilder.customBlockParserFactory(new TableBlockParser.Factory());
    }

    @Override
    public void extend(HtmlRenderer.Builder rendererBuilder) {
        rendererBuilder.customHtmlRenderer(new TableHtmlRenderer());
    }

}
