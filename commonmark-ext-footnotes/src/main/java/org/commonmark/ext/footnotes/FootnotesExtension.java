package org.commonmark.ext.footnotes;

import org.commonmark.Extension;
import org.commonmark.ext.footnotes.internal.*;
import org.commonmark.parser.Parser;
import org.commonmark.parser.beta.InlineContentParserFactory;
import org.commonmark.renderer.NodeRenderer;
import org.commonmark.renderer.html.HtmlRenderer;
import org.commonmark.renderer.markdown.MarkdownNodeRendererContext;
import org.commonmark.renderer.markdown.MarkdownNodeRendererFactory;
import org.commonmark.renderer.markdown.MarkdownRenderer;

import java.util.Set;

/**
 * Extension for footnotes with syntax like GitHub Flavored Markdown:
 * <pre><code>
 * Some text with a footnote[^1].
 *
 * [^1]: The text of the footnote.
 * </code></pre>
 * The <code>[^1]</code> is a {@link FootnoteReference}, with "1" being the label.
 * <p>
 * The line with <code>[^1]: ...</code> is a {@link FootnoteDefinition}, with the contents as child nodes (can be a
 * paragraph like in the example, or other blocks like lists).
 * <p>
 * All the footnotes (definitions) will be rendered in a list at the end of a document, no matter where they appear in
 * the source. The footnotes will be numbered starting from 1, then 2, etc, depending on the order in which they appear
 * in the text (and not dependent on the label). The footnote reference is a link to the footnote, and from the footnote
 * there is a link back to the reference (or multiple).
 * <p>
 * There is also optional support for inline footnotes, use {@link #builder()} and then set {@link Builder#inlineFootnotes}.
 *
 * @see <a href="https://docs.github.com/en/get-started/writing-on-github/getting-started-with-writing-and-formatting-on-github/basic-writing-and-formatting-syntax#footnotes">GitHub docs for footnotes</a>
 */
public class FootnotesExtension implements Parser.ParserExtension,
        HtmlRenderer.HtmlRendererExtension,
        MarkdownRenderer.MarkdownRendererExtension {

    private final boolean inlineFootnotes;

    private FootnotesExtension(boolean inlineFootnotes) {
        this.inlineFootnotes = inlineFootnotes;
    }

    /**
     * The extension with the default configuration (no support for inline footnotes).
     */
    public static Extension create() {
        return builder().build();
    }

    public static Builder builder() {
        return new Builder();
    }

    @Override
    public void extend(Parser.Builder parserBuilder) {
        parserBuilder
                .customBlockParserFactory(new FootnoteBlockParser.Factory())
                .linkProcessor(new FootnoteLinkProcessor());
        if (inlineFootnotes) {
            parserBuilder.linkMarker('^');
        }
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

    public static class Builder {

        private boolean inlineFootnotes = false;

        /**
         * Enable support for inline footnotes without definitions, e.g.:
         * <pre>
         * Some text^[this is an inline footnote]
         * </pre>
         */
        public Builder inlineFootnotes(boolean inlineFootnotes) {
            this.inlineFootnotes = inlineFootnotes;
            return this;
        }

        public FootnotesExtension build() {
            return new FootnotesExtension(inlineFootnotes);
        }
    }
}
