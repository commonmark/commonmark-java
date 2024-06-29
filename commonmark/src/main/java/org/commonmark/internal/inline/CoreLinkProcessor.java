package org.commonmark.internal.inline;

import org.commonmark.node.Image;
import org.commonmark.node.Link;
import org.commonmark.node.LinkReferenceDefinition;
import org.commonmark.node.Node;
import org.commonmark.parser.InlineParserContext;
import org.commonmark.parser.beta.LinkInfo;
import org.commonmark.parser.beta.LinkProcessor;
import org.commonmark.parser.beta.LinkResult;
import org.commonmark.parser.beta.Scanner;

public class CoreLinkProcessor implements LinkProcessor {

    @Override
    public LinkResult process(LinkInfo linkInfo, Scanner scanner, InlineParserContext context) {
        if (linkInfo.destination() != null) {
            // Inline link
            var node = createNode(linkInfo, linkInfo.destination(), linkInfo.title());
            return LinkResult.wrapTextIn(node, scanner.position());
        }

        var label = linkInfo.label();
        var ref = label != null && !label.isEmpty() ? label : linkInfo.text();
        var def = context.getDefinition(LinkReferenceDefinition.class, ref);
        if (def != null) {
            // Reference link
            var node = createNode(linkInfo, def.getDestination(), def.getTitle());
            return LinkResult.wrapTextIn(node, scanner.position());
        }
        return LinkResult.none();
    }

    private static Node createNode(LinkInfo linkInfo, String destination, String title) {
        return linkInfo.openerType() == LinkInfo.OpenerType.IMAGE ?
                new Image(destination, title) :
                new Link(destination, title);
    }
}
