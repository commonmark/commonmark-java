package org.commonmark.ext.styles;

import org.commonmark.Extension;
import org.commonmark.ext.styles.internal.StylesAttributeProvider;
import org.commonmark.ext.styles.internal.StylesDelimiterProcessor;
import org.commonmark.parser.Parser;
import org.commonmark.renderer.html.AttributeProvider;
import org.commonmark.renderer.html.AttributeProviderContext;
import org.commonmark.renderer.html.AttributeProviderFactory;
import org.commonmark.renderer.html.HtmlRenderer;

/**
 * Extension for adding styles to nodes.
 * <p>
 * Create it with {@link #create()} and then configure it on the builders
 * ({@link org.commonmark.parser.Parser.Builder#extensions(Iterable)},
 * {@link HtmlRenderer.Builder#extensions(Iterable)}).
 * </p>
 */
public class StylesExtension implements Parser.ParserExtension, HtmlRenderer.HtmlRendererExtension {

    private StylesExtension() {
    }

    static Extension create() {
        return new StylesExtension();
    }

    @Override
    public void extend(Parser.Builder parserBuilder) {
        parserBuilder.customDelimiterProcessor(new StylesDelimiterProcessor());
    }

    @Override
    public void extend(HtmlRenderer.Builder rendererBuilder) {
        rendererBuilder.attributeProviderFactory(new AttributeProviderFactory() {
            @Override
            public AttributeProvider create(AttributeProviderContext context) {
                return StylesAttributeProvider.create();
            }
        });
    }
}
