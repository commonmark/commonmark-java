package org.commonmark.ext.front.matter;

import java.util.Objects;
import java.util.Set;
import org.commonmark.Extension;
import org.commonmark.ext.front.matter.extractor.YamlContentExtractor;
import org.commonmark.ext.front.matter.extractor.YamlDataExtractor;
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
 * <p>By default, the extension parses the subset of YAML with a built-int parser.
 * The parsed metadata is turned into {@link YamlFrontMatterNode}. You can access
 * the metadata using {@link YamlFrontMatterVisitor#readData(Node)}.
 *
 * <p>Alternatively, you can create the extension with {@link YamlContentExtractor.Factory}.
 * It turns the YAML front matter into {@link YamlFrontMatterContent} node, which stores
 * the front matter content as a simple string. You can access the content with
 * {@link YamlFrontMatterVisitor#readContent(Node)} to process it with other tools.
 *
 * <p>To create a custom YAML front matter extractor, implement {@link YamlFrontMatterExtractor}
 * interface and the corresponding factory.
 */
public class YamlFrontMatterExtension
        implements Parser.ParserExtension, MarkdownRenderer.MarkdownRendererExtension {

    private final YamlFrontMatterExtractor.Factory yamlExtractorFactory;

    private YamlFrontMatterExtension(YamlFrontMatterExtractor.Factory yamlExtractorFactory) {
        this.yamlExtractorFactory = Objects.requireNonNull(yamlExtractorFactory);
    }

    @Override
    public void extend(Parser.Builder parserBuilder) {
        parserBuilder.customBlockParserFactory(new YamlFrontMatterBlockParser.Factory(yamlExtractorFactory));
    }

    public static Extension create() {
        return create(new YamlDataExtractor.Factory());
    }

    public static Extension create(YamlFrontMatterExtractor.Factory extractor) {
        return new YamlFrontMatterExtension(extractor);
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
