package org.commonmark.ext.metadata;

import org.commonmark.Extension;
import org.commonmark.ext.metadata.internal.MetadataBlockParser;
import org.commonmark.ext.metadata.internal.MetadataBlockRenderer;
import org.commonmark.html.HtmlRenderer;
import org.commonmark.parser.Parser;

/**
 * Extension for YAML-like metadata.
 * <p>
 * Create it with {@link #create()} and then configure it on the builders
 * ({@link org.commonmark.parser.Parser.Builder#extensions(Iterable)},
 * {@link org.commonmark.html.HtmlRenderer.Builder#extensions(Iterable)}).
 * </p>
 * <p>
 * The parsed metadata is turned into {@link MetadataNode}. You can access the metadata using {@link MetadataVisitor}.
 * </p>
 */
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
