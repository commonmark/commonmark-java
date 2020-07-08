package org.commonmark.experimental;

import org.commonmark.experimental.setup.EmphasisNodeSetup;
import org.commonmark.experimental.setup.ImageLogoNodeSetup;
import org.commonmark.experimental.setup.LinkNodeSetup;
import org.commonmark.experimental.setup.StrongEmphasisNodeSetup;
import org.commonmark.parser.InlineParser;
import org.commonmark.parser.InlineParserContext;

public class InlineParserNodeSetupFactory implements org.commonmark.parser.InlineParserFactory {
    private static final InlineParserImpl inlineParser = InlineParserImpl.builder()
            .nodeSetup(new ImageLogoNodeSetup())
            .nodeSetup(new LinkNodeSetup())
            .nodeSetup(new StrongEmphasisNodeSetup())
            .nodeSetup(new EmphasisNodeSetup())
            .build();

    @Override
    public InlineParser create(InlineParserContext inlineParserContext) {
        return inlineParser;
    }
}
