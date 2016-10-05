package org.commonmark.ext.heading.anchor;

import org.commonmark.Extension;
import org.commonmark.ext.heading.anchor.internal.HeadingIdAttributeProvider;
import org.commonmark.renderer.html.AttributeProvider;
import org.commonmark.renderer.html.AttributeProviderContext;
import org.commonmark.renderer.html.AttributeProviderFactory;
import org.commonmark.renderer.html.HtmlRenderer;

/**
 * Extension for adding auto generated IDs to headings.
 * <p>
 * Create it with {@link #create()} or {@link #builder()} and then configure it on the
 * renderer builder ({@link HtmlRenderer.Builder#extensions(Iterable)}).
 * <p>
 * The heading text will be used to create the id. Multiple headings with the
 * same text will result in appending a hyphen and number. For example:
 * <pre><code>
 * # Heading
 * # Heading
 * </code></pre>
 * will result in
 * <pre><code>
 * &lt;h1 id="heading"&gt;Heading&lt;/h1&gt;
 * &lt;h1 id="heading-1"&gt;Heading&lt;/h1&gt;
 * </code></pre>
 *
 * @see IdGenerator the IdGenerator class if just the ID generation part is needed
 */
public class HeadingAnchorExtension implements HtmlRenderer.HtmlRendererExtension {

    private final String defaultId;
    private final String idPrefix;
    private final String idSuffix;

    private HeadingAnchorExtension(Builder builder) {
        this.defaultId = builder.defaultId;
        this.idPrefix = builder.idPrefix;
        this.idSuffix = builder.idSuffix;
    }

    /**
     * @return the extension built with default settings
     */
    public static Extension create() {
        return new HeadingAnchorExtension(builder());
    }

    /**
     * @return a builder to configure the extension settings
     */
    public static Builder builder() {
        return new Builder();
    }

    @Override
    public void extend(HtmlRenderer.Builder rendererBuilder) {
        rendererBuilder.attributeProviderFactory(new AttributeProviderFactory() {
            @Override
            public AttributeProvider create(AttributeProviderContext context) {
                return HeadingIdAttributeProvider.create(defaultId, idPrefix, idSuffix);
            }
        });
    }

    public static class Builder {
        private String defaultId = "id";
        private String idPrefix = "";
        private String idSuffix = "";

        /**
         * @param value Default value for the id to take if no generated id can be extracted. Default "id"
         * @return {@code this}
         */
        public Builder defaultId(String value) {
            this.defaultId = value;
            return this;
        }

        /**
         * @param value Set the value to be prepended to every id generated. Default ""
         * @return {@code this}
         */
        public Builder idPrefix(String value) {
            this.idPrefix = value;
            return this;
        }

        /**
         * @param value Set the value to be appended to every id generated. Default ""
         * @return {@code this}
         */
        public Builder idSuffix(String value) {
            this.idSuffix = value;
            return this;
        }

        /**
         * @return a configured extension
         */
        public Extension build() {
            return new HeadingAnchorExtension(this);
        }
    }
}
