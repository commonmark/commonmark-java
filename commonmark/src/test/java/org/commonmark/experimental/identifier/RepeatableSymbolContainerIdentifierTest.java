package org.commonmark.experimental.identifier;

import org.commonmark.experimental.NodePatternIdentifier;
import org.junit.Before;
import org.junit.Test;

import static org.commonmark.experimental.identifier.TextIdentifierBaseTest.readLine;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;

public class RepeatableSymbolContainerIdentifierTest {
    private NodePatternIdentifier nodePatternIdentifier;
    private RepeatableSymbolContainerIdentifier textIdentifier;

    @Before
    public void setUp() {
        nodePatternIdentifier = mock(NodePatternIdentifier.class);
        textIdentifier = new RepeatableSymbolContainerIdentifier(
                new RepeatableSymbolContainerPattern('~', 2));
    }

    @Test
    public void shouldIdentifyMultipleCharacter() {
        readLine("some ~~image.gif~~ text", textIdentifier, nodePatternIdentifier);

        verify(nodePatternIdentifier).found(5, 18, null);
    }

    @Test
    public void shouldIdentifyMultipleCharacterWheContentSizeIsOne() {
        readLine("~~i~~", textIdentifier, nodePatternIdentifier);

        verify(nodePatternIdentifier).found(0, 5, null);
    }

    @Test
    public void shouldNotIdentifyMultipleCharacterIfContainerSymbolsIsSingle() {
        readLine("some ~image.gif~ text", textIdentifier, nodePatternIdentifier);

        verifyNoInteractions(nodePatternIdentifier);
    }

    @Test
    public void shouldNotIdentifyMultipleCharacterIfNotInSequenceFromTheBegin() {
        readLine("some ~ ~image.gif~~ text", textIdentifier, nodePatternIdentifier);

        verifyNoInteractions(nodePatternIdentifier);
    }

    @Test
    public void shouldNotIdentifyMultipleCharacterIfNotInSequenceInTheEnd() {
        readLine("some ~~image.gif~ ~ text", textIdentifier, nodePatternIdentifier);

        verifyNoInteractions(nodePatternIdentifier);
    }

    @Test
    public void shouldNotIdentifyIfEmptyTextBetweenCharacters() {
        readLine("~~~~", textIdentifier, nodePatternIdentifier);

        verifyNoInteractions(nodePatternIdentifier);
    }

    @Test
    public void shouldIdentifyMultipleCharacterMultipleTimes() {
        readLine("some ~~image.gif~~ and ~~second.jpg~~ text", textIdentifier, nodePatternIdentifier);

        verify(nodePatternIdentifier).found(5, 18, null);
        verify(nodePatternIdentifier).found(23, 37, null);
    }

    @Test
    public void shouldIdentifyMultipleCharacterWhenStartingInTheLineBegin() {
        readLine("~~image.gif~~ text", textIdentifier, nodePatternIdentifier);

        verify(nodePatternIdentifier).found(0, 13, null);
    }

    @Test
    public void shouldIdentifyMultipleCharacterWhenEndingTheLine() {
        readLine("text ~~image.gif~~", textIdentifier, nodePatternIdentifier);

        verify(nodePatternIdentifier).found(5, 18, null);
    }

    @Test
    public void shouldIdentifyMultipleCharacterBetweenWords() {
        readLine("some~~image.gif~~thing", textIdentifier, nodePatternIdentifier);

        verify(nodePatternIdentifier).found(4, 17, null);
    }

    @Test
    public void supportMultipleSymbolsAsSetup() {
        textIdentifier = new RepeatableSymbolContainerIdentifier(
                new RepeatableSymbolContainerPattern('*', 4));

        readLine("some ****image.gif**** thing **foo**", textIdentifier, nodePatternIdentifier);

        verify(nodePatternIdentifier).found(5, 22, null);
        verify(nodePatternIdentifier, times(1))
                .found(anyInt(), anyInt(), eq((NodePatternIdentifier.InternalBlocks[])null));
    }
}
