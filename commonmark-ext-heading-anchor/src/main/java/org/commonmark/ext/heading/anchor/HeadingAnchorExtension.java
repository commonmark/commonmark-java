package org.commonmark.ext.heading.anchor;

import org.commonmark.Extension;
import org.commonmark.html.HtmlRenderer;

import org.commonmark.ext.heading.anchor.internal.HeadingIdAttributeProvider;

/**
 * Extension for adding auto generated ids to headings
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
 */
public class HeadingAnchorExtension implements HtmlRenderer.HtmlRendererExtension {

    private HeadingAnchorExtension() {
    }

    public static Extension create() {
        return new HeadingAnchorExtension();
    }

    @Override
    public void extend(HtmlRenderer.Builder rendererBuilder) {
        rendererBuilder.attributeProvider(HeadingIdAttributeProvider.create());
    }
}
