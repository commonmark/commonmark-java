package org.commonmark.html.attribute;

/**
 * Factory for instantiating new attribute providers when rendering is done.
 */
public interface AttributeProviderFactory {

    /**
     * Create a new attribute provider.
     *
     * @param context for this attribute provider
     * @return an AttributeProvider
     */
    AttributeProvider create(AttributeProviderContext context);
}
