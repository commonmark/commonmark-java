package org.commonmark.ext.metadata;

import org.commonmark.Extension;
import org.commonmark.ext.metadata.internal.MetadataBlockParser;
import org.commonmark.ext.metadata.internal.MetadataBlockRenderer;
import org.commonmark.html.HtmlRenderer;
import org.commonmark.parser.Parser;

public class MetadataExtension implements Parser.ParserExtension, HtmlRenderer.HtmlRendererExtension {
    private MetadataExtension() {
    }

    @Override
    public void extend(HtmlRenderer.Builder rendererBuilder) {
        rendererBuilder.customHtmlRenderer(new MetadataBlockRenderer());
    }

    @Override
    public void extend(Parser.Builder parserBuilder) {
        parserBuilder.customBlockParserFactory(new MetadataBlockParser.Factory());
    }

    public static Extension create() {
        return new MetadataExtension();
    }
}
