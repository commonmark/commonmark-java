package org.commonmark.experimental.identifier;

import org.commonmark.experimental.NodePatternIdentifier;
import org.commonmark.experimental.identifier.BracketContainerPattern.OpenClose;
import org.junit.Before;
import org.junit.Test;
import org.mockito.ArgumentCaptor;

import static org.commonmark.experimental.identifier.TextIdentifierBaseTest.readLine;
import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

public class BracketContainerIdentifierTest {
    private NodePatternIdentifier nodePatternIdentifier;
    private BracketContainerIdentifier textIdentifier;
    private ArgumentCaptor<NodePatternIdentifier.InternalBlocks[]> argumentCaptor;

    @Before
    public void setUp() {
        argumentCaptor = ArgumentCaptor.forClass(NodePatternIdentifier.InternalBlocks[].class);
        nodePatternIdentifier = mock(NodePatternIdentifier.class);
        textIdentifier = new BracketContainerIdentifier(
                BracketContainerPattern.of(OpenClose.of('[', ']')));
    }

    @Test
    public void shouldIdentifySimpleBracket() {
        readLine("[i]", textIdentifier, nodePatternIdentifier);

        verify(nodePatternIdentifier).found(eq(0), eq(3), argumentCaptor.capture());
        assertThat(argumentCaptor.getValue().length, is(1));
        assertThat(argumentCaptor.getValue()[0].getRelativeStartIndex(), is(0));
        assertThat(argumentCaptor.getValue()[0].getRelativeEndIndex(), is(3));
    }

    @Test
    public void shouldIdentifyInternalBlockPositionAsRelativeIndex() {
        readLine("some [i] end", textIdentifier, nodePatternIdentifier);

        verify(nodePatternIdentifier).found(eq(5), eq(8), argumentCaptor.capture());

        assertThat(argumentCaptor.getValue().length, is(1));
        assertThat(argumentCaptor.getValue()[0].getRelativeStartIndex(), is(0));
        assertThat(argumentCaptor.getValue()[0].getRelativeEndIndex(), is(3));
    }

    @Test
    public void shouldIdentifySimpleBracketWithEmptyContent() {
        readLine("[]", textIdentifier, nodePatternIdentifier);

        verify(nodePatternIdentifier).found(eq(0), eq(2), any(NodePatternIdentifier.InternalBlocks[].class));
    }

    @Test
    public void shouldNotIdentifyIfJustOpenBracket() {
        readLine("[i", textIdentifier, nodePatternIdentifier);

        verifyNoInteractions(nodePatternIdentifier);
    }

    @Test
    public void shouldNotIdentifyIfJustCloseBracket() {
        readLine("i]", textIdentifier, nodePatternIdentifier);

        verifyNoInteractions(nodePatternIdentifier);
    }

    @Test
    public void shouldIdentifyTwiceIfTwiceInTheLine() {
        readLine("[i][b]", textIdentifier, nodePatternIdentifier);

        verify(nodePatternIdentifier).found(eq(0), eq(3), argumentCaptor.capture());
        assertThat(argumentCaptor.getValue().length, is(1));
        assertThat(argumentCaptor.getValue()[0].getRelativeStartIndex(), is(0));
        assertThat(argumentCaptor.getValue()[0].getRelativeEndIndex(), is(3));

        verify(nodePatternIdentifier).found(eq(3), eq(6), argumentCaptor.capture());
        assertThat(argumentCaptor.getValue().length, is(1));
        assertThat(argumentCaptor.getValue()[0].getRelativeStartIndex(), is(0));
        assertThat(argumentCaptor.getValue()[0].getRelativeEndIndex(), is(3));
    }

    @Test
    public void shouldIdentifyTwiceIfTwiceInTheLineWithSpaces() {
        readLine("[i] [b]", textIdentifier, nodePatternIdentifier);

        verify(nodePatternIdentifier).found(eq(0), eq(3), any(NodePatternIdentifier.InternalBlocks[].class));
        verify(nodePatternIdentifier).found(eq(4), eq(7), any(NodePatternIdentifier.InternalBlocks[].class));
    }

    @Test
    public void shouldNotIdentifyIfNotBalancedBracket() {
        readLine("[i[]", textIdentifier, nodePatternIdentifier);
        readLine("[[i]", textIdentifier, nodePatternIdentifier);
        readLine("[[]", textIdentifier, nodePatternIdentifier);

        verifyNoInteractions(nodePatternIdentifier);
    }

    @Test
    public void shouldNotIdentifyStartInvertedWithEndSymbol() {
        readLine("][", textIdentifier, nodePatternIdentifier);

        verifyNoInteractions(nodePatternIdentifier);
    }

    @Test
    public void shouldIdentifyAfterFirstCloseBracket() {
        readLine("][i][", textIdentifier, nodePatternIdentifier);

        verify(nodePatternIdentifier).found(eq(1), eq(4), any(NodePatternIdentifier.InternalBlocks[].class));
        verify(nodePatternIdentifier, times(1)).found(anyInt(), anyInt(), any(NodePatternIdentifier.InternalBlocks[].class));
    }

    @Test
    public void shouldIdentifyDoubleBracketConfiguration() {
        bracketIdentifierDoubleSymbol();

        readLine("[i](b)", textIdentifier, nodePatternIdentifier);

        verify(nodePatternIdentifier).found(eq(0), eq(6), argumentCaptor.capture());
        assertThat(argumentCaptor.getValue().length, is(2));
        assertThat(argumentCaptor.getValue()[0].getRelativeStartIndex(), is(0));
        assertThat(argumentCaptor.getValue()[0].getRelativeEndIndex(), is(3));
        assertThat(argumentCaptor.getValue()[1].getRelativeStartIndex(), is(3));
        assertThat(argumentCaptor.getValue()[1].getRelativeEndIndex(), is(6));
    }

    @Test
    public void shouldIdentifyDoubleBracketConfigurationTwiceInLine() {
        bracketIdentifierDoubleSymbol();

        readLine("[i](b) [d](e)", textIdentifier, nodePatternIdentifier);

        verify(nodePatternIdentifier).found(eq(0), eq(6), any(NodePatternIdentifier.InternalBlocks[].class));
        verify(nodePatternIdentifier).found(eq(7), eq(13), any(NodePatternIdentifier.InternalBlocks[].class));
    }

    @Test
    public void shouldIdentifyBracketWithBalancedBracketsForBothGroupsTwiceInline() {
        bracketIdentifierDoubleSymbol();

        readLine("[i](b[]()) [e[]](()b)", textIdentifier, nodePatternIdentifier);

        verify(nodePatternIdentifier).found(eq(0), eq(10), any(NodePatternIdentifier.InternalBlocks[].class));
        verify(nodePatternIdentifier).found(eq(11), eq(21), any(NodePatternIdentifier.InternalBlocks[].class));
    }

    @Test
    public void shouldConsiderNotBalancedWithSymbolOverlapBetweenGroups() {
        bracketIdentifierDoubleSymbol();

        readLine("[i(](b) [e](b)", textIdentifier, nodePatternIdentifier);

        verify(nodePatternIdentifier).found(eq(0), eq(7), any(NodePatternIdentifier.InternalBlocks[].class));
        verify(nodePatternIdentifier).found(eq(8), eq(14), any(NodePatternIdentifier.InternalBlocks[].class));
    }

    @Test
    public void shouldIdentifyDoubleBracketConfigurationInFirstCloseIfFinalNotBalanced() {
        bracketIdentifierDoubleSymbol();

        readLine("[i](b))", textIdentifier, nodePatternIdentifier);

        verify(nodePatternIdentifier).found(eq(0), eq(6), any(NodePatternIdentifier.InternalBlocks[].class));
    }

    @Test
    public void shouldNotIdentifyIfNotBalancedSecondGroup() {
        bracketIdentifierDoubleSymbol();

        readLine("[i](b()", textIdentifier, nodePatternIdentifier);

        verifyNoInteractions(nodePatternIdentifier);
    }

    @Test
    public void shouldNotIdentifyIfInvertedGroupOrder() {
        bracketIdentifierDoubleSymbol();

        readLine("(b)[i]", textIdentifier, nodePatternIdentifier);

        verifyNoInteractions(nodePatternIdentifier);
    }

    @Test
    public void shouldNotIdentifyIfBetweenGroupsHasSomeExtraCharacter() {
        bracketIdentifierDoubleSymbol();

        readLine("[i] (b)", textIdentifier, nodePatternIdentifier);
        readLine("[i]|(b)", textIdentifier, nodePatternIdentifier);
        readLine("[i]$(b)", textIdentifier, nodePatternIdentifier);

        verifyNoInteractions(nodePatternIdentifier);
    }

    private void bracketIdentifierDoubleSymbol() {
        textIdentifier = new BracketContainerIdentifier(
                BracketContainerPattern.of(
                        OpenClose.of('[', ']'),
                        OpenClose.of('(', ')')));
    }

    @Test
    public void shouldIdentifyDoubleBracketConfigurationStartingByCharacter() {
        bracketIdentifierDoubleSymbolStartWith();

        readLine("![i](b)", textIdentifier, nodePatternIdentifier);

        verify(nodePatternIdentifier).found(eq(0), eq(7), any(NodePatternIdentifier.InternalBlocks[].class));
    }

    @Test
    public void shouldIdentifyDoubleBracketConfigurationByInternalGroups() {
        bracketIdentifierDoubleSymbolStartWith();

        readLine("![i](b)", textIdentifier, nodePatternIdentifier);

        verify(nodePatternIdentifier).found(eq(0), eq(7), argumentCaptor.capture());
        assertThat(argumentCaptor.getValue().length, is(2));
        assertThat(argumentCaptor.getValue()[0].getRelativeStartIndex(), is(0));
        assertThat(argumentCaptor.getValue()[0].getRelativeEndIndex(), is(4));
        assertThat(argumentCaptor.getValue()[1].getRelativeStartIndex(), is(4));
        assertThat(argumentCaptor.getValue()[1].getRelativeEndIndex(), is(7));
    }

    @Test
    public void shouldIdentifyDoubleBracketConfigurationWithInternalGroupsWithRelativePosition() {
        bracketIdentifierDoubleSymbolStartWith();

        readLine("some ![abcs](defg) foo", textIdentifier, nodePatternIdentifier);

        verify(nodePatternIdentifier).found(eq(5), eq(18), argumentCaptor.capture());
        assertThat(argumentCaptor.getValue().length, is(2));
        assertThat(argumentCaptor.getValue()[0].getRelativeStartIndex(), is(0));
        assertThat(argumentCaptor.getValue()[0].getRelativeEndIndex(), is(7));
        assertThat(argumentCaptor.getValue()[1].getRelativeStartIndex(), is(7));
        assertThat(argumentCaptor.getValue()[1].getRelativeEndIndex(), is(13));
    }

    @Test
    public void shouldIdentifyDoubleBracketConfigurationStartingByCharacterWithContentStartSymbol() {
        bracketIdentifierDoubleSymbolStartWith();

        readLine("![!i!](!b!)", textIdentifier, nodePatternIdentifier);

        verify(nodePatternIdentifier).found(eq(0), eq(11), any(NodePatternIdentifier.InternalBlocks[].class));
    }

    @Test
    public void shouldNotIdentifyDoubleBracketConfigurationStartingByCharacterWhenHasSpace() {
        bracketIdentifierDoubleSymbolStartWith();

        readLine("! [i](b)", textIdentifier, nodePatternIdentifier);

        verifyNoInteractions(nodePatternIdentifier);
    }

    @Test
    public void shouldIdentifyDoubleBracketConfigurationStartingByCharacterWithContentStartSymbolInSecondOccurrence() {
        bracketIdentifierDoubleSymbolStartWith();

        readLine("! [i](b) ![a](c)", textIdentifier, nodePatternIdentifier);

        verify(nodePatternIdentifier).found(eq(9), eq(16), any(NodePatternIdentifier.InternalBlocks[].class));
    }

    @Test
    public void shouldIdentifyDoubleBracketStartAfterSymbolStart() {
        bracketIdentifierDoubleSymbolStartWith();

        readLine("!![i](b)", textIdentifier, nodePatternIdentifier);

        verify(nodePatternIdentifier).found(eq(1), eq(8), any(NodePatternIdentifier.InternalBlocks[].class));
    }

    @Test
    public void shouldIdentifyDoubleBracketStartAfterSymbolAndSpaceStart() {
        bracketIdentifierDoubleSymbolStartWith();

        readLine("! ![i](b)", textIdentifier, nodePatternIdentifier);

        verify(nodePatternIdentifier).found(eq(2), eq(9), any(NodePatternIdentifier.InternalBlocks[].class));
    }

    private void bracketIdentifierDoubleSymbolStartWith() {
        textIdentifier = new BracketContainerIdentifier(
                BracketContainerPattern.of(
                        '!',
                        OpenClose.of('[', ']'),
                        OpenClose.of('(', ')')));
    }
}
