package org.commonmark.parser;

import org.commonmark.node.LinkReferenceDefinition;
import org.commonmark.parser.beta.LinkProcessor;
import org.commonmark.parser.beta.InlineContentParserFactory;
import org.commonmark.parser.delimiter.DelimiterProcessor;

import java.util.List;
import java.util.Set;

/**
 * Context for inline parsing.
 */
public interface InlineParserContext {

    /**
     * @return custom inline content parsers that have been configured with
     * {@link Parser.Builder#customInlineContentParserFactory(InlineContentParserFactory)}
     */
    List<InlineContentParserFactory> getCustomInlineContentParserFactories();

    /**
     * @return custom delimiter processors that have been configured with
     * {@link Parser.Builder#customDelimiterProcessor(DelimiterProcessor)}
     */
    List<DelimiterProcessor> getCustomDelimiterProcessors();

    /**
     * @return custom link processors that have been configured with {@link Parser.Builder#linkProcessor}.
     */
    List<LinkProcessor> getCustomLinkProcessors();

    /**
     * @return custom link markers that have been configured with {@link Parser.Builder#linkMarker}.
     */
    Set<Character> getCustomLinkMarkers();

    /**
     * Look up a {@link LinkReferenceDefinition} for a given label.
     * <p>
     * Note that the passed in label does not need to be normalized; implementations are responsible for doing the
     * normalization before lookup.
     *
     * @param label the link label to look up
     * @return the definition if one exists, {@code null} otherwise
     * @deprecated use {@link #getDefinition} with {@link LinkReferenceDefinition} instead
     */
    @Deprecated
    LinkReferenceDefinition getLinkReferenceDefinition(String label);

    /**
     * Look up a definition of a type for a given label.
     * <p>
     * Note that the passed in label does not need to be normalized; implementations are responsible for doing the
     * normalization before lookup.
     *
     * @return the definition if one exists, null otherwise
     */
    <D> D getDefinition(Class<D> type, String label);
}
