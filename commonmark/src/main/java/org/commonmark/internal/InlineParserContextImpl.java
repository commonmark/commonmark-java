package org.commonmark.internal;

import org.commonmark.node.LinkReferenceDefinition;
import org.commonmark.parser.InlineParserContext;
import org.commonmark.parser.beta.LinkProcessor;
import org.commonmark.parser.beta.InlineContentParserFactory;
import org.commonmark.parser.delimiter.DelimiterProcessor;

import java.util.List;

public class InlineParserContextImpl implements InlineParserContext {

    private final List<InlineContentParserFactory> inlineContentParserFactories;
    private final List<DelimiterProcessor> delimiterProcessors;
    private final List<LinkProcessor> linkProcessors;
    private final Definitions definitions;

    public InlineParserContextImpl(List<InlineContentParserFactory> inlineContentParserFactories,
                                   List<DelimiterProcessor> delimiterProcessors,
                                   List<LinkProcessor> linkProcessors,
                                   Definitions definitions) {
        this.inlineContentParserFactories = inlineContentParserFactories;
        this.delimiterProcessors = delimiterProcessors;
        this.linkProcessors = linkProcessors;
        this.definitions = definitions;
    }

    @Override
    public List<InlineContentParserFactory> getCustomInlineContentParserFactories() {
        return inlineContentParserFactories;
    }

    @Override
    public List<DelimiterProcessor> getCustomDelimiterProcessors() {
        return delimiterProcessors;
    }

    @Override
    public List<LinkProcessor> getCustomLinkProcessors() {
        return linkProcessors;
    }

    @Override
    public LinkReferenceDefinition getLinkReferenceDefinition(String label) {
        return definitions.getDefinition(LinkReferenceDefinition.class, label);
    }

    @Override
    public <D> D getDefinition(Class<D> type, String label) {
        return definitions.getDefinition(type, label);
    }
}
