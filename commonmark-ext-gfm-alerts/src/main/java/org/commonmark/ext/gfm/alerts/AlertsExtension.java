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
        rendererBuilder.nodeRendererFactory(context -> new AlertHtmlNodeRenderer(context, customTypes));
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
         * Allows or disallows custom titles on alerts. Inline formatting is supported
         * within these titles.
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
