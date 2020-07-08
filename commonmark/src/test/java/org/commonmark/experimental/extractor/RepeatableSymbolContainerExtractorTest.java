package org.commonmark.experimental.extractor;

import org.commonmark.experimental.identifier.RepeatableSymbolContainerPattern;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public class RepeatableSymbolContainerExtractorTest {
    @Test
    public void shouldExtractJustTextSkippingTheFirstCharacter() {
        assertThat(RepeatableSymbolContainerExtractor
                        .from("~~some~~", RepeatableSymbolContainerPattern.of('~', 2)),
                is("some"));
    }

    @Test
    public void shouldReturnEmptyIfNotNullText() {
        assertThat(RepeatableSymbolContainerExtractor
                        .from(null, RepeatableSymbolContainerPattern.of('~', 2)),
                is(""));
    }

    @Test
    public void shouldReturnEmptyIfEmptyText() {
        assertThat(RepeatableSymbolContainerExtractor
                        .from("", RepeatableSymbolContainerPattern.of('~', 2)),
                is(""));
    }

    @Test
    public void shouldReturnEmptyIfNoContent() {
        assertThat(RepeatableSymbolContainerExtractor
                        .from("~~~~", RepeatableSymbolContainerPattern.of('~', 2)),
                is(""));
    }

    @Test
    public void shouldReturnEmptyIfLessThenMinSizeCharacter() {
        assertThat(RepeatableSymbolContainerExtractor
                        .from("~~~", RepeatableSymbolContainerPattern.of('~', 2)),
                is(""));
        assertThat(RepeatableSymbolContainerExtractor
                        .from("~~", RepeatableSymbolContainerPattern.of('~', 2)),
                is(""));
    }
}
