package org.commonmark.test;

import org.commonmark.node.SourceSpan;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

public class SourceSpanTest {

    @Test
    public void testSubSpan() {
        var span = SourceSpan.of(1, 2, 3, 5);

        assertThat(span.subSpan(0)).isSameAs(span);
        assertThat(span.subSpan(0, 5)).isSameAs(span);

        assertThat(span.subSpan(1)).isEqualTo(SourceSpan.of(1, 3, 4, 4));
        assertThat(span.subSpan(2)).isEqualTo(SourceSpan.of(1, 4, 5, 3));
        assertThat(span.subSpan(3)).isEqualTo(SourceSpan.of(1, 5, 6, 2));
        assertThat(span.subSpan(4)).isEqualTo(SourceSpan.of(1, 6, 7, 1));
        // Not sure if empty spans are useful, but it probably makes sense to mirror how substrings work
        assertThat(span.subSpan(5)).isEqualTo(SourceSpan.of(1, 7, 8, 0));
        assertThat("abcde".substring(5)).isEqualTo("");

        assertThat(span.subSpan(0, 5)).isEqualTo(SourceSpan.of(1, 2, 3, 5));
        assertThat(span.subSpan(0, 4)).isEqualTo(SourceSpan.of(1, 2, 3, 4));
        assertThat(span.subSpan(0, 3)).isEqualTo(SourceSpan.of(1, 2, 3, 3));
        assertThat(span.subSpan(0, 2)).isEqualTo(SourceSpan.of(1, 2, 3, 2));
        assertThat(span.subSpan(0, 1)).isEqualTo(SourceSpan.of(1, 2, 3, 1));
        assertThat(span.subSpan(0, 0)).isEqualTo(SourceSpan.of(1, 2, 3, 0));
        assertThat("abcde".substring(0, 1)).isEqualTo("a");
        assertThat("abcde".substring(0, 0)).isEqualTo("");

        assertThat(span.subSpan(1, 4)).isEqualTo(SourceSpan.of(1, 3, 4, 3));
        assertThat(span.subSpan(2, 3)).isEqualTo(SourceSpan.of(1, 4, 5, 1));
    }

    @Test
    public void testSubSpanBeginIndexNegative() {
        var sourceSpan = SourceSpan.of(1, 2, 3, 5);
        assertThatThrownBy(() -> sourceSpan.subSpan(-1)).isInstanceOf(IndexOutOfBoundsException.class);
    }

    @Test
    public void testSubSpanBeginIndexOutOfBounds() {
        var sourceSpan = SourceSpan.of(1, 2, 3, 5);
        assertThatThrownBy(() -> sourceSpan.subSpan(6)).isInstanceOf(IndexOutOfBoundsException.class);
    }

    @Test
    public void testSubSpanEndIndexNegative() {
        var sourceSpan = SourceSpan.of(1, 2, 3, 5);
        assertThatThrownBy(() -> sourceSpan.subSpan(0, -1)).isInstanceOf(IndexOutOfBoundsException.class);
    }

    @Test
    public void testSubSpanEndIndexOutOfBounds() {
        var sourceSpan = SourceSpan.of(1, 2, 3, 5);
        assertThatThrownBy(() -> sourceSpan.subSpan(0, 6)).isInstanceOf(IndexOutOfBoundsException.class);
    }

    @Test
    public void testSubSpanBeginIndexGreaterThanEndIndex() {
        var sourceSpan = SourceSpan.of(1, 2, 3, 5);
        assertThatThrownBy(() -> sourceSpan.subSpan(2, 1)).isInstanceOf(IndexOutOfBoundsException.class);
    }
}
