package org.commonmark.internal;

import org.commonmark.parser.beta.InlineContentParserFactory;
import org.commonmark.node.LinkReferenceDefinition;
import org.commonmark.parser.InlineParserContext;
import org.commonmark.parser.delimiter.DelimiterProcessor;

import java.util.List;

public class InlineParserContextImpl implements InlineParserContext {

    private final List<InlineContentParserFactory> inlineContentParserFactories;
    private final List<DelimiterProcessor> delimiterProcessors;
    private final LinkReferenceDefinitions linkReferenceDefinitions;

    public InlineParserContextImpl(List<InlineContentParserFactory> inlineContentParserFactories,
                                   List<DelimiterProcessor> delimiterProcessors,
                                   LinkReferenceDefinitions linkReferenceDefinitions) {
        this.inlineContentParserFactories = inlineContentParserFactories;
        this.delimiterProcessors = delimiterProcessors;
        this.linkReferenceDefinitions = linkReferenceDefinitions;
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
    public LinkReferenceDefinition getLinkReferenceDefinition(String label) {
        return linkReferenceDefinitions.get(label);
    }
}
