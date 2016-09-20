package org.commonmark.ext.heading.anchor;

import org.commonmark.Extension;
import org.commonmark.ext.heading.anchor.internal.HeadingIdAttributeProvider;
import org.commonmark.html.HtmlRenderer;
import org.commonmark.html.attribute.AttributeProvider;
import org.commonmark.html.attribute.AttributeProviderContext;
import org.commonmark.html.attribute.AttributeProviderFactory;

/**
 * Extension for adding auto generated IDs to headings.
 * <p>
 * Create it with {@link #create()} and then configure it on the builder
 * {@link org.commonmark.html.HtmlRenderer.Builder#extensions(Iterable)}).
 * </p>
 * <p>
 * The heading text will be used to create the id. Multiple headings with the
 * same text will result in appending a hyphen and number. For example:
 * </p>
 * <code>
 * <pre>
 * # Heading
 * # Heading
 * </pre>
 * </code>
 * will result in
 * <code>
 * <pre>
 * &lt;h1 id="heading"&gt;Heading&lt;/h1&gt;
 * &lt;h1 id="heading-1"&gt;Heading&lt;/h1&gt;
 * </pre>
 * </code>
 *
 * @see IdGenerator the IdGenerator class if just the ID generation part is needed
 */
public class HeadingAnchorExtension implements HtmlRenderer.HtmlRendererExtension {

    private HeadingAnchorExtension() {
    }

    public static Extension create() {
        return new HeadingAnchorExtension();
    }

    @Override
    public void extend(HtmlRenderer.Builder rendererBuilder) {
        rendererBuilder.attributeProviderFactory(new AttributeProviderFactory() {
            @Override
            public AttributeProvider create(AttributeProviderContext context) {
                return HeadingIdAttributeProvider.create();
            }
        });
    }
}
