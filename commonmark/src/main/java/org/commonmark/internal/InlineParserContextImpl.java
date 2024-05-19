package org.commonmark.internal;

import org.commonmark.node.DefinitionMap;
import org.commonmark.node.LinkReferenceDefinition;
import org.commonmark.parser.InlineParserContext;
import org.commonmark.parser.beta.BracketProcessor;
import org.commonmark.parser.beta.InlineContentParserFactory;
import org.commonmark.parser.delimiter.DelimiterProcessor;

import java.util.List;

public class InlineParserContextImpl implements InlineParserContext {

    private final List<InlineContentParserFactory> inlineContentParserFactories;
    private final List<DelimiterProcessor> delimiterProcessors;
    private final List<BracketProcessor> bracketProcessors;
    private final DefinitionMap<LinkReferenceDefinition> linkReferenceDefinitions;

    public InlineParserContextImpl(List<InlineContentParserFactory> inlineContentParserFactories,
                                   List<DelimiterProcessor> delimiterProcessors,
                                   List<BracketProcessor> bracketProcessors,
                                   DefinitionMap<LinkReferenceDefinition> linkReferenceDefinitions) {
        this.inlineContentParserFactories = inlineContentParserFactories;
        this.delimiterProcessors = delimiterProcessors;
        this.bracketProcessors = bracketProcessors;
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
    public List<BracketProcessor> getCustomBracketProcessors() {
        return bracketProcessors;
    }

    @Override
    public LinkReferenceDefinition getLinkReferenceDefinition(String label) {
        return linkReferenceDefinitions.get(label);
    }
}
