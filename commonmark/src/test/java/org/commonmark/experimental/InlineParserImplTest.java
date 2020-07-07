package org.commonmark.experimental;

import org.commonmark.experimental.identifier.SingleSymbolContainerIdentifier;
import org.commonmark.experimental.identifier.SingleSymbolContainerPattern;
import org.commonmark.experimental.identifier.StartSymbolIdentifier;
import org.commonmark.experimental.identifier.StartSymbolPattern;
import org.commonmark.node.Node;
import org.junit.Before;
import org.junit.Test;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class InlineParserImplTest {
    private NodeCreator nodeCreator;
    private NodeCreator literalNodeCreator;
    private NodeSetup literalNodeSetup;
    private Node nodeParent;

    @Before
    public void setUp() {
        nodeParent = mock(Node.class);
        nodeCreator = mock(NodeCreator.class);
        literalNodeCreator = mock(NodeCreator.class);
        literalNodeSetup = mock(NodeSetup.class);
        when(literalNodeSetup.nodeCreator()).thenReturn(literalNodeCreator);
    }

    @Test
    public void shouldParseTextByNodeSetup() {
        setupParser(new SingleSymbolContainerIdentifier(new SingleSymbolContainerPattern('~')))
                .parse("some ~image.gif~ text", nodeParent);

        verify(nodeCreator).build(eq("~image.gif~"), eq((NodePatternIdentifier.InternalBlocks[]) null));
    }

    @Test
    public void shouldNoticeNodesMultipleTimes() {
        setupParser(new StartSymbolIdentifier(new StartSymbolPattern('/', '.')))
                .parse("/image.gif /something.jpg", nodeParent);

        verify(nodeCreator).build(eq("/image.gif"), eq((NodePatternIdentifier.InternalBlocks[]) null));
        verify(nodeCreator).build(eq("/something.jpg"), eq((NodePatternIdentifier.InternalBlocks[]) null));
    }

    @Test
    public void shouldConsiderStartSymbolBetweenWords() {
        setupParser(new SingleSymbolContainerIdentifier(new SingleSymbolContainerPattern('~')))
                .parse("~image.gif~ something~second.jpg~", nodeParent);

        verify(nodeCreator).build(eq("~image.gif~"), eq((NodePatternIdentifier.InternalBlocks[]) null));
        verify(nodeCreator).build(eq("~second.jpg~"), eq((NodePatternIdentifier.InternalBlocks[]) null));
    }

    @Test
    public void shouldResetConfigurationWhenReadLineASecondTime() {
        InlineParserImpl inlineParser = setupParser(new SingleSymbolContainerIdentifier(new SingleSymbolContainerPattern('*')));
        inlineParser.parse("some *image text", nodeParent);
        inlineParser.parse("some *second.gif* text", nodeParent);

        verify(nodeCreator).build(eq("*second.gif*"), eq((NodePatternIdentifier.InternalBlocks[]) null));
    }

    @Test
    public void shouldTakeLiteralBeforeNodeFound() {
        setupParser(new StartSymbolIdentifier(new StartSymbolPattern('~')))
                .parse("some ~node", nodeParent);

        verify(literalNodeCreator).build(eq("some "), eq((NodePatternIdentifier.InternalBlocks[]) null));
    }

    @Test
    public void shouldTakeLiteralAfterNodeFound() {
        setupParser(new StartSymbolIdentifier(new StartSymbolPattern('~')))
                .parse("~node after", nodeParent);

        verify(literalNodeCreator).build(eq(" after"), eq((NodePatternIdentifier.InternalBlocks[]) null));
    }

    @Test
    public void shouldTakeLiteralBetweenNodes() {
        setupParser(new StartSymbolIdentifier(new StartSymbolPattern('~')))
                .parse("~nodeone between ~nodetwo", nodeParent);

        verify(literalNodeCreator).build(eq(" between "), eq((NodePatternIdentifier.InternalBlocks[]) null));
    }

    @Test
    public void shouldTakeLiteralBetweenNodesInTheEndOfTheLine() {
        setupParser(new StartSymbolIdentifier(new StartSymbolPattern('~')))
                .parse(" ~nodeone between ~nodetwo after", nodeParent);

        verify(literalNodeCreator).build(eq(" "), eq((NodePatternIdentifier.InternalBlocks[]) null));
        verify(literalNodeCreator).build(eq(" between "), eq((NodePatternIdentifier.InternalBlocks[]) null));
        verify(literalNodeCreator).build(eq(" after"), eq((NodePatternIdentifier.InternalBlocks[]) null));
    }

    private InlineParserImpl setupParser(final TextIdentifier textIdentifier) {
        TextNodeIdentifierSetup nodeSetup = new TextNodeIdentifierSetup() {
            @Override
            public TextIdentifier textIdentifier() {
                return textIdentifier;
            }

            @Override
            public int priority() {
                return 0;
            }

            @Override
            public NodeCreator nodeCreator() {
                return nodeCreator;
            }
        };

        return InlineParserImpl.builder()
                .literalNodeSetup(literalNodeSetup)
                .nodeSetup(nodeSetup)
                .build();
    }
}
