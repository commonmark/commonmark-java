package org.commonmark.experimental.setup;

import org.commonmark.experimental.NodeCreator;
import org.commonmark.experimental.NodePatternIdentifier.InternalBlocks;
import org.commonmark.experimental.TextIdentifier;
import org.commonmark.experimental.TextNodeIdentifierSetup;
import org.commonmark.experimental.extractor.BracketContainerExtractor;
import org.commonmark.experimental.identifier.BracketContainerIdentifier;
import org.commonmark.experimental.identifier.BracketContainerPattern;
import org.commonmark.experimental.identifier.BracketContainerPattern.OpenClose;
import org.commonmark.node.Link;
import org.commonmark.node.Node;

public class LinkNodeSetup implements TextNodeIdentifierSetup {

    private static final BracketContainerPattern LINK_PATTERN = BracketContainerPattern.of(
            OpenClose.of('[', ']'),
            OpenClose.of('(', ')'));

    @Override
    public TextIdentifier textIdentifier() {
        return new BracketContainerIdentifier(LINK_PATTERN);
    }

    @Override
    public int priority() {
        return DefaultPriority.BRACKET_SYMBOL;
    }

    @Override
    public NodeCreator nodeCreator() {
        return new NodeCreator() {
            @Override
            public Node build(String textFound, InternalBlocks[] internalBlocks) {
                String[] content = BracketContainerExtractor.from(textFound, LINK_PATTERN, internalBlocks);
                return new Link(content[1], content[0]);
            }
        };
    }
}
