package org.commonmark.ext.underline;

import org.commonmark.Extension;
import org.commonmark.ext.underline.internal.UnderlineDelimiterProcessor;
import org.commonmark.ext.underline.internal.UnderlineNodeRenderer;
import org.commonmark.html.renderer.NodeRenderer;
import org.commonmark.html.renderer.NodeRendererContext;
import org.commonmark.html.renderer.NodeRendererFactory;
import org.commonmark.parser.Parser;
import org.commonmark.html.HtmlRenderer;

/**
 * Extension for underline using ++
 * <p>
 * Create it with {@link #create()} and then configure it on the builders
 * ({@link org.commonmark.parser.Parser.Builder#extensions(Iterable)},
 * {@link org.commonmark.html.HtmlRenderer.Builder#extensions(Iterable)}).
 * </p>
 * <p>
 * The parsed underline text regions are turned into {@link Underline} nodes.
 * </p>
 */
public class UnderlineExtension implements Parser.ParserExtension, HtmlRenderer.HtmlRendererExtension {

    private UnderlineExtension() {
    }

    public static Extension create() {
        return new UnderlineExtension();
    }

    @Override
    public void extend(Parser.Builder parserBuilder) {
        parserBuilder.customDelimiterProcessor(new UnderlineDelimiterProcessor());
    }

    @Override
    public void extend(HtmlRenderer.Builder rendererBuilder) {
        rendererBuilder.nodeRendererFactory(new NodeRendererFactory() {
            @Override
            public NodeRenderer create(NodeRendererContext context) {
                return new UnderlineNodeRenderer(context);
            }
        });
    }
}
