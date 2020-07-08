package org.commonmark.experimental.setup;

import org.commonmark.experimental.NodeCreator;
import org.commonmark.experimental.NodePatternIdentifier.InternalBlocks;
import org.commonmark.experimental.TextIdentifier;
import org.commonmark.experimental.TextNodeIdentifierSetup;
import org.commonmark.experimental.extractor.RepeatableSymbolContainerExtractor;
import org.commonmark.experimental.identifier.RepeatableSymbolContainerIdentifier;
import org.commonmark.experimental.identifier.RepeatableSymbolContainerPattern;
import org.commonmark.node.Node;
import org.commonmark.node.StrongEmphasis;
import org.commonmark.node.Text;

public class StrongEmphasisNodeSetup implements TextNodeIdentifierSetup {

    private static final RepeatableSymbolContainerPattern STRONG_EMPHASIS_PATTERN =
            RepeatableSymbolContainerPattern.of('*', 2);

    @Override
    public TextIdentifier textIdentifier() {
        return new RepeatableSymbolContainerIdentifier(STRONG_EMPHASIS_PATTERN);
    }

    @Override
    public int priority() {
        return DefaultPriority.REPEATABLE_SYMBOL;
    }

    @Override
    public NodeCreator nodeCreator() {
        return new NodeCreator() {
            @Override
            public Node build(String textFound, InternalBlocks[] internalBlocks) {
                StrongEmphasis emphasis = new StrongEmphasis(STRONG_EMPHASIS_PATTERN.literalSymbolContainer());
                emphasis.appendChild(
                        new Text(RepeatableSymbolContainerExtractor.from(textFound, STRONG_EMPHASIS_PATTERN)));
                return emphasis;
            }
        };
    }
}
