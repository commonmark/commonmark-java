package org.commonmark.ext.footnotes.internal;

import org.commonmark.ext.footnotes.FootnoteDefinition;
import org.commonmark.ext.footnotes.FootnoteReference;
import org.commonmark.node.LinkReferenceDefinition;
import org.commonmark.parser.InlineParserContext;
import org.commonmark.parser.beta.BracketInfo;
import org.commonmark.parser.beta.BracketProcessor;
import org.commonmark.parser.beta.BracketResult;
import org.commonmark.parser.beta.Scanner;

public class FootnoteBracketProcessor implements BracketProcessor {
    @Override
    public BracketResult process(BracketInfo bracketInfo, Scanner scanner, InlineParserContext context) {
        var text = bracketInfo.text();
        if (!text.startsWith("^")) {
            // Footnote reference needs to start with [^
            return BracketResult.none();
        }

        if (bracketInfo.label() != null && context.getDefinition(LinkReferenceDefinition.class, bracketInfo.label()) != null) {
            // If there's a label after the text and the label has a definition -> it's a link, and it should take
            // preference, e.g. in `[^foo][bar]` if `[bar]` has a definition, `[^foo]` won't be a footnote reference.
            return BracketResult.none();
        }

        var label = text.substring(1);
        // Check if we have a definition, otherwise ignore (same behavior as for link reference definitions).
        // Note that the definition parser already checked the syntax of the label, we don't need to check again.
        var def = context.getDefinition(FootnoteDefinition.class, label);
        if (def == null) {
            return BracketResult.none();
        }

        // For footnotes, we only ever consume the text part of the link, not the label part (if any)
        var position = bracketInfo.afterTextBracket();
        // If the marker is `![`, we don't want to include the `!`, so start from bracket
        return BracketResult.replaceWith(new FootnoteReference(label), position).startFromBracket();
    }
}
