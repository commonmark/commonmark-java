package org.commonmark.parser.beta;

import org.commonmark.parser.InlineParserContext;

public interface BracketProcessor {

    BracketResult process(BracketInfo bracketInfo, Scanner scanner, InlineParserContext context);
}
