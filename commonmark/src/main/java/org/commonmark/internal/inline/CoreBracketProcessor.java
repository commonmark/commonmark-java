package org.commonmark.internal.inline;

import org.commonmark.node.Image;
import org.commonmark.node.Link;
import org.commonmark.parser.InlineParserContext;
import org.commonmark.parser.beta.BracketInfo;
import org.commonmark.parser.beta.BracketProcessor;
import org.commonmark.parser.beta.BracketResult;
import org.commonmark.parser.beta.Scanner;

public class CoreBracketProcessor implements BracketProcessor {

    @Override
    public BracketResult process(BracketInfo bracketInfo, Scanner scanner, InlineParserContext context) {
        var label = bracketInfo.label();
        var ref = label != null && !label.isEmpty() ? label : bracketInfo.text();
        var def = context.getLinkReferenceDefinition(ref);
        if (def != null) {
            if (bracketInfo.openerType() == BracketInfo.OpenerType.IMAGE) {
                return BracketResult.wrapTextIn(new Image(def.getDestination(), def.getTitle()), scanner.position());
            } else if (bracketInfo.openerType() == BracketInfo.OpenerType.LINK) {
                return BracketResult.wrapTextIn(new Link(def.getDestination(), def.getTitle()), scanner.position());
            }
        }
        return BracketResult.none();
    }
}
