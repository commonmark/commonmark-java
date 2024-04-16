package org.commonmark.internal;

import org.commonmark.internal.inline.InlineContentParser;
import org.commonmark.node.LinkReferenceDefinition;
import org.commonmark.parser.InlineParserContext;
import org.commonmark.parser.delimiter.DelimiterProcessor;

import java.util.List;

public class InlineParserContextImpl implements InlineParserContext {

    private final List<InlineContentParser> inlineContentParsers;
    private final List<DelimiterProcessor> delimiterProcessors;
    private final LinkReferenceDefinitions linkReferenceDefinitions;

    public InlineParserContextImpl(List<InlineContentParser> inlineContentParsers,
                                   List<DelimiterProcessor> delimiterProcessors,
                                   LinkReferenceDefinitions linkReferenceDefinitions) {
        this.inlineContentParsers = inlineContentParsers;
        this.delimiterProcessors = delimiterProcessors;
        this.linkReferenceDefinitions = linkReferenceDefinitions;
    }

    @Override
    public List<InlineContentParser> getCustomInlineContentParsers() {
        return inlineContentParsers;
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
