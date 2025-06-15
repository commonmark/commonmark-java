package org.commonmark.parser.beta;

import org.commonmark.node.SourceSpan;
import org.commonmark.parser.SourceLine;
import org.commonmark.parser.SourceLines;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class ScannerTest {

    @Test
    void testNext() {
        Scanner scanner = new Scanner(List.of(
                SourceLine.of("foo bar", null)),
                0, 4);
        assertThat(scanner.peek()).isEqualTo('b');
        scanner.next();
        assertThat(scanner.peek()).isEqualTo('a');
        scanner.next();
        assertThat(scanner.peek()).isEqualTo('r');
        scanner.next();
        assertThat(scanner.peek()).isEqualTo('\0');
    }

    @Test
    void testMultipleLines() {
        Scanner scanner = new Scanner(List.of(
                SourceLine.of("ab", null),
                SourceLine.of("cde", null)),
                0, 0);
        assertThat(scanner.hasNext()).isTrue();
        assertThat(scanner.peekPreviousCodePoint()).isEqualTo('\0');
        assertThat(scanner.peek()).isEqualTo('a');
        scanner.next();

        assertThat(scanner.hasNext()).isTrue();
        assertThat(scanner.peekPreviousCodePoint()).isEqualTo('a');
        assertThat(scanner.peek()).isEqualTo('b');
        scanner.next();

        assertThat(scanner.hasNext()).isTrue();
        assertThat(scanner.peekPreviousCodePoint()).isEqualTo('b');
        assertThat(scanner.peek()).isEqualTo('\n');
        scanner.next();

        assertThat(scanner.hasNext()).isTrue();
        assertThat(scanner.peekPreviousCodePoint()).isEqualTo('\n');
        assertThat(scanner.peek()).isEqualTo('c');
        scanner.next();

        assertThat(scanner.hasNext()).isTrue();
        assertThat(scanner.peekPreviousCodePoint()).isEqualTo('c');
        assertThat(scanner.peek()).isEqualTo('d');
        scanner.next();

        assertThat(scanner.hasNext()).isTrue();
        assertThat(scanner.peekPreviousCodePoint()).isEqualTo('d');
        assertThat(scanner.peek()).isEqualTo('e');
        scanner.next();

        assertThat(scanner.hasNext()).isFalse();
        assertThat(scanner.peekPreviousCodePoint()).isEqualTo('e');
        assertThat(scanner.peek()).isEqualTo('\0');
    }

    @Test
    void testCodePoints() {
        Scanner scanner = new Scanner(List.of(SourceLine.of("\uD83D\uDE0A", null)), 0, 0);

        assertThat(scanner.hasNext()).isTrue();
        assertThat(scanner.peekPreviousCodePoint()).isEqualTo('\0');
        assertThat(scanner.peekCodePoint()).isEqualTo(128522);
        scanner.next();
        // This jumps chars, not code points. So jump two here
        scanner.next();

        assertThat(scanner.hasNext()).isFalse();
        assertThat(scanner.peekPreviousCodePoint()).isEqualTo(128522);
        assertThat(scanner.peekCodePoint()).isEqualTo('\0');
    }

    @Test
    void testTextBetween() {
        Scanner scanner = new Scanner(List.of(
                SourceLine.of("ab", SourceSpan.of(10, 3, 13, 2)),
                SourceLine.of("cde", SourceSpan.of(11, 4, 20, 3))),
                0, 0);

        Position start = scanner.position();

        scanner.next();
        assertSourceLines(scanner.getSource(start, scanner.position()),
                "a",
                SourceSpan.of(10, 3, 13, 1));

        Position afterA = scanner.position();

        scanner.next();
        assertSourceLines(scanner.getSource(start, scanner.position()),
                "ab",
                SourceSpan.of(10, 3, 13, 2));

        Position afterB = scanner.position();

        scanner.next();
        assertSourceLines(scanner.getSource(start, scanner.position()),
                "ab\n",
                SourceSpan.of(10, 3, 13, 2));

        scanner.next();
        assertSourceLines(scanner.getSource(start, scanner.position()),
                "ab\nc",
                SourceSpan.of(10, 3, 13, 2),
                SourceSpan.of(11, 4, 20, 1));

        scanner.next();
        assertSourceLines(scanner.getSource(start, scanner.position()),
                "ab\ncd",
                SourceSpan.of(10, 3, 13, 2),
                SourceSpan.of(11, 4, 20, 2));

        scanner.next();
        assertSourceLines(scanner.getSource(start, scanner.position()),
                "ab\ncde",
                SourceSpan.of(10, 3, 13, 2),
                SourceSpan.of(11, 4, 20, 3));

        assertSourceLines(scanner.getSource(afterA, scanner.position()),
                "b\ncde",
                SourceSpan.of(10, 4, 14, 1),
                SourceSpan.of(11, 4, 20, 3));

        assertSourceLines(scanner.getSource(afterB, scanner.position()),
                "\ncde",
                SourceSpan.of(11, 4, 20, 3));
    }

    private void assertSourceLines(SourceLines sourceLines, String expectedContent, SourceSpan... expectedSourceSpans) {
        assertThat(sourceLines.getContent()).isEqualTo(expectedContent);
        assertThat(sourceLines.getSourceSpans()).isEqualTo(List.of(expectedSourceSpans));
    }

    @Test
    void nextString() {
        Scanner scanner = Scanner.of(SourceLines.of(List.of(
                SourceLine.of("hey ya", null),
                SourceLine.of("hi", null))));
        assertThat(scanner.next("hoy")).isFalse();
        assertThat(scanner.next("hey")).isTrue();
        assertThat(scanner.next(' ')).isTrue();
        assertThat(scanner.next("yo")).isFalse();
        assertThat(scanner.next("ya")).isTrue();
        assertThat(scanner.next(" ")).isFalse();
    }
}
