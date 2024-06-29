package org.commonmark.parser.beta;

import org.commonmark.parser.InlineParserContext;

public interface LinkProcessor {

    LinkResult process(LinkInfo linkInfo, Scanner scanner, InlineParserContext context);
}
