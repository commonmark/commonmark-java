package org.commonmark.ext.gfm.tables;

import java.util.Set;
import org.commonmark.Extension;
import org.commonmark.ext.gfm.tables.internal.TableBlockParser;
import org.commonmark.ext.gfm.tables.internal.TableHtmlNodeRenderer;
import org.commonmark.ext.gfm.tables.internal.TableMarkdownNodeRenderer;
import org.commonmark.ext.gfm.tables.internal.TableTextContentNodeRenderer;
import org.commonmark.parser.Parser;
import org.commonmark.renderer.NodeRenderer;
import org.commonmark.renderer.html.HtmlRenderer;
import org.commonmark.renderer.markdown.MarkdownNodeRendererContext;
import org.commonmark.renderer.markdown.MarkdownNodeRendererFactory;
import org.commonmark.renderer.markdown.MarkdownRenderer;
import org.commonmark.renderer.text.TextContentRenderer;

/**
 * Extension for GFM tables using "|" pipes (GitHub Flavored Markdown).
 *
 * <p>Create it with {@link #create()} and then configure it on the builders ({@link
 * org.commonmark.parser.Parser.Builder#extensions(Iterable)}, {@link
 * HtmlRenderer.Builder#extensions(Iterable)}).
 *
 * <p>The parsed tables are turned into {@link TableBlock} blocks.
 *
 * @see <a href="https://github.github.com/gfm/#tables-extension-">Tables (extension) in GitHub
 *     Flavored Markdown Spec</a>
 */
public class TablesExtension
        implements Parser.ParserExtension,
                HtmlRenderer.HtmlRendererExtension,
                TextContentRenderer.TextContentRendererExtension,
                MarkdownRenderer.MarkdownRendererExtension {

    /** The default limit for the maximum number of cells, see {@link Builder#maxCells(Integer)}. */
    public static final int DEFAULT_MAX_CELLS = 1_000_000;

    private final Integer maxCells;

    private TablesExtension(Builder builder) {
        this.maxCells = builder.maxCells;
    }

    public static Extension create() {
        return builder().build();
    }

    /**
     * @return a builder to configure the behavior of the extension.
     */
    public static Builder builder() {
        return new Builder();
    }

    @Override
    public void extend(Parser.Builder parserBuilder) {
        parserBuilder.customBlockParserFactory(new TableBlockParser.Factory(maxCells));
    }

    @Override
    public void extend(HtmlRenderer.Builder rendererBuilder) {
        rendererBuilder.nodeRendererFactory(TableHtmlNodeRenderer::new);
    }

    @Override
    public void extend(TextContentRenderer.Builder rendererBuilder) {
        rendererBuilder.nodeRendererFactory(TableTextContentNodeRenderer::new);
    }

    @Override
    public void extend(MarkdownRenderer.Builder rendererBuilder) {
        rendererBuilder.nodeRendererFactory(
                new MarkdownNodeRendererFactory() {
                    @Override
                    public NodeRenderer create(MarkdownNodeRendererContext context) {
                        return new TableMarkdownNodeRenderer(context);
                    }

                    @Override
                    public Set<Character> getSpecialCharacters() {
                        return Set.of('|');
                    }
                });
    }

    public static class Builder {

        private Integer maxCells = DEFAULT_MAX_CELLS;

        /**
         * Set the maximum number of cells that are parsed for a single table; if more cells are
         * present in the source, parsing will throw an exception. This is to guard against
         * malicious input.
         *
         * <p>The default is {@link #DEFAULT_MAX_CELLS}; use {@code null} for no limit.
         */
        public Builder maxCells(Integer maxCells) {
            this.maxCells = maxCells;
            return this;
        }

        /**
         * @return a configured extension
         */
        public Extension build() {
            return new TablesExtension(this);
        }
    }
}
