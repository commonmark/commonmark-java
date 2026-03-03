package org.commonmark.ext.gfm.alerts;

import org.commonmark.Extension;
import org.commonmark.ext.gfm.alerts.internal.AlertPostProcessor;
import org.commonmark.ext.gfm.alerts.internal.AlertHtmlNodeRenderer;
import org.commonmark.ext.gfm.alerts.internal.AlertMarkdownNodeRenderer;
import org.commonmark.parser.Parser;
import org.commonmark.renderer.NodeRenderer;
import org.commonmark.renderer.html.HtmlNodeRendererContext;
import org.commonmark.renderer.html.HtmlNodeRendererFactory;
import org.commonmark.renderer.html.HtmlRenderer;
import org.commonmark.renderer.markdown.MarkdownNodeRendererContext;
import org.commonmark.renderer.markdown.MarkdownNodeRendererFactory;
import org.commonmark.renderer.markdown.MarkdownRenderer;

import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;

/**
 * Extension for GFM alerts using {@code [!TYPE]} syntax (GitHub Flavored Markdown).
 * <p>
 * Create with {@link #create()} or {@link #builder()} and configure on builders
 * ({@link org.commonmark.parser.Parser.Builder#extensions(Iterable)},
 * {@link HtmlRenderer.Builder#extensions(Iterable)}).
 * Parsed alerts become {@link Alert} blocks.
 */
public class AlertsExtension implements Parser.ParserExtension, HtmlRenderer.HtmlRendererExtension,
        MarkdownRenderer.MarkdownRendererExtension {

    static final Set<String> STANDARD_TYPES = Set.of("NOTE", "TIP", "IMPORTANT", "WARNING", "CAUTION");

    private final Map<String, String> customTypes;

    private AlertsExtension(Builder builder) {
        this.customTypes = new LinkedHashMap<>(builder.customTypes);
    }

    public static Extension create() {
        return builder().build();
    }

    public static Builder builder() {
        return new Builder();
    }

    @Override
    public void extend(Parser.Builder parserBuilder) {
        Set<String> allowedTypes = new HashSet<>(STANDARD_TYPES);
        allowedTypes.addAll(customTypes.keySet());
        parserBuilder.postProcessor(new AlertPostProcessor(allowedTypes));
    }

    @Override
    public void extend(HtmlRenderer.Builder rendererBuilder) {
        rendererBuilder.nodeRendererFactory(new HtmlNodeRendererFactory() {
            @Override
            public NodeRenderer create(HtmlNodeRendererContext context) {
                return new AlertHtmlNodeRenderer(context, customTypes);
            }
        });
    }

    @Override
    public void extend(MarkdownRenderer.Builder rendererBuilder) {
        rendererBuilder.nodeRendererFactory(new MarkdownNodeRendererFactory() {
            @Override
            public NodeRenderer create(MarkdownNodeRendererContext context) {
                return new AlertMarkdownNodeRenderer(context);
            }

            @Override
            public Set<Character> getSpecialCharacters() {
                return Set.of();
            }
        });
    }

    /**
     * Builder for configuring the alerts extension.
     */
    public static class Builder {
        private final Map<String, String> customTypes = new LinkedHashMap<>();

        /**
         * Adds a custom alert type with a display title.
         * <p>
         * This can also be used to override the display title of standard GFM types
         * (e.g., for localization).
         *
         * @param type the alert type (must be uppercase)
         * @param title the display title for this alert type
         * @return {@code this}
         */
        public Builder addCustomType(String type, String title) {
            if (type == null || type.isEmpty()) {
                throw new IllegalArgumentException("Type must not be null or empty");
            }
            if (title == null || title.isEmpty()) {
                throw new IllegalArgumentException("Title must not be null or empty");
            }
            if (!type.equals(type.toUpperCase())) {
                throw new IllegalArgumentException("Type must be uppercase: " + type);
            }
            customTypes.put(type, title);
            return this;
        }

        /**
         * @return a configured {@link Extension}
         */
        public Extension build() {
            return new AlertsExtension(this);
        }
    }
}
