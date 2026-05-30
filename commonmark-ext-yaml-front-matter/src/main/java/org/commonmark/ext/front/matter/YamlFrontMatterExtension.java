package org.commonmark.ext.front.matter;

import java.util.Set;
import org.commonmark.Extension;
import org.commonmark.ext.front.matter.internal.YamlFrontMatterBlockParser;
import org.commonmark.ext.front.matter.internal.YamlFrontMatterMarkdownNodeRenderer;
import org.commonmark.parser.Parser;
import org.commonmark.renderer.NodeRenderer;
import org.commonmark.renderer.html.HtmlRenderer;
import org.commonmark.renderer.markdown.MarkdownNodeRendererContext;
import org.commonmark.renderer.markdown.MarkdownNodeRendererFactory;
import org.commonmark.renderer.markdown.MarkdownRenderer;

/**
 * Extension for YAML-like metadata.
 * <p>
 * Create it with {@link #create()} and then configure it on the builders
 * ({@link org.commonmark.parser.Parser.Builder#extensions(Iterable)},
 * {@link HtmlRenderer.Builder#extensions(Iterable)}).
 * </p>
 * <p>
 * The parsed metadata is turned into {@link YamlFrontMatterNode}. You can access the metadata using {@link YamlFrontMatterVisitor}.
 * </p>
 */
public class YamlFrontMatterExtension implements Parser.ParserExtension, MarkdownRenderer.MarkdownRendererExtension {

    private YamlFrontMatterExtension() {
    }

    @Override
    public void extend(Parser.Builder parserBuilder) {
        parserBuilder.customBlockParserFactory(new YamlFrontMatterBlockParser.Factory());
    }

    public static Extension create() {
        return new YamlFrontMatterExtension();
    }

    @Override
    public void extend(MarkdownRenderer.Builder rendererBuilder) {
        rendererBuilder.nodeRendererFactory(new MarkdownNodeRendererFactory() {
            @Override
            public NodeRenderer create(MarkdownNodeRendererContext context) {
                return new YamlFrontMatterMarkdownNodeRenderer(context);
            }

            @Override
            public Set<Character> getSpecialCharacters() {
                return Set.of();
            }
        });
    }
}
