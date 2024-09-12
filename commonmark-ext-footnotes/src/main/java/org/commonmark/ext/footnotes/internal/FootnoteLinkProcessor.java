package org.commonmark.ext.footnotes.internal;

import org.commonmark.ext.footnotes.FootnoteDefinition;
import org.commonmark.ext.footnotes.FootnoteReference;
import org.commonmark.ext.footnotes.InlineFootnote;
import org.commonmark.node.LinkReferenceDefinition;
import org.commonmark.node.Text;
import org.commonmark.parser.InlineParserContext;
import org.commonmark.parser.beta.LinkInfo;
import org.commonmark.parser.beta.LinkProcessor;
import org.commonmark.parser.beta.LinkResult;
import org.commonmark.parser.beta.Scanner;

/**
 * For turning e.g. <code>[^foo]</code> into a {@link FootnoteReference},
 * and <code>^[foo]</code> into an {@link InlineFootnote}.
 */
public class FootnoteLinkProcessor implements LinkProcessor {
    @Override
    public LinkResult process(LinkInfo linkInfo, Scanner scanner, InlineParserContext context) {

        if (linkInfo.marker() != null && linkInfo.marker().getLiteral().equals("^")) {
            // An inline footnote like ^[footnote text]. Note that we only get the marker here if the option is enabled
            // on the extension.
            return LinkResult.wrapTextIn(new InlineFootnote(), linkInfo.afterTextBracket()).includeMarker();
        }

        if (linkInfo.destination() != null) {
            // If it's an inline link, it can't be a footnote reference
            return LinkResult.none();
        }

        var text = linkInfo.text();
        if (!text.startsWith("^")) {
            // Footnote reference needs to start with [^
            return LinkResult.none();
        }

        if (linkInfo.label() != null && context.getDefinition(LinkReferenceDefinition.class, linkInfo.label()) != null) {
            // If there's a label after the text and the label has a definition -> it's a link, and it should take
            // preference, e.g. in `[^foo][bar]` if `[bar]` has a definition, `[^foo]` won't be a footnote reference.
            return LinkResult.none();
        }

        var label = text.substring(1);
        // Check if we have a definition, otherwise ignore (same behavior as for link reference definitions).
        // Note that the definition parser already checked the syntax of the label, we don't need to check again.
        var def = context.getDefinition(FootnoteDefinition.class, label);
        if (def == null) {
            return LinkResult.none();
        }

        // For footnotes, we only ever consume the text part of the link, not the label part (if any)
        var position = linkInfo.afterTextBracket();
        // If the marker is `![`, we don't want to include the `!`, so start from bracket
        return LinkResult.replaceWith(new FootnoteReference(label), position);
    }
}
