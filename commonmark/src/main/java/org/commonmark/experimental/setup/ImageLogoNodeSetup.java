package org.commonmark.experimental.setup;

import org.commonmark.experimental.NodeCreator;
import org.commonmark.experimental.NodePatternIdentifier;
import org.commonmark.experimental.TextIdentifier;
import org.commonmark.experimental.TextNodeIdentifierSetup;
import org.commonmark.experimental.identifier.BracketContainerIdentifier;
import org.commonmark.experimental.identifier.BracketContainerPattern;
import org.commonmark.experimental.identifier.BracketContainerPattern.OpenClose;
import org.commonmark.node.Image;
import org.commonmark.node.Node;

public class ImageLogoNodeSetup implements TextNodeIdentifierSetup {
    @Override
    public TextIdentifier textIdentifier() {
        return new BracketContainerIdentifier(
                BracketContainerPattern.of(
                        '!',
                        OpenClose.of('[', ']'),
                        OpenClose.of('(', ')')));
    }

    @Override
    public int priority() {
        return 0;
    }

    @Override
    public NodeCreator nodeCreator() {
        return new NodeCreator() {
            @Override
            public Node build(String found, NodePatternIdentifier.InternalBlocks[] internalBlocks) {
                String title = found.substring(
                        internalBlocks[0].getRelativeStartIndex() + 2,
                        internalBlocks[0].getRelativeEndIndex() - 1);
                String destination = found.substring(
                        internalBlocks[1].getRelativeStartIndex() + 1,
                        internalBlocks[1].getRelativeEndIndex() - 1);
                return new Image(destination, title);
            }
        };
    }
}
