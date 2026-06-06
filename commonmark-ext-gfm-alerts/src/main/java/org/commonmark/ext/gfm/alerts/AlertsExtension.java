package org.commonmark.ext.gfm.alerts;

import org.commonmark.Extension;
import org.commonmark.ext.gfm.alerts.internal.AlertBlockParser;
import org.commonmark.ext.gfm.alerts.internal.AlertHtmlNodeRenderer;
import org.commonmark.ext.gfm.alerts.internal.AlertMarkdownNodeRenderer;
import org.commonmark.parser.Parser;
import org.commonmark.renderer.NodeRenderer;
import org.commonmark.renderer.html.HtmlRenderer;
import org.commonmark.renderer.markdown.MarkdownNodeRendererContext;
import org.commonmark.renderer.markdown.MarkdownNodeRendererFactory;
import org.commonmark.renderer.markdown.MarkdownRenderer;

import java.util.HashMap;
import java.util.Locale;
import java.util.HashSet;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

/**
 * Extension for GFM alerts using {@code [!TYPE]} syntax (GitHub Flavored Markdown).
 * <p>
 * Create with {@link #create()} or {@link #builder()} and configure on builders
 * ({@link Parser.Builder#extensions(Iterable)}, {@link HtmlRenderer.Builder#extensions(Iterable)}).
 * Parsed alerts become {@link Alert} blocks. If custom alert titles are allowed
 * via {@link Builder#allowCustomTitles(boolean)}, the inline formatting of those
 * titles will be parsed into {@link AlertTitle} nodes.
 *
 * The {@link #create() default configuration} of this extension will match GFM
 * exactly, with the following exceptions:
 *
 * - Alert markers take precedence over <a href="https://spec.commonmark.org/current/#shortcut-reference-link">shortcut reference links</a>.
 * - Alerts with no content are allowed. Example:
 *
 *   <pre>{@code
 *   <!-- Valid -->
 *   > [!NOTE]
 *
 *   <!-- Also valid if custom titles are allowed -->
 *   > [!NOTE] Custom title
 *   }</pre>
 * - Lazy continuation is not allowed between the marker and the body text. Example:
 *
 *   <pre>{@code
 *   > [!NOTE]
 *   Lazy body text will be parsed as a new paragraph
 *   }</pre>
 */
public class AlertsExtension implements Parser.ParserExtension, HtmlRenderer.HtmlRendererExtension,
        MarkdownRenderer.MarkdownRendererExtension {

    /**
     * The standard GitHub Flavored Markdown (GFM) types that the extension
     * enables by default. These can be overwritten with {@link Builder#setAllowedTypes(Map)}.
     */
    public static final Map<String, String> STANDARD_TYPES = Map.ofEntries(
        Map.entry("NOTE", "Note"),
        Map.entry("TIP", "Tip"),
        Map.entry("IMPORTANT", "Important"),
        Map.entry("WARNING", "Warning"),
        Map.entry("CAUTION", "Caution")
    );

    /**
     * A map of alert marker ({@code [!TYPE]}) to the default title for that marker.
     */
    private final Map<String, String> allowedTypes;
    private final boolean customTitlesAllowed;
    private final boolean nestedAlertsAllowed;

    private AlertsExtension(Builder builder) {
        this.allowedTypes = new HashMap<>(builder.allowedTypes);
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
        var allowedTypesSet = new HashSet<>(allowedTypes.keySet());
        parserBuilder.customBlockParserFactory(
            new AlertBlockParser.Factory(allowedTypesSet, customTitlesAllowed, nestedAlertsAllowed));
    }

    @Override
    public void extend(HtmlRenderer.Builder rendererBuilder) {
        rendererBuilder.nodeRendererFactory(context -> new AlertHtmlNodeRenderer(context, allowedTypes));
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
        private Map<String, String> allowedTypes = new HashMap<>(STANDARD_TYPES);
        private boolean customTitlesAllowed = false;
        private boolean nestedAlertsAllowed = false;

        /**
         * Sets which alert types will be recognized and parsed into {@link Alert} blocks,
         * completely overwriting any previous configuration.
         * <p>
         * By default, {@link AlertsExtension#STANDARD_TYPES} are used.
         *
         * @param allowedTypes A map of alert type to the default title for that type.
         *                     Must not be null/empty or contain any null/empty keys or
         *                     values. Additionally, all alert types must be uppercase.
         * @return {@code this}
         */
        public Builder setAllowedTypes(Map<String, String> allowedTypes) {
            Objects.requireNonNull(allowedTypes, "allowedTypes must not be null");
            if (allowedTypes.isEmpty()) {
                throw new IllegalArgumentException("allowedTypes must not be empty");
            }

            for (Map.Entry<String, String> entry : allowedTypes.entrySet()) {
                var type = Objects.requireNonNull(entry.getKey(), "Types must not be null");
                if (type.isEmpty()) {
                    throw new IllegalArgumentException("Types must not be empty");
                }
                if (!type.equals(type.toUpperCase(Locale.ROOT))) {
                    throw new IllegalArgumentException("Types must be uppercase: " + type);
                }

                var defaultTitle = Objects.requireNonNull(entry.getValue(), "Default titles must not be null: " + type);
                if (defaultTitle.isEmpty()) {
                    throw new IllegalArgumentException("Default titles must not be empty: " + type);
                }
            }

            this.allowedTypes = Map.copyOf(allowedTypes);
            return this;
        }

        /**
         * Allows or disallows custom titles on alerts. Inline formatting is supported
         * within these titles.
         *
         * @param allow Whether to allow or disallow custom titles on alerts.
         * @return {@code this}
         * @see AlertTitle
         */
        public Builder allowCustomTitles(boolean allow) {
            customTitlesAllowed = allow;
            return this;
        }

        /**
         * Allows or disallows parsing alerts within non-root blocks ({@code Document}).
         * <p>
         * When disallowed, if an alert appears within another block, it will be parsed as
         * a regular {@code BlockQuote}.
         * <p>
         * Note that even when this is allowed, {@link Parser.Builder#maxOpenBlockParsers(int)}
         * will be respected.
         *
         * @param allow Whether to allow or disallow parsing alerts within non-root blocks.
         * @return {@code this}
         */
        public Builder allowNestedAlerts(boolean allow) {
            nestedAlertsAllowed = allow;
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
