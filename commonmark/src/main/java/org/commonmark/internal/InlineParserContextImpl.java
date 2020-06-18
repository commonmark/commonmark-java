package org.commonmark.internal;

import org.commonmark.node.LinkReferenceDefinition;
import org.commonmark.parser.InlineParser;
import org.commonmark.parser.InlineParserContext;
import org.commonmark.parser.delimiter.DelimiterProcessor;

import java.util.List;
import java.util.Map;

public class InlineParserContextImpl implements InlineParserContext {

    private final List<DelimiterProcessor> delimiterProcessors;
    private final Map<String, LinkReferenceDefinition> linkReferenceDefinitions;
    private final List<InlineParser.NodeExtension> nodeExtensions;

    public InlineParserContextImpl(List<DelimiterProcessor> delimiterProcessors,
                                   Map<String, LinkReferenceDefinition> linkReferenceDefinitions,
                                   List<InlineParser.NodeExtension> nodeExtensions) {
        this.delimiterProcessors = delimiterProcessors;
        this.linkReferenceDefinitions = linkReferenceDefinitions;
        this.nodeExtensions = nodeExtensions;
    }

    @Override
    public List<DelimiterProcessor> getCustomDelimiterProcessors() {
        return delimiterProcessors;
    }

    @Override
    public LinkReferenceDefinition getLinkReferenceDefinition(String label) {
        return linkReferenceDefinitions.get(label);
    }

    @Override
    public List<InlineParser.NodeExtension> nodeExtensions() {
        return nodeExtensions;
    }
}
