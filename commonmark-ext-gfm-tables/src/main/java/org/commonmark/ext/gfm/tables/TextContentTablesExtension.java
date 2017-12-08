package org.commonmark.ext.gfm.tables;

import org.commonmark.Extension;
import org.commonmark.ext.gfm.tables.internal.TableBlockParser;
import org.commonmark.ext.gfm.tables.internal.TextContentTableNodeRenderer;
import org.commonmark.parser.Parser;
import org.commonmark.renderer.NodeRenderer;
import org.commonmark.renderer.text.TextContentNodeRendererContext;
import org.commonmark.renderer.text.TextContentNodeRendererFactory;
import org.commonmark.renderer.text.TextContentRenderer;

/**
 * Extension for GFM tables using "|" pipes (GitHub Flavored Markdown).
 * <p>
 * Create it with {@link #create()} and then configure it on the builders
 * ({@link org.commonmark.parser.Parser.Builder#extensions(Iterable)},
 * {@link TextContentRenderer.Builder#extensions(Iterable)}).
 * </p>
 * <p>
 * The parsed tables are turned into {@link TableBlock} blocks.
 * </p>
 */
public class TextContentTablesExtension implements Parser.ParserExtension, TextContentRenderer.TextContentRendererExtension {

    private TextContentTablesExtension() {
    }

    public static Extension create() {
        return new TextContentTablesExtension();
    }

    @Override
    public void extend(Parser.Builder parserBuilder) {
        parserBuilder.customBlockParserFactory(new TableBlockParser.Factory());
    }

    @Override
    public void extend(TextContentRenderer.Builder rendererBuilder) {
        rendererBuilder.nodeRendererFactory(new TextContentNodeRendererFactory() {
            @Override
            public NodeRenderer create(TextContentNodeRendererContext context) {
                return new TextContentTableNodeRenderer(context);
            }
        });
    }
}
