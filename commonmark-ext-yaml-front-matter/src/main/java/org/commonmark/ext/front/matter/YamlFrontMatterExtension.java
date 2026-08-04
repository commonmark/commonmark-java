package org.commonmark.ext.front.matter;

import java.util.Objects;
import java.util.Set;
import org.commonmark.Extension;
import org.commonmark.ext.front.matter.parser.FrontMatterParser;
import org.commonmark.ext.front.matter.parser.RawContentParser;
import org.commonmark.ext.front.matter.parser.YamlSubsetParser;
import org.commonmark.ext.front.matter.internal.YamlFrontMatterBlockParser;
import org.commonmark.ext.front.matter.internal.YamlFrontMatterMarkdownNodeRenderer;
import org.commonmark.node.Node;
import org.commonmark.parser.Parser;
import org.commonmark.renderer.NodeRenderer;
import org.commonmark.renderer.html.HtmlRenderer;
import org.commonmark.renderer.markdown.MarkdownNodeRendererContext;
import org.commonmark.renderer.markdown.MarkdownNodeRendererFactory;
import org.commonmark.renderer.markdown.MarkdownRenderer;

/**
 * Extension for YAML-like metadata.
 *
 * <p>Create it with {@link #create()} and then configure it on the builders ({@link
 * org.commonmark.parser.Parser.Builder#extensions(Iterable)}, {@link
 * HtmlRenderer.Builder#extensions(Iterable)}).
 *
 * <p>By default, the extension parses the subset of YAML with a built-int {@link YamlSubsetParser}.
 * The parsed metadata is turned into {@link YamlFrontMatterNode}. You can access
 * the metadata using {@link YamlFrontMatterVisitor#readData(Node)}.
 *
 * <p>Alternatively, you can create the extension with {@link RawContentParser.Factory}.
 * It turns the YAML front matter into {@link YamlFrontMatterRawContent} node, which stores
 * the entire front matter as a string. You can access the content with
 * {@link YamlFrontMatterVisitor#readRawContent(Node)} to process it with other tools.
 *
 * <p>Implement {@link FrontMatterParser} interface and the corresponding factory to
 * parse the front matter with a custom parser.
 */
public class YamlFrontMatterExtension
        implements Parser.ParserExtension, MarkdownRenderer.MarkdownRendererExtension {

    private final FrontMatterParser.Factory frontMatterParserFactory;

    private YamlFrontMatterExtension(FrontMatterParser.Factory frontMatterParserFactory) {
        this.frontMatterParserFactory = Objects.requireNonNull(frontMatterParserFactory);
    }

    @Override
    public void extend(Parser.Builder parserBuilder) {
        parserBuilder.customBlockParserFactory(new YamlFrontMatterBlockParser.Factory(frontMatterParserFactory));
    }

    public static Extension create() {
        return create(new YamlSubsetParser.Factory());
    }

    public static Extension create(FrontMatterParser.Factory parser) {
        return new YamlFrontMatterExtension(parser);
    }

    @Override
    public void extend(MarkdownRenderer.Builder rendererBuilder) {
        rendererBuilder.nodeRendererFactory(
                new MarkdownNodeRendererFactory() {
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
