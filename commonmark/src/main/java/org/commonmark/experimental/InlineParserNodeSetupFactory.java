package org.commonmark.experimental;

import org.commonmark.experimental.setup.ImageLogoNodeSetup;
import org.commonmark.parser.InlineParser;
import org.commonmark.parser.InlineParserContext;

public class InlineParserNodeSetupFactory implements org.commonmark.parser.InlineParserFactory {
    @Override
    public InlineParser create(InlineParserContext inlineParserContext) {
        return InlineParserImpl.builder()
                .nodeSetup(new ImageLogoNodeSetup())
                .build();
    }
}
