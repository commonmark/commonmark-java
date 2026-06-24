package org.commonmark.internal;

import org.commonmark.parser.IncludeSourceSpans;
import org.commonmark.parser.InlineParserFactory;
import org.commonmark.parser.beta.InlineContentParserFactory;
import org.commonmark.parser.beta.LinkProcessor;
import org.commonmark.parser.block.BlockParserFactory;
import org.commonmark.parser.delimiter.DelimiterProcessor;

import java.util.List;
import java.util.Set;

public class DocumentParserConfig {

    private final List<BlockParserFactory> blockParserFactories;
    private final InlineParserFactory inlineParserFactory;
    private final List<InlineContentParserFactory> inlineContentParserFactories;
    private final List<DelimiterProcessor> delimiterProcessors;
    private final List<LinkProcessor> linkProcessors;
    private final Set<Character> linkMarkers;
    private final IncludeSourceSpans includeSourceSpans;
    private final int maxOpenBlockParsers;

    public DocumentParserConfig(
            List<BlockParserFactory> blockParserFactories,
            InlineParserFactory inlineParserFactory,
            List<InlineContentParserFactory> inlineContentParserFactories,
            List<DelimiterProcessor> delimiterProcessors,
            List<LinkProcessor> linkProcessors,
            Set<Character> linkMarkers,
            IncludeSourceSpans includeSourceSpans,
            int maxOpenBlockParsers) {

        this.blockParserFactories = blockParserFactories;
        this.inlineParserFactory = inlineParserFactory;
        this.inlineContentParserFactories = inlineContentParserFactories;
        this.delimiterProcessors = delimiterProcessors;
        this.linkProcessors = linkProcessors;
        this.linkMarkers = linkMarkers;
        this.includeSourceSpans = includeSourceSpans;
        this.maxOpenBlockParsers = maxOpenBlockParsers;
    }

    // getters
    public List<BlockParserFactory> getBlockParserFactories() {
        return blockParserFactories;
    }

    public InlineParserFactory getInlineParserFactory() {
        return inlineParserFactory;
    }

    public List<InlineContentParserFactory> getInlineContentParserFactories() {
        return inlineContentParserFactories;
    }

    public List<DelimiterProcessor> getDelimiterProcessors() {
        return delimiterProcessors;
    }

    public List<LinkProcessor> getLinkProcessors() {
        return linkProcessors;
    }

    public Set<Character> getLinkMarkers() {
        return linkMarkers;
    }

    public IncludeSourceSpans getIncludeSourceSpans() {
        return includeSourceSpans;
    }

    public int getMaxOpenBlockParsers() {
        return maxOpenBlockParsers;
    }
}
