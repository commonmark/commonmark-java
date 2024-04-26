package org.commonmark.ext.ins;

import org.commonmark.Extension;
import org.commonmark.ext.ins.internal.InsDelimiterProcessor;
import org.commonmark.ext.ins.internal.InsHtmlNodeRenderer;
import org.commonmark.ext.ins.internal.InsMarkdownNodeRenderer;
import org.commonmark.ext.ins.internal.InsTextContentNodeRenderer;
import org.commonmark.parser.Parser;
import org.commonmark.renderer.NodeRenderer;
import org.commonmark.renderer.html.HtmlNodeRendererContext;
import org.commonmark.renderer.html.HtmlNodeRendererFactory;
import org.commonmark.renderer.html.HtmlRenderer;
import org.commonmark.renderer.markdown.MarkdownNodeRendererContext;
import org.commonmark.renderer.markdown.MarkdownNodeRendererFactory;
import org.commonmark.renderer.markdown.MarkdownRenderer;
import org.commonmark.renderer.text.TextContentNodeRendererContext;
import org.commonmark.renderer.text.TextContentNodeRendererFactory;
import org.commonmark.renderer.text.TextContentRenderer;

import java.util.Set;

/**
 * Extension for ins using ++
 * <p>
 * Create it with {@link #create()} and then configure it on the builders
 * ({@link org.commonmark.parser.Parser.Builder#extensions(Iterable)},
 * {@link HtmlRenderer.Builder#extensions(Iterable)}).
 * </p>
 * <p>
 * The parsed ins text regions are turned into {@link Ins} nodes.
 * </p>
 */
public class InsExtension implements Parser.ParserExtension, HtmlRenderer.HtmlRendererExtension, TextContentRenderer.TextContentRendererExtension, MarkdownRenderer.MarkdownRendererExtension {

    private InsExtension() {
    }

    public static Extension create() {
        return new InsExtension();
    }

    @Override
    public void extend(Parser.Builder parserBuilder) {
        parserBuilder.customDelimiterProcessor(new InsDelimiterProcessor());
    }

    @Override
    public void extend(HtmlRenderer.Builder rendererBuilder) {
        rendererBuilder.nodeRendererFactory(new HtmlNodeRendererFactory() {
            @Override
            public NodeRenderer create(HtmlNodeRendererContext context) {
                return new InsHtmlNodeRenderer(context);
            }
        });
    }

    @Override
    public void extend(TextContentRenderer.Builder rendererBuilder) {
        rendererBuilder.nodeRendererFactory(new TextContentNodeRendererFactory() {
            @Override
            public NodeRenderer create(TextContentNodeRendererContext context) {
                return new InsTextContentNodeRenderer(context);
            }
        });
    }

    @Override
    public void extend(MarkdownRenderer.Builder rendererBuilder) {
        rendererBuilder.nodeRendererFactory(new MarkdownNodeRendererFactory() {
            @Override
            public NodeRenderer create(MarkdownNodeRendererContext context) {
                return new InsMarkdownNodeRenderer(context);
            }

            @Override
            public Set<Character> getSpecialCharacters() {
                // We technically don't need to escape single occurrences of +, but that's all the extension API
                // exposes currently.
                return Set.of('+');
            }
        });
    }
}
