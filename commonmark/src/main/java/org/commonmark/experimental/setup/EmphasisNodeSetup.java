package org.commonmark.experimental.setup;

import org.commonmark.experimental.NodeCreator;
import org.commonmark.experimental.NodePatternIdentifier.InternalBlocks;
import org.commonmark.experimental.TextIdentifier;
import org.commonmark.experimental.TextNodeIdentifierSetup;
import org.commonmark.experimental.extractor.SingleSymbolContainerExtractor;
import org.commonmark.experimental.identifier.SingleSymbolContainerIdentifier;
import org.commonmark.experimental.identifier.SingleSymbolContainerPattern;
import org.commonmark.node.Emphasis;
import org.commonmark.node.Node;
import org.commonmark.node.Text;

public class EmphasisNodeSetup implements TextNodeIdentifierSetup {

    private static final SingleSymbolContainerPattern EMPHASIS_PATTERN = SingleSymbolContainerPattern.of('*');

    @Override
    public TextIdentifier textIdentifier() {
        return new SingleSymbolContainerIdentifier(EMPHASIS_PATTERN);
    }

    @Override
    public int priority() {
        return DefaultPriority.DEFAULT;
    }

    @Override
    public NodeCreator nodeCreator() {
        return new NodeCreator() {
            @Override
            public Node build(String textFound, InternalBlocks[] internalBlocks) {
                Emphasis emphasis = new Emphasis("*");
                emphasis.appendChild(new Text(SingleSymbolContainerExtractor.from(textFound)));
                return emphasis;
            }
        };
    }
}
