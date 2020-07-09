package org.commonmark.experimental;

import org.commonmark.experimental.setup.EmphasisNodeSetup;
import org.commonmark.experimental.setup.ImageLogoNodeSetup;
import org.commonmark.experimental.setup.LinkNodeSetup;
import org.commonmark.experimental.setup.StrongEmphasisNodeSetup;
import org.commonmark.parser.InlineParser;
import org.commonmark.parser.InlineParserContext;

import java.util.ArrayList;
import java.util.List;

public class InlineParserNodeSetupFactory implements org.commonmark.parser.InlineParserFactory {
    private final InlineParserImpl inlineParser;

    @Override
    public InlineParser create(InlineParserContext inlineParserContext) {
        return inlineParser;
    }

    public InlineParserNodeSetupFactory() {
        this(new ArrayList<TextNodeIdentifierSetup>());
    }

    public InlineParserNodeSetupFactory(List<TextNodeIdentifierSetup> nodeSetupExtensions) {
        inlineParser = InlineParserImpl.builder()
                .nodeSetup(new ImageLogoNodeSetup())
                .nodeSetup(new LinkNodeSetup())
                .nodeSetup(new StrongEmphasisNodeSetup())
                .nodeSetup(new EmphasisNodeSetup())
                .nodeSetup(nodeSetupExtensions)
                .build();
    }
}
