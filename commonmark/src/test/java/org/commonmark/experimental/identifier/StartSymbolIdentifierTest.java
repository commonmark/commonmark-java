package org.commonmark.experimental.identifier;


import org.commonmark.experimental.NodePatternIdentifier;
import org.junit.Before;
import org.junit.Test;

import static org.commonmark.experimental.identifier.TextIdentifierBaseTest.readLine;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

public class StartSymbolIdentifierTest {
    private NodePatternIdentifier nodePatternIdentifier;
    private StartSymbolIdentifier textIdentifier;

    @Before
    public void setUp() {
        nodePatternIdentifier = mock(NodePatternIdentifier.class);
        textIdentifier = new StartSymbolIdentifier(new StartSymbolPattern('@', '/'));
    }

    @Test
    public void shouldIdentifyStartCharacterUntilSpace() {
        readLine("some @some text", textIdentifier, nodePatternIdentifier);

        verify(nodePatternIdentifier).found(5, 10, null);
    }

    @Test
    public void shouldIdentifyStartCharacterWithSlashInTheText() {
        readLine("some @github/support text", textIdentifier, nodePatternIdentifier);

        verify(nodePatternIdentifier).found(5, 20, null);
    }

    @Test
    public void shouldIdentifyStartCharacterUntilSpaceBySpecialCharacter() {
        textIdentifier = new StartSymbolIdentifier(new StartSymbolPattern('&'));

        readLine("some &123 text", textIdentifier, nodePatternIdentifier);

        verify(nodePatternIdentifier).found(5, 9, null);
    }

    @Test
    public void shouldIdentifyStartCharacterUntilSignal() {
        readLine("some @some. Text after", textIdentifier, nodePatternIdentifier);

        verify(nodePatternIdentifier).found(5, 10, null);
    }

    @Test
    public void shouldIdentifyStartCharacterMultipleTimes() {
        readLine("some @some and @any text", textIdentifier, nodePatternIdentifier);

        verify(nodePatternIdentifier).found(5, 10, null);
        verify(nodePatternIdentifier).found(15, 19, null);
    }

    @Test
    public void shouldIdentifyStartSymbolWhenStartingInTheLine() {
        readLine("@some text", textIdentifier, nodePatternIdentifier);

        verify(nodePatternIdentifier).found(0, 5, null);
    }

    @Test
    public void shouldIdentifyStartCharacterWhenEndingTheLine() {
        readLine("text @some", textIdentifier, nodePatternIdentifier);

        verify(nodePatternIdentifier).found(5, 10, null);
    }

    @Test
    public void shouldNotIdentifyStartSymbolWhenCharacterBetweenWords() {
        readLine("some@image.gifthing some", textIdentifier, nodePatternIdentifier);

        verifyNoInteractions(nodePatternIdentifier);
    }
}
