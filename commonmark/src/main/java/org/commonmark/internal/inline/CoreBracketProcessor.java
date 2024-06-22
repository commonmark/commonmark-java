package org.commonmark.internal.inline;

import org.commonmark.node.Image;
import org.commonmark.node.Link;
import org.commonmark.node.LinkReferenceDefinition;
import org.commonmark.node.Node;
import org.commonmark.parser.InlineParserContext;
import org.commonmark.parser.beta.BracketInfo;
import org.commonmark.parser.beta.BracketProcessor;
import org.commonmark.parser.beta.BracketResult;
import org.commonmark.parser.beta.Scanner;

public class CoreBracketProcessor implements BracketProcessor {

    @Override
    public BracketResult process(BracketInfo bracketInfo, Scanner scanner, InlineParserContext context) {
        if (bracketInfo.destination() != null) {
            // Inline link
            var node = createNode(bracketInfo, bracketInfo.destination(), bracketInfo.title());
            return BracketResult.wrapTextIn(node, scanner.position());
        }

        var label = bracketInfo.label();
        var ref = label != null && !label.isEmpty() ? label : bracketInfo.text();
        var def = context.getDefinition(LinkReferenceDefinition.class, ref);
        if (def != null) {
            // Reference link
            var node = createNode(bracketInfo, def.getDestination(), def.getTitle());
            return BracketResult.wrapTextIn(node, scanner.position());
        }
        return BracketResult.none();
    }

    private static Node createNode(BracketInfo bracketInfo, String destination, String title) {
        return bracketInfo.openerType() == BracketInfo.OpenerType.IMAGE ?
                new Image(destination, title) :
                new Link(destination, title);
    }
}
