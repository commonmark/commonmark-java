package org.commonmark.ext.yaml;

import org.commonmark.Extension;
import org.commonmark.ext.yaml.internal.YAMLFrontMatterBlockParser;
import org.commonmark.ext.yaml.internal.YAMLFrontMatterBlockRenderer;
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
 * The parsed metadata is turned into {@link YAMLFrontMatterNode}. You can access the metadata using {@link YAMLFrontMatterVisitor}.
 * </p>
 */
public class YAMLFrontMatterExtension implements Parser.ParserExtension, HtmlRenderer.HtmlRendererExtension {
    private YAMLFrontMatterExtension() {
    }

    @Override
    public void extend(HtmlRenderer.Builder rendererBuilder) {
        rendererBuilder.customHtmlRenderer(new YAMLFrontMatterBlockRenderer());
    }

    @Override
    public void extend(Parser.Builder parserBuilder) {
        parserBuilder.customBlockParserFactory(new YAMLFrontMatterBlockParser.Factory());
    }

    public static Extension create() {
        return new YAMLFrontMatterExtension();
    }
}
