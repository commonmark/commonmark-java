package org.commonmark.ext.autolink;

import java.util.EnumSet;
import java.util.Set;
import org.commonmark.Extension;
import org.commonmark.ext.autolink.internal.AutolinkPostProcessor;
import org.commonmark.ext.autolink.internal.BareAutoLinkHtmlNodeRenderer;
import org.commonmark.ext.autolink.internal.BareAutoLinkMarkdownNodeRenderer;
import org.commonmark.ext.autolink.internal.BareAutoLinkTextContentNodeRenderer;
import org.commonmark.parser.Parser;
import org.commonmark.renderer.html.HtmlRenderer;
import org.commonmark.renderer.NodeRenderer;
import org.commonmark.renderer.markdown.MarkdownNodeRendererContext;
import org.commonmark.renderer.markdown.MarkdownNodeRendererFactory;
import org.commonmark.renderer.markdown.MarkdownRenderer;
import org.commonmark.renderer.text.TextContentRenderer;

/**
 * Extension for automatically turning plain URLs and email addresses into links.
 *
 * <p>Create it with {@link #create()} and then configure it on the builders ({@link
 * org.commonmark.parser.Parser.Builder#extensions(Iterable)}, {@link
 * HtmlRenderer.Builder#extensions(Iterable)}, {@link
 * MarkdownRenderer.Builder#extensions(Iterable)}).
 *
 * <p>Detected bare links are represented as {@link BareAutoLink} nodes in the AST.
 */
public class AutolinkExtension
        implements Parser.ParserExtension,
                HtmlRenderer.HtmlRendererExtension,
                TextContentRenderer.TextContentRendererExtension,
                MarkdownRenderer.MarkdownRendererExtension {

    private final Set<AutolinkType> linkTypes;

    private AutolinkExtension(Builder builder) {
        this.linkTypes = builder.linkTypes;
    }

    /**
     * @return the extension with default options
     */
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
        parserBuilder.postProcessor(new AutolinkPostProcessor(linkTypes));
    }

    @Override
    public void extend(HtmlRenderer.Builder rendererBuilder) {
        rendererBuilder.nodeRendererFactory(BareAutoLinkHtmlNodeRenderer::new);
    }

    @Override
    public void extend(TextContentRenderer.Builder rendererBuilder) {
        rendererBuilder.nodeRendererFactory(BareAutoLinkTextContentNodeRenderer::new);
    }

    @Override
    public void extend(MarkdownRenderer.Builder rendererBuilder) {
        rendererBuilder.nodeRendererFactory(
                new MarkdownNodeRendererFactory() {
                    @Override
                    public NodeRenderer create(MarkdownNodeRendererContext context) {
                        return new BareAutoLinkMarkdownNodeRenderer(context);
                    }

                    @Override
                    public Set<Character> getSpecialCharacters() {
                        return Set.of();
                    }
                });
    }

    public static class Builder {

        private Set<AutolinkType> linkTypes = EnumSet.allOf(AutolinkType.class);

        /**
         * @param linkTypes the link types that should be converted. By default, all {@link
         *     AutolinkType}s are converted.
         * @return {@code this}
         */
        public Builder linkTypes(AutolinkType... linkTypes) {
            if (linkTypes == null) {
                throw new NullPointerException("linkTypes must not be null");
            }

            return this.linkTypes(Set.of(linkTypes));
        }

        /**
         * @param linkTypes the link types that should be converted. By default, all {@link
         *     AutolinkType}s are converted.
         * @return {@code this}
         */
        public Builder linkTypes(Set<AutolinkType> linkTypes) {
            if (linkTypes == null) {
                throw new NullPointerException("linkTypes must not be null");
            }

            if (linkTypes.isEmpty()) {
                throw new IllegalArgumentException("linkTypes must not be empty");
            }

            this.linkTypes = EnumSet.copyOf(linkTypes);
            return this;
        }

        /**
         * @return a configured extension
         */
        public Extension build() {
            return new AutolinkExtension(this);
        }
    }
}
