package org.commonmark.internal.inline;

import org.commonmark.node.InlineImage;
import org.commonmark.node.InlineLink;
import org.commonmark.node.LinkReferenceDefinition;
import org.commonmark.node.ReferenceImage;
import org.commonmark.node.ReferenceLink;
import org.commonmark.parser.InlineParserContext;
import org.commonmark.parser.beta.LinkInfo;
import org.commonmark.parser.beta.LinkProcessor;
import org.commonmark.parser.beta.LinkResult;
import org.commonmark.parser.beta.Scanner;

public class CoreLinkProcessor implements LinkProcessor {

    @Override
    public LinkResult process(LinkInfo linkInfo, Scanner scanner, InlineParserContext context) {
        if (linkInfo.destination() != null) {
            // Inline link: destination written directly in source
            return processInline(linkInfo, scanner, linkInfo.destination(), linkInfo.title());
        }

        // Reference link: look up definition by label (or text for shortcut/collapsed)
        var label = linkInfo.label();
        var ref = label != null && !label.isEmpty() ? label : linkInfo.text();
        var def = context.getDefinition(LinkReferenceDefinition.class, ref);
        if (def != null) {
            // Mirror LinkInfo label encoding: null = shortcut, "" = collapsed, non-empty = full
            String nodeLabel = label == null ? null : label.isEmpty() ? "" : label;
            return processReference(linkInfo, scanner, nodeLabel, def);
        }
        return LinkResult.none();
    }

    private static LinkResult processInline(
            LinkInfo linkInfo, Scanner scanner, String destination, String title) {
        if (linkInfo.marker() != null && linkInfo.marker().getLiteral().equals("!")) {
            return LinkResult.wrapTextIn(new InlineImage(destination, title), scanner.position())
                    .includeMarker();
        }
        return LinkResult.wrapTextIn(new InlineLink(destination, title), scanner.position());
    }

    private static LinkResult processReference(
            LinkInfo linkInfo, Scanner scanner, String label, LinkReferenceDefinition definition) {
        if (linkInfo.marker() != null && linkInfo.marker().getLiteral().equals("!")) {
            return LinkResult.wrapTextIn(new ReferenceImage(label, definition), scanner.position())
                    .includeMarker();
        }
        return LinkResult.wrapTextIn(new ReferenceLink(label, definition), scanner.position());
    }
}
