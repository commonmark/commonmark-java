package org.commonmark.experimental.identifier;


import org.commonmark.experimental.NodePatternIdentifier;
import org.junit.Before;
import org.junit.Test;

import static org.commonmark.experimental.identifier.TextIdentifierBaseTest.readLine;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

public class SingleSymbolContainerIdentifierTest {
    private NodePatternIdentifier nodePatternIdentifier;
    private SingleSymbolContainerIdentifier textIdentifier;

    @Before
    public void setUp() {
        nodePatternIdentifier = mock(NodePatternIdentifier.class);
        textIdentifier = new SingleSymbolContainerIdentifier(new SingleSymbolContainerPattern('~'));
    }

    @Test
    public void shouldIdentifySingleCharacter() {
        readLine("some ~image.gif~ text", textIdentifier, nodePatternIdentifier);

        verify(nodePatternIdentifier).found(5, 16, null);
    }

    @Test
    public void shouldNotIdentifySingleCharacterInTextWithoutOpenSymbol() {
        readLine("some image.gif~ text", textIdentifier, nodePatternIdentifier);

        verifyNoInteractions(nodePatternIdentifier);
    }

    @Test
    public void shouldNotIdentifySingleCharacterInTextWithoutClosedCharacter() {
        readLine("some ~image.gif text", textIdentifier, nodePatternIdentifier);

        verifyNoInteractions(nodePatternIdentifier);
    }

    @Test
    public void shouldNotIdentifySingleCharacterInTextWithoutAnyCharacter() {
        readLine("some image.gif text", textIdentifier, nodePatternIdentifier);

        verifyNoInteractions(nodePatternIdentifier);
    }

    @Test
    public void shouldIdentifySingleCharacterBetweenSymbols() {
        readLine("~i~", textIdentifier, nodePatternIdentifier);

        verify(nodePatternIdentifier).found(0, 3, null);
    }

    @Test
    public void shouldNotIdentifySingleCharacterIfEmptyTextBetweenCharacters() {
        readLine("~~", textIdentifier, nodePatternIdentifier);

        verifyNoInteractions(nodePatternIdentifier);
    }

    @Test
    public void shouldIdentifySingleCharacterIfEmptyTextBetweenCharactersWhenMinSizeZero() {
        textIdentifier = new SingleSymbolContainerIdentifier(
                new SingleSymbolContainerPattern('~', 0));
        readLine("~~", textIdentifier, nodePatternIdentifier);

        verify(nodePatternIdentifier).found(0, 2, null);
    }

    @Test
    public void shouldIdentifySingleCharacterMultipleTimes() {
        readLine("some ~image.gif~ and ~second.jpg~ text", textIdentifier, nodePatternIdentifier);

        verify(nodePatternIdentifier).found(5, 16, null);
        verify(nodePatternIdentifier).found(21, 33, null);
    }

    @Test
    public void shouldIdentifySingleCharacterWhenStartingInTheLine() {
        readLine("~image.gif~ text", textIdentifier, nodePatternIdentifier);

        verify(nodePatternIdentifier).found(0, 11, null);
    }

    @Test
    public void shouldIdentifySingleCharacterWhenEndingTheLine() {
        readLine("text ~image.gif~", textIdentifier, nodePatternIdentifier);

        verify(nodePatternIdentifier).found(5, 16, null);
    }

    @Test
    public void shouldIdentifySingleCharacterBetweenWords() {
        readLine("some~image.gif~thing", textIdentifier, nodePatternIdentifier);

        verify(nodePatternIdentifier).found(4, 15, null);
    }
}
