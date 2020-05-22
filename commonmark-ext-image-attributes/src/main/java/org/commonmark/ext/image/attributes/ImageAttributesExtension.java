package org.commonmark.ext.image.attributes;

import org.commonmark.Extension;
import org.commonmark.ext.image.attributes.internal.ImageAttributesAttributeProvider;
import org.commonmark.ext.image.attributes.internal.ImageAttributesDelimiterProcessor;
import org.commonmark.parser.Parser;
import org.commonmark.renderer.html.AttributeProvider;
import org.commonmark.renderer.html.AttributeProviderContext;
import org.commonmark.renderer.html.AttributeProviderFactory;
import org.commonmark.renderer.html.HtmlRenderer;

/**
 * Extension for adding attributes to image nodes.
 * <p>
 * Create it with {@link #create()} and then configure it on the builders
 * ({@link org.commonmark.parser.Parser.Builder#extensions(Iterable)},
 * {@link HtmlRenderer.Builder#extensions(Iterable)}).
 * </p>
 *
 * @since 0.15.0
 */
public class ImageAttributesExtension implements Parser.ParserExtension, HtmlRenderer.HtmlRendererExtension {

    private ImageAttributesExtension() {
    }

    public static Extension create() {
        return new ImageAttributesExtension();
    }

    @Override
    public void extend(Parser.Builder parserBuilder) {
        parserBuilder.customDelimiterProcessor(new ImageAttributesDelimiterProcessor());
    }

    @Override
    public void extend(HtmlRenderer.Builder rendererBuilder) {
        rendererBuilder.attributeProviderFactory(new AttributeProviderFactory() {
            @Override
            public AttributeProvider create(AttributeProviderContext context) {
                return ImageAttributesAttributeProvider.create();
            }
        });
    }
}
