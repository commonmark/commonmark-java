package org.commonmark.experimental.extractor;

import org.junit.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.hamcrest.MatcherAssert.assertThat;

public class SingleSymbolContainerExtractorTest {
    @Test
    public void shouldExtractJustTextSkippingTheFirstCharacter() {
        assertThat(SingleSymbolContainerExtractor.from("~some~"), is("some"));
    }

    @Test
    public void shouldReturnEmptyIfNotNullText() {
        assertThat(SingleSymbolContainerExtractor.from(null), is(""));
    }

    @Test
    public void shouldReturnEmptyIfNoContent() {
        assertThat(SingleSymbolContainerExtractor.from("~~"), is(""));
    }

    @Test
    public void shouldReturnEmptyIfOneCharacter() {
        assertThat(SingleSymbolContainerExtractor.from("~"), is(""));
    }

    @Test
    public void shouldReturnEmptyIfEmptyText() {
        assertThat(SingleSymbolContainerExtractor.from(""), is(""));
    }
}
