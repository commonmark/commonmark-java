package org.commonmark.experimental.extractor;

import org.commonmark.experimental.identifier.BracketContainerPattern;
import org.junit.Test;

import static org.commonmark.experimental.NodePatternIdentifier.InternalBlocks;
import static org.commonmark.experimental.identifier.BracketContainerPattern.OpenClose;
import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

public class BracketContainerExtractorTest {
    @Test
    public void shouldExtractJustTextSkippingTheContainers() {
        String[] content = BracketContainerExtractor.from("(some)",
                BracketContainerPattern.of(OpenClose.of('(', ')')),
                new InternalBlocks[]{new InternalBlocks(0, 6)});

        assertThat(content.length, is(1));
        assertThat(content[0], is("some"));
    }

    @Test
    public void shouldExtractJustTextSkippingTheContainersForMultipleBrackets() {
        String[] content = BracketContainerExtractor.from("[test()[]](some()[])",
                BracketContainerPattern.of(
                        OpenClose.of('[', ']'),
                        OpenClose.of('(', ')')),
                new InternalBlocks[]{
                        new InternalBlocks(0, 10),
                        new InternalBlocks(10, 20)
                });

        assertThat(content.length, is(2));
        assertThat(content[0], is("test()[]"));
        assertThat(content[1], is("some()[]"));
    }

    @Test
    public void shouldExtractJustTextSkippingTheContainersAndFirstSymbolForMultipleBrackets() {
        String[] content = BracketContainerExtractor.from("![test()[]](some()[])",
                BracketContainerPattern.of(
                        '!',
                        OpenClose.of('[', ']'),
                        OpenClose.of('(', ')')),
                new InternalBlocks[]{
                        new InternalBlocks(0, 11),
                        new InternalBlocks(11, 21)
                });

        assertThat(content.length, is(2));
        assertThat(content[0], is("test()[]"));
        assertThat(content[1], is("some()[]"));
    }

    @Test
    public void shouldReturnEmptyIfNotNullText() {
        String[] content = BracketContainerExtractor.from(null,
                BracketContainerPattern.of(OpenClose.of('[', ']')),
                new InternalBlocks[]{
                        new InternalBlocks(0, 6)
                });

        assertThat(content.length, is(0));
    }

    @Test
    public void shouldReturnEmptyIfNoContent() {
        String[] content = BracketContainerExtractor.from("()",
                BracketContainerPattern.of(OpenClose.of('(', ')')),
                new InternalBlocks[]{
                        new InternalBlocks(0, 2)
                });

        assertThat(content.length, is(1));
        assertThat(content[0], is(""));
    }
}
