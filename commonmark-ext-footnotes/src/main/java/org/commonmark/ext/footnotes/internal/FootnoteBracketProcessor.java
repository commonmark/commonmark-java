package org.commonmark.ext.footnotes.internal;

import org.commonmark.ext.footnotes.FootnoteReference;
import org.commonmark.parser.InlineParserContext;
import org.commonmark.parser.beta.BracketInfo;
import org.commonmark.parser.beta.BracketProcessor;
import org.commonmark.parser.beta.BracketResult;
import org.commonmark.parser.beta.Scanner;

public class FootnoteBracketProcessor implements BracketProcessor {
    @Override
    public BracketResult process(BracketInfo bracketInfo, Scanner scanner, InlineParserContext context) {
        // TODO: Does parsing need to be more strict here?
        var text = bracketInfo.text();
        if (text.startsWith("^")) {
            // TODO: Do we need to check if a definition exists before doing this? (That would be the same as reference
            //  links.)

            // For footnotes, we only ever consume the text part of the link, not the label part (if any).
            var position = bracketInfo.afterTextBracket();
            var label = text.substring(1);
            return BracketResult.replaceWith(new FootnoteReference(label), position);
        }
        return BracketResult.none();
    }
}
