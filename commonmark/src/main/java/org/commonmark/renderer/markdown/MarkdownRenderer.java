package org.commonmark.renderer.markdown;

import org.commonmark.Extension;
import org.commonmark.internal.renderer.NodeRendererMap;
import org.commonmark.node.Node;
import org.commonmark.renderer.NodeRenderer;
import org.commonmark.renderer.Renderer;

import java.util.ArrayList;
import java.util.List;

/**
 * Renders nodes to CommonMark Markdown.
 * <p>
 * Note that it does not currently attempt to preserve the exact syntax of the original input Markdown (if any):
 * <ul>
 *     <li>Headings are output as ATX headings if possible (multi-line headings need Setext headings)</li>
 *     <li>Escaping might be over-eager, e.g. a plain {@code *} might be escaped
 *     even though it doesn't need to be in that particular context</li>
 * </ul>
 */
public class MarkdownRenderer implements Renderer {

    private final List<MarkdownNodeRendererFactory> nodeRendererFactories;

    private MarkdownRenderer(Builder builder) {
        this.nodeRendererFactories = new ArrayList<>(builder.nodeRendererFactories.size() + 1);
        this.nodeRendererFactories.addAll(builder.nodeRendererFactories);
        // Add as last. This means clients can override the rendering of core nodes if they want.
        this.nodeRendererFactories.add(new MarkdownNodeRendererFactory() {
            @Override
            public NodeRenderer create(MarkdownNodeRendererContext context) {
                return new CoreMarkdownNodeRenderer(context);
            }
        });
    }

    /**
     * Create a new builder for configuring a {@link MarkdownRenderer}.
     *
     * @return a builder
     */
    public static Builder builder() {
        return new Builder();
    }

    @Override
    public void render(Node node, Appendable output) {
        RendererContext context = new RendererContext(new MarkdownWriter(output));
        context.render(node);
    }

    @Override
    public String render(Node node) {
        StringBuilder sb = new StringBuilder();
        render(node, sb);
        return sb.toString();
    }

    /**
     * Builder for configuring a {@link MarkdownRenderer}. See methods for default configuration.
     */
    public static class Builder {

        private List<MarkdownNodeRendererFactory> nodeRendererFactories = new ArrayList<>();

        /**
         * @return the configured {@link MarkdownRenderer}
         */
        public MarkdownRenderer build() {
            return new MarkdownRenderer(this);
        }

        /**
         * Add a factory for instantiating a node renderer (done when rendering). This allows to override the rendering
         * of node types or define rendering for custom node types.
         * <p>
         * If multiple node renderers for the same node type are created, the one from the factory that was added first
         * "wins". (This is how the rendering for core node types can be overridden; the default rendering comes last.)
         *
         * @param nodeRendererFactory the factory for creating a node renderer
         * @return {@code this}
         */
        public Builder nodeRendererFactory(MarkdownNodeRendererFactory nodeRendererFactory) {
            this.nodeRendererFactories.add(nodeRendererFactory);
            return this;
        }

        /**
         * @param extensions extensions to use on this renderer
         * @return {@code this}
         */
        public Builder extensions(Iterable<? extends Extension> extensions) {
            for (Extension extension : extensions) {
                if (extension instanceof MarkdownRendererExtension) {
                    MarkdownRendererExtension markdownRendererExtension = (MarkdownRendererExtension) extension;
                    markdownRendererExtension.extend(this);
                }
            }
            return this;
        }
    }

    /**
     * Extension for {@link MarkdownRenderer}.
     */
    public interface MarkdownRendererExtension extends Extension {
        void extend(Builder rendererBuilder);
    }

    private class RendererContext implements MarkdownNodeRendererContext {
        private final MarkdownWriter writer;
        private final NodeRendererMap nodeRendererMap = new NodeRendererMap();

        private RendererContext(MarkdownWriter writer) {
            this.writer = writer;

            // The first node renderer for a node type "wins".
            for (int i = nodeRendererFactories.size() - 1; i >= 0; i--) {
                MarkdownNodeRendererFactory nodeRendererFactory = nodeRendererFactories.get(i);
                NodeRenderer nodeRenderer = nodeRendererFactory.create(this);
                nodeRendererMap.add(nodeRenderer);
            }
        }

        @Override
        public MarkdownWriter getWriter() {
            return writer;
        }

        @Override
        public void render(Node node) {
            nodeRendererMap.render(node);
        }
    }
}
