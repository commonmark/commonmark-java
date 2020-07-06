package org.commonmark.experimental;

import org.commonmark.experimental.NodePatternIdentifier.InternalBlocks;
import org.commonmark.experimental.identifier.RepeatableSymbolContainerIdentifier;
import org.commonmark.experimental.identifier.RepeatableSymbolContainerPattern;
import org.commonmark.experimental.identifier.SingleSymbolContainerIdentifier;
import org.commonmark.experimental.identifier.SingleSymbolContainerPattern;
import org.commonmark.experimental.identifier.StartSymbolIdentifier;
import org.commonmark.experimental.identifier.StartSymbolPattern;
import org.junit.Before;
import org.junit.Test;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

public class InlineParserImplMultipleNodesSetupTest {
    private NodeCreator nodeCreatorOne;
    private NodeCreator nodeCreatorTwo;

    private NodeSetup literalNodeSetup;

    @Before
    public void setUp() {
        nodeCreatorOne = mock(NodeCreator.class);
        nodeCreatorTwo = mock(NodeCreator.class);

        literalNodeSetup = mock(NodeSetup.class);
        when(literalNodeSetup.nodeCreator()).thenReturn(mock(NodeCreator.class));
    }

    @Test
    public void shouldConsiderTwoNodesSetup() {
        setupParser(new SingleSymbolContainerIdentifier(new SingleSymbolContainerPattern('*')),
                new StartSymbolIdentifier(new StartSymbolPattern('@')))
                .readLine("*image.gif* @second");

        verify(nodeCreatorOne).build(eq("*image.gif*"), eq((InternalBlocks[]) null));
        verify(nodeCreatorTwo).build(eq("@second"), eq((InternalBlocks[]) null));
    }

    @Test
    public void shouldConsiderTwoNodeSetupWithSameStartSymbolAndSamePriorityTheFirstToAdded() {
        setupParser(0, new SingleSymbolContainerIdentifier(new SingleSymbolContainerPattern('*')),
                0, new SingleSymbolContainerIdentifier(new SingleSymbolContainerPattern('*')))
                .readLine("*image.gif*");

        verify(nodeCreatorOne).build(eq("*image.gif*"), eq((InternalBlocks[]) null));
        verifyNoInteractions(nodeCreatorTwo);
    }

    @Test
    public void shouldConsiderTwoNodeSetupWithSameStartSymbolWinningTheHighPriority() {
        setupParser(0, new SingleSymbolContainerIdentifier(new SingleSymbolContainerPattern('~')),
                10, new RepeatableSymbolContainerIdentifier(new RepeatableSymbolContainerPattern('~', 2)))
                .readLine("~image.gif~ ~~second.jpg~~");

        verify(nodeCreatorOne).build(eq("~image.gif~"), eq((InternalBlocks[]) null));
        verify(nodeCreatorTwo).build(eq("~~second.jpg~~"), eq((InternalBlocks[]) null));
    }

    @Test
    public void shouldConsiderTheNodeSetupByPriorityForOverlapConflict() {
        setupParser(1, new SingleSymbolContainerIdentifier(new SingleSymbolContainerPattern('`')),
                0, new SingleSymbolContainerIdentifier(new SingleSymbolContainerPattern('~')))
                .readLine("`the ~image.gif~ here`");

        verify(nodeCreatorOne).build(eq("`the ~image.gif~ here`"), eq((InternalBlocks[]) null));
        verifyNoInteractions(nodeCreatorTwo);
    }

    @Test
    public void shouldConsiderTheNodeSetupByPriorityForLineConflictForInternalComponent() {
        setupParser(0, new SingleSymbolContainerIdentifier(new SingleSymbolContainerPattern('`')),
                1, new SingleSymbolContainerIdentifier(new SingleSymbolContainerPattern('~')))
                .readLine("`the ~image.gif~ here`");

        verifyNoInteractions(nodeCreatorOne);
        verify(nodeCreatorTwo).build(eq("~image.gif~"), eq((InternalBlocks[]) null));
    }

    @Test
    public void shouldConsiderTheNodeSetupByPriorityForLineConflictForOverlapByLeftSide() {
        setupParser(1, new SingleSymbolContainerIdentifier(new SingleSymbolContainerPattern('`')),
                0, new SingleSymbolContainerIdentifier(new SingleSymbolContainerPattern('~')))
                .readLine("`the ~image.gif here` another~");

        verify(nodeCreatorOne).build(eq("`the ~image.gif here`"), eq((InternalBlocks[]) null));
        verifyNoInteractions(nodeCreatorTwo);
    }

    @Test
    public void shouldConsiderTheNodeSetupByPriorityForLineConflictMultipleOccurrences() {
        setupParser(1, new SingleSymbolContainerIdentifier(new SingleSymbolContainerPattern('`')),
                0, new SingleSymbolContainerIdentifier(new SingleSymbolContainerPattern('~')))
                .readLine("~the `image.gif` here `second.jpg` another~");

        verify(nodeCreatorOne).build(eq("`image.gif`"), eq((InternalBlocks[]) null));
        verify(nodeCreatorOne).build(eq("`second.jpg`"), eq((InternalBlocks[]) null));
        verifyNoInteractions(nodeCreatorTwo);
    }

    private InlineParserImpl setupParser(final int priorityOne, final TextIdentifier textIdentifierOne,
                                         final int priorityTwo, final TextIdentifier textIdentifierTwo) {
        TextNodeIdentifierSetup nodeSetupOne = textNodeIdentifierSetup(textIdentifierOne, priorityOne, nodeCreatorOne);
        TextNodeIdentifierSetup nodeSetupTwo = textNodeIdentifierSetup(textIdentifierTwo, priorityTwo, nodeCreatorTwo);

        return InlineParserImpl.builder()
                .literalNodeSetup(literalNodeSetup)
                .nodeSetup(nodeSetupOne, nodeSetupTwo)
                .build();
    }

    private InlineParserImpl setupParser(final TextIdentifier textIdentifierOne,
                                         final TextIdentifier textIdentifierTwo) {
        return setupParser(0, textIdentifierOne, 0, textIdentifierTwo);
    }

    private TextNodeIdentifierSetup textNodeIdentifierSetup(final TextIdentifier textIdentifierOne,
                                                            final int priority,
                                                            final NodeCreator nodeCreatorOne) {
        return new TextNodeIdentifierSetup() {
            @Override
            public TextIdentifier textIdentifier() {
                return textIdentifierOne;
            }

            @Override
            public int priority() {
                return priority;
            }

            @Override
            public NodeCreator nodeCreator() {
                return nodeCreatorOne;
            }
        };
    }
}
