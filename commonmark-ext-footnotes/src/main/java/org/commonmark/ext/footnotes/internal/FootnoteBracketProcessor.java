package org.commonmark.ext.footnotes.internal;

import org.commonmark.ext.footnotes.FootnoteDefinition;
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
            var label = text.substring(1);
            // Check if we have a definition, otherwise ignore (same behavior as for link reference definitions)
            var def = context.getDefinition(FootnoteDefinition.class, label);
            if (def != null) {
                // For footnotes, we only ever consume the text part of the link, not the label part (if any).
                var position = bracketInfo.afterTextBracket();
                return BracketResult.replaceWith(new FootnoteReference(label), position);
            }
        }
        return BracketResult.none();
    }
}
