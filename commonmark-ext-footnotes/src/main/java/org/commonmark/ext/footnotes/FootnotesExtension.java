package org.commonmark.ext.footnotes;

import org.commonmark.Extension;
import org.commonmark.ext.footnotes.internal.FootnoteBlockParser;
import org.commonmark.ext.footnotes.internal.FootnoteLinkProcessor;
import org.commonmark.ext.footnotes.internal.FootnoteHtmlNodeRenderer;
import org.commonmark.ext.footnotes.internal.FootnoteMarkdownNodeRenderer;
import org.commonmark.parser.Parser;
import org.commonmark.renderer.NodeRenderer;
import org.commonmark.renderer.html.HtmlRenderer;
import org.commonmark.renderer.markdown.MarkdownNodeRendererContext;
import org.commonmark.renderer.markdown.MarkdownNodeRendererFactory;
import org.commonmark.renderer.markdown.MarkdownRenderer;

import java.util.Set;

/**
 * TODO
 */
public class FootnotesExtension implements Parser.ParserExtension,
        HtmlRenderer.HtmlRendererExtension,
        MarkdownRenderer.MarkdownRendererExtension {

    private FootnotesExtension() {
    }

    public static Extension create() {
        return new FootnotesExtension();
    }

    @Override
    public void extend(Parser.Builder parserBuilder) {
        parserBuilder
                .customBlockParserFactory(new FootnoteBlockParser.Factory())
                .linkProcessor(new FootnoteLinkProcessor());
    }

    @Override
    public void extend(HtmlRenderer.Builder rendererBuilder) {
        rendererBuilder.nodeRendererFactory(FootnoteHtmlNodeRenderer::new);
    }

    @Override
    public void extend(MarkdownRenderer.Builder rendererBuilder) {
        rendererBuilder.nodeRendererFactory(new MarkdownNodeRendererFactory() {
            @Override
            public NodeRenderer create(MarkdownNodeRendererContext context) {
                return new FootnoteMarkdownNodeRenderer(context);
            }

            @Override
            public Set<Character> getSpecialCharacters() {
                return Set.of();
            }
        });
    }
}
