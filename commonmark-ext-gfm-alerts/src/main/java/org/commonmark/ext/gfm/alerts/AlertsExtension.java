package org.commonmark.ext.gfm.alerts;

import org.commonmark.Extension;
import org.commonmark.ext.gfm.alerts.internal.AlertBlockParser;
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

import java.util.HashMap;
import java.util.Locale;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Extension for GFM alerts using {@code [!TYPE]} syntax (GitHub Flavored Markdown).
 * <p>
 * Create with {@link #create()} or {@link #builder()} and configure on builders
 * ({@link org.commonmark.parser.Parser.Builder#extensions(Iterable)},
 * {@link HtmlRenderer.Builder#extensions(Iterable)}).
 * Parsed alerts become {@link Alert} blocks.
 *
 * The {@link #create() default configuration} of this extension will match GFM
 * exactly, with the following exceptions:
 *
 * - Alert markers take precedence over link reference definitions.
 * - Lazy continuation is not allowed between the marker and the body text. Example:
 *
 *   <pre>{@code
 *   > [!NOTE]
 *   Lazy body text will be parsed as a new paragraph
 *   }</pre>
 */
public class AlertsExtension implements Parser.ParserExtension, HtmlRenderer.HtmlRendererExtension,
        MarkdownRenderer.MarkdownRendererExtension {

    static final Set<String> STANDARD_TYPES = Set.of("NOTE", "TIP", "IMPORTANT", "WARNING", "CAUTION");

    private final Map<String, String> customTypes;
    private final boolean customTitlesAllowed;
    private final boolean nestedAlertsAllowed;

    private AlertsExtension(Builder builder) {
        this.customTypes = new HashMap<>(builder.customTypes);
        this.customTitlesAllowed = builder.customTitlesAllowed;
        this.nestedAlertsAllowed = builder.nestedAlertsAllowed;
    }

    public static Extension create() {
        return builder().build();
    }

    public static Builder builder() {
        return new Builder();
    }

    @Override
    public void extend(Parser.Builder parserBuilder) {
        var allowedTypes = new HashSet<>(STANDARD_TYPES);
        allowedTypes.addAll(customTypes.keySet());
        parserBuilder.customBlockParserFactory(
            new AlertBlockParser.Factory(allowedTypes, customTitlesAllowed, nestedAlertsAllowed));
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
        private final Map<String, String> customTypes = new HashMap<>();
        private boolean customTitlesAllowed = false;
        private boolean nestedAlertsAllowed = false;

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
            if (!type.equals(type.toUpperCase(Locale.ROOT))) {
                throw new IllegalArgumentException("Type must be uppercase: " + type);
            }
            customTypes.put(type, title);
            return this;
        }

        /**
         * Allows custom titles on alerts. See {@link AlertTitle} for more information.
         * @return {@code this}
         */
        public Builder allowCustomTitles() {
            customTitlesAllowed = true;
            return this;
        }

        /**
         * Disallows custom titles on alerts. See {@link AlertTitle} for more information.
         * @return {@code this}
         */
        public Builder disallowCustomTitles() {
            customTitlesAllowed = false;
            return this;
        }

        /**
         * Allows alerts to be parsed within blocks other than {@code Document} (the root).
         * <p>
         * Note that even with this enabled, {@link Parser.Builder#maxOpenBlockParsers(int)}
         * will be respected.
         * @return {@code this}
         */
        public Builder allowNestedAlerts() {
            nestedAlertsAllowed = true;
            return this;
        }

        /**
         * Prevents alerts from being parsed within blocks other than {@code Document}
         * (the root). If an alert appears within another block, it will be parsed as
         * a regular {@code BlockQuote}.
         * @return {@code this}
         */
        public Builder disallowNestedAlerts() {
            nestedAlertsAllowed = false;
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
