package org.commonmark.parser.beta;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import java.util.List;
import org.commonmark.node.SourceSpan;
import org.commonmark.parser.SourceLine;
import org.commonmark.parser.SourceLines;
import org.commonmark.text.CharMatcher;
import org.junit.jupiter.api.Test;

class ScannerTest {

    @Test
    void testNext() {
        Scanner scanner = new Scanner(List.of(SourceLine.of("foo bar", null)), 0, 4);
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
        Scanner scanner =
                new Scanner(List.of(SourceLine.of("ab", null), SourceLine.of("cde", null)), 0, 0);
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
        Scanner scanner =
                new Scanner(
                        List.of(
                                SourceLine.of("ab", SourceSpan.of(10, 3, 13, 2)),
                                SourceLine.of("cde", SourceSpan.of(11, 4, 20, 3))),
                        0,
                        0);

        Position start = scanner.position();

        scanner.next();
        assertSourceLines(
                scanner.getSource(start, scanner.position()), "a", SourceSpan.of(10, 3, 13, 1));

        Position afterA = scanner.position();

        scanner.next();
        assertSourceLines(
                scanner.getSource(start, scanner.position()), "ab", SourceSpan.of(10, 3, 13, 2));

        Position afterB = scanner.position();

        scanner.next();
        assertSourceLines(
                scanner.getSource(start, scanner.position()), "ab\n", SourceSpan.of(10, 3, 13, 2));

        scanner.next();
        assertSourceLines(
                scanner.getSource(start, scanner.position()),
                "ab\nc",
                SourceSpan.of(10, 3, 13, 2),
                SourceSpan.of(11, 4, 20, 1));

        scanner.next();
        assertSourceLines(
                scanner.getSource(start, scanner.position()),
                "ab\ncd",
                SourceSpan.of(10, 3, 13, 2),
                SourceSpan.of(11, 4, 20, 2));

        scanner.next();
        assertSourceLines(
                scanner.getSource(start, scanner.position()),
                "ab\ncde",
                SourceSpan.of(10, 3, 13, 2),
                SourceSpan.of(11, 4, 20, 3));

        assertSourceLines(
                scanner.getSource(afterA, scanner.position()),
                "b\ncde",
                SourceSpan.of(10, 4, 14, 1),
                SourceSpan.of(11, 4, 20, 3));

        assertSourceLines(
                scanner.getSource(afterB, scanner.position()),
                "\ncde",
                SourceSpan.of(11, 4, 20, 3));
    }

    private void assertSourceLines(
            SourceLines sourceLines, String expectedContent, SourceSpan... expectedSourceSpans) {
        assertThat(sourceLines.getContent()).isEqualTo(expectedContent);
        assertThat(sourceLines.getSourceSpans()).isEqualTo(List.of(expectedSourceSpans));
    }

    @Test
    void testPrev() {
        Scanner scanner = Scanner.of(SourceLines.of(SourceLine.of("abc", null)));
        Position start = scanner.position();
        scanner.next();
        Position b = scanner.position();
        assertThat(scanner.peek()).isEqualTo('b');
        scanner.next();
        Position c = scanner.position();
        scanner.next();
        Position eof = scanner.position();
        assertThat(scanner.peek()).isEqualTo('\0');
        assertThat(scanner.hasNext()).isFalse();

        scanner.prev();
        assertThat(scanner.position()).isEqualTo(c);
        assertThat(scanner.peek()).isEqualTo('c');
        scanner.prev();
        assertThat(scanner.position()).isEqualTo(b);
        assertThat(scanner.peek()).isEqualTo('b');
        scanner.prev();
        assertThat(scanner.position()).isEqualTo(start);
        assertThat(scanner.peek()).isEqualTo('a');

        // No-op at the beginning.
        scanner.prev();
        assertThat(scanner.position()).isEqualTo(start);
        assertThat(scanner.peek()).isEqualTo('a');

        scanner.next();
        scanner.prev();
        assertThat(scanner.position()).isEqualTo(start);
        assertThat(scanner.peek()).isEqualTo('a');

        scanner.setPosition(eof);
        scanner.prev();
        scanner.next();
        assertThat(scanner.position()).isEqualTo(eof);
        assertThat(scanner.peek()).isEqualTo('\0');

        Scanner empty = Scanner.of(SourceLines.empty());
        Position emptyPos = empty.position();
        empty.prev();
        assertThat(empty.position()).isEqualTo(emptyPos);
        assertThat(empty.peek()).isEqualTo('\0');
        assertThat(empty.hasNext()).isFalse();

        Scanner emptyLine = Scanner.of(SourceLines.of(SourceLine.of("", null)));
        Position emptyLinePos = emptyLine.position();
        assertThat(emptyLine.peek()).isEqualTo('\0');
        assertThat(emptyLine.hasNext()).isFalse();
        emptyLine.prev();
        assertThat(emptyLine.position()).isEqualTo(emptyLinePos);
        assertThat(emptyLine.peek()).isEqualTo('\0');
    }

    @Test
    void testPrevAcrossLines() {
        Scanner scanner =
                Scanner.of(
                        SourceLines.of(
                                List.of(SourceLine.of("ab", null), SourceLine.of("c", null))));
        Position start = scanner.position();
        scanner.next();
        assertThat(scanner.peek()).isEqualTo('b');
        scanner.next();
        Position newline = scanner.position();
        assertThat(scanner.peek()).isEqualTo('\n');
        scanner.next();
        assertThat(scanner.peek()).isEqualTo('c');
        scanner.next();
        assertThat(scanner.peek()).isEqualTo('\0');
        assertThat(scanner.hasNext()).isFalse();

        scanner.prev();
        assertThat(scanner.peek()).isEqualTo('c');
        scanner.prev();
        assertThat(scanner.position()).isEqualTo(newline);
        assertThat(scanner.peek()).isEqualTo('\n');
        scanner.prev();
        assertThat(scanner.peek()).isEqualTo('b');
        scanner.next();
        assertThat(scanner.position()).isEqualTo(newline);
        assertThat(scanner.peek()).isEqualTo('\n');
        scanner.prev();
        scanner.prev();
        assertThat(scanner.position()).isEqualTo(start);
        assertThat(scanner.peek()).isEqualTo('a');
        scanner.prev();
        assertThat(scanner.position()).isEqualTo(start);

        // Unequal lengths: cached line content/length must follow the line we step onto.
        Scanner uneven =
                Scanner.of(
                        SourceLines.of(
                                List.of(SourceLine.of("a", null), SourceLine.of("bbb", null))));
        uneven.next();
        uneven.next();
        assertThat(uneven.peek()).isEqualTo('b');
        uneven.prev();
        assertThat(uneven.peek()).isEqualTo('\n');
        uneven.prev();
        assertThat(uneven.peek()).isEqualTo('a');
        uneven.next();
        assertThat(uneven.peek()).isEqualTo('\n');
        uneven.next();
        uneven.next();
        uneven.next();
        uneven.next();
        assertThat(uneven.peek()).isEqualTo('\0');
        uneven.prev();
        assertThat(uneven.peek()).isEqualTo('b');

        // Consecutive empty lines, including an empty final line: no newline after the last line.
        Scanner blanks =
                Scanner.of(
                        SourceLines.of(
                                List.of(
                                        SourceLine.of("", null),
                                        SourceLine.of("", null),
                                        SourceLine.of("", null))));
        Position blanksStart = blanks.position();
        assertThat(blanks.peek()).isEqualTo('\n');
        blanks.next();
        assertThat(blanks.peek()).isEqualTo('\n');
        blanks.next();
        assertThat(blanks.peek()).isEqualTo('\0');
        assertThat(blanks.hasNext()).isFalse();
        blanks.prev();
        assertThat(blanks.peek()).isEqualTo('\n');
        blanks.prev();
        assertThat(blanks.position()).isEqualTo(blanksStart);
        assertThat(blanks.peek()).isEqualTo('\n');
        blanks.next();
        blanks.next();
        assertThat(blanks.peek()).isEqualTo('\0');

        // Empty final line steps back onto the preceding newline, not a made-up terminal one.
        Scanner trailingBlank =
                Scanner.of(
                        SourceLines.of(
                                List.of(SourceLine.of("a", null), SourceLine.of("", null))));
        Position trailingStart = trailingBlank.position();
        trailingBlank.next();
        Position precedingNewline = trailingBlank.position();
        assertThat(trailingBlank.peek()).isEqualTo('\n');
        trailingBlank.next();
        assertThat(trailingBlank.peek()).isEqualTo('\0');
        assertThat(trailingBlank.hasNext()).isFalse();
        trailingBlank.prev();
        assertThat(trailingBlank.position()).isEqualTo(precedingNewline);
        assertThat(trailingBlank.peek()).isEqualTo('\n');
        trailingBlank.next();
        assertThat(trailingBlank.peek()).isEqualTo('\0');
        trailingBlank.prev();
        trailingBlank.prev();
        assertThat(trailingBlank.position()).isEqualTo(trailingStart);
        assertThat(trailingBlank.peek()).isEqualTo('a');
    }

    @Test
    void testFindPrev() {
        Scanner scanner = Scanner.of(SourceLines.of(SourceLine.of("abXcdX", null)));
        Position start = scanner.position();
        assertThat(scanner.findPrev('a')).isEqualTo(0);
        assertThat(scanner.position()).isEqualTo(start);
        assertThat(scanner.findPrev(c -> c == 'a')).isEqualTo(0);
        assertThat(scanner.position()).isEqualTo(start);

        scanner.next();
        scanner.next();
        Position firstX = scanner.position();
        assertThat(scanner.peek()).isEqualTo('X');
        assertThat(scanner.findPrev('X')).isEqualTo(0);
        assertThat(scanner.position()).isEqualTo(firstX);

        scanner.next();
        scanner.next();
        // Between the two matches: the nearer (earlier) X, not one after the cursor.
        assertThat(scanner.peek()).isEqualTo('d');
        assertThat(scanner.findPrev('X')).isEqualTo(2);
        assertThat(scanner.position()).isEqualTo(firstX);

        scanner.next();
        scanner.next();
        scanner.next();
        Position secondX = scanner.position();
        assertThat(scanner.peek()).isEqualTo('X');
        scanner.next();
        assertThat(scanner.peek()).isEqualTo('\0');
        // Match immediately before EOF counts the step off the sentinel.
        assertThat(scanner.findPrev('X')).isEqualTo(1);
        assertThat(scanner.position()).isEqualTo(secondX);
        assertThat(scanner.peek()).isEqualTo('X');

        scanner.next();
        assertThat(scanner.findPrev(c -> c == 'X')).isEqualTo(1);
        assertThat(scanner.position()).isEqualTo(secondX);

        assertThat(scanner.findPrev('z')).isEqualTo(-1);
        assertThat(scanner.position()).isEqualTo(start);
        assertThat(scanner.peek()).isEqualTo('a');

        scanner.setPosition(secondX);
        scanner.next();
        assertThat(scanner.findPrev(Scanner.END)).isEqualTo(-1);
        assertThat(scanner.position()).isEqualTo(start);
        scanner.setPosition(secondX);
        scanner.next();
        assertThat(scanner.findPrev(c -> c == Scanner.END)).isEqualTo(-1);
        assertThat(scanner.position()).isEqualTo(start);

        CharMatcher any = c -> true;
        scanner.setPosition(secondX);
        assertThat(scanner.findPrev(any)).isEqualTo(0);
        assertThat(scanner.position()).isEqualTo(secondX);
        scanner.next();
        assertThat(scanner.findPrev(any)).isEqualTo(1);
        assertThat(scanner.position()).isEqualTo(secondX);
        assertThat(scanner.peek()).isEqualTo('X');

        Scanner lines =
                Scanner.of(
                        SourceLines.of(
                                List.of(SourceLine.of("ab", null), SourceLine.of("c", null))));
        lines.next();
        lines.next();
        Position newline = lines.position();
        lines.next();
        lines.next();
        assertThat(lines.peek()).isEqualTo('\0');
        assertThat(lines.findPrev('\n')).isEqualTo(2);
        assertThat(lines.position()).isEqualTo(newline);
        assertThat(lines.peek()).isEqualTo('\n');
        assertThat(lines.findPrev(c -> c == '\n')).isEqualTo(0);
        assertThat(lines.position()).isEqualTo(newline);

        Scanner empty = Scanner.of(SourceLines.empty());
        Position emptyPos = empty.position();
        assertThat(empty.findPrev('a')).isEqualTo(-1);
        assertThat(empty.findPrev(Scanner.END)).isEqualTo(-1);
        assertThat(empty.findPrev(any)).isEqualTo(-1);
        assertThat(empty.findPrev(c -> c == Scanner.END)).isEqualTo(-1);
        assertThat(empty.position()).isEqualTo(emptyPos);
        assertThat(empty.peek()).isEqualTo('\0');

        Scanner text = Scanner.of(SourceLines.of(SourceLine.of("a", null)));
        text.next();
        assertThat(text.findPrev(any)).isEqualTo(1);
        assertThat(text.peek()).isEqualTo('a');
    }

    @Test
    void testFindPrevBounds() {
        Scanner scanner = Scanner.of(SourceLines.of(SourceLine.of("aXbYc", null)));
        Position start = scanner.position();
        scanner.next();
        Position x = scanner.position();
        scanner.next();
        scanner.next();
        Position y = scanner.position();
        scanner.next();
        scanner.next();
        Position eof = scanner.position();
        assertThat(scanner.peek()).isEqualTo('\0');

        // Inclusive bound on the same line, including a match sitting on the bound.
        assertThat(scanner.findPrev('Y', x)).isEqualTo(2);
        assertThat(scanner.position()).isEqualTo(y);
        scanner.setPosition(eof);
        assertThat(scanner.findPrev('Y', y)).isEqualTo(2);
        assertThat(scanner.position()).isEqualTo(y);
        assertThat(scanner.peek()).isEqualTo('Y');

        // A match before the bound is excluded; a miss stays on the bound.
        scanner.setPosition(eof);
        assertThat(scanner.findPrev('X', y)).isEqualTo(-1);
        assertThat(scanner.position()).isEqualTo(y);
        assertThat(scanner.peek()).isEqualTo('Y');
        scanner.setPosition(eof);
        assertThat(scanner.findPrev('z', y)).isEqualTo(-1);
        assertThat(scanner.position()).isEqualTo(y);

        // Start and bound are the same position: only that character is checked.
        assertThat(scanner.findPrev('Y', y)).isEqualTo(0);
        assertThat(scanner.position()).isEqualTo(y);
        assertThat(scanner.findPrev('X', y)).isEqualTo(-1);
        assertThat(scanner.position()).isEqualTo(y);
        scanner.setPosition(eof);
        assertThat(scanner.findPrev('c', eof)).isEqualTo(-1);
        assertThat(scanner.position()).isEqualTo(eof);
        assertThat(scanner.peek()).isEqualTo('\0');

        scanner.setPosition(eof);
        assertThat(scanner.findPrev(c -> c == 'Y', x)).isEqualTo(2);
        assertThat(scanner.position()).isEqualTo(y);
        scanner.setPosition(eof);
        assertThat(scanner.findPrev(c -> c == 'X', y)).isEqualTo(-1);
        assertThat(scanner.position()).isEqualTo(y);
        assertThat(scanner.findPrev(c -> c == 'Y', y)).isEqualTo(0);

        // Across lines the bound is still inclusive and stops the search.
        Scanner lines =
                Scanner.of(
                        SourceLines.of(
                                List.of(SourceLine.of("aXb", null), SourceLine.of("cYd", null))));
        Position lineStart = lines.position();
        lines.next();
        Position lineX = lines.position();
        lines.next();
        lines.next();
        Position newline = lines.position();
        assertThat(lines.peek()).isEqualTo('\n');
        lines.next();
        Position secondStart = lines.position();
        lines.next();
        Position lineY = lines.position();
        lines.next();
        lines.next();
        Position linesEof = lines.position();

        assertThat(lines.findPrev('Y', newline)).isEqualTo(2);
        assertThat(lines.position()).isEqualTo(lineY);
        lines.setPosition(linesEof);
        assertThat(lines.findPrev('\n', lineStart)).isEqualTo(4);
        assertThat(lines.position()).isEqualTo(newline);
        assertThat(lines.peek()).isEqualTo('\n');

        lines.setPosition(linesEof);
        assertThat(lines.findPrev('X', newline)).isEqualTo(-1);
        assertThat(lines.position()).isEqualTo(newline);
        assertThat(lines.peek()).isEqualTo('\n');
        lines.setPosition(linesEof);
        assertThat(lines.findPrev('a', secondStart)).isEqualTo(-1);
        assertThat(lines.position()).isEqualTo(secondStart);

        lines.setPosition(linesEof);
        assertThat(lines.findPrev('a', lineStart)).isEqualTo(7);
        assertThat(lines.position()).isEqualTo(lineStart);
        lines.setPosition(linesEof);
        assertThat(lines.findPrev(c -> c == '\n', newline)).isEqualTo(4);
        assertThat(lines.position()).isEqualTo(newline);
        lines.setPosition(linesEof);
        assertThat(lines.findPrev(c -> c == 'X', secondStart)).isEqualTo(-1);
        assertThat(lines.position()).isEqualTo(secondStart);

        // Future and out-of-range bounds throw before the cursor moves.
        lines.setPosition(lineX);
        assertThat(lines.peek()).isEqualTo('X');
        assertThatThrownBy(() -> lines.findPrev('a', lineY))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> lines.findPrev(c -> true, linesEof))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> lines.findPrev('a', new Position(-1, 0)))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> lines.findPrev('a', new Position(0, -1)))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> lines.findPrev('a', new Position(0, 4)))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> lines.findPrev('a', new Position(2, 0)))
                .isInstanceOf(IllegalArgumentException.class);
        assertThat(lines.position()).isEqualTo(lineX);
        assertThat(lines.peek()).isEqualTo('X');

        Scanner empty = Scanner.of(SourceLines.empty());
        Position origin = empty.position();
        assertThat(empty.findPrev('x', origin)).isEqualTo(-1);
        assertThat(empty.findPrev(c -> true, origin)).isEqualTo(-1);
        assertThat(empty.findPrev(Scanner.END, origin)).isEqualTo(-1);
        assertThat(empty.position()).isEqualTo(origin);
        assertThatThrownBy(() -> empty.findPrev('x', new Position(0, 1)))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> empty.findPrev(c -> true, new Position(1, 0)))
                .isInstanceOf(IllegalArgumentException.class);
        assertThat(empty.position()).isEqualTo(origin);
        assertThat(empty.peek()).isEqualTo('\0');

        // Same-line bound equal to the start still accepts the character there.
        scanner.setPosition(start);
        assertThat(scanner.findPrev('a', start)).isEqualTo(0);
        assertThat(scanner.position()).isEqualTo(start);
    }

    @Test
    void testCodeUnitMovement() {
        Scanner scanner = Scanner.of(SourceLines.of(SourceLine.of("A\uD83D\uDE0AZ", null)));
        assertThat(scanner.peek()).isEqualTo('A');
        assertThat(scanner.peekCodePoint()).isEqualTo('A');
        assertThat(scanner.peekPreviousCodePoint()).isEqualTo('\0');

        scanner.next();
        assertThat(scanner.peek()).isEqualTo('\uD83D');
        assertThat(scanner.peekCodePoint()).isEqualTo(128522);
        assertThat(scanner.peekPreviousCodePoint()).isEqualTo('A');

        scanner.next();
        assertThat(scanner.peek()).isEqualTo('\uDE0A');
        assertThat(scanner.peekCodePoint()).isEqualTo('\uDE0A');
        assertThat(scanner.peekPreviousCodePoint()).isEqualTo('\uD83D');

        scanner.next();
        assertThat(scanner.peek()).isEqualTo('Z');
        // Previous code point reassembles the surrogate pair behind this code unit.
        assertThat(scanner.peekPreviousCodePoint()).isEqualTo(128522);
        scanner.next();
        assertThat(scanner.hasNext()).isFalse();
        assertThat(scanner.peek()).isEqualTo('\0');
        assertThat(scanner.peekCodePoint()).isEqualTo('\0');
        assertThat(scanner.peekPreviousCodePoint()).isEqualTo('Z');

        scanner.prev();
        assertThat(scanner.peek()).isEqualTo('Z');
        scanner.prev();
        assertThat(scanner.peek()).isEqualTo('\uDE0A');
        assertThat(scanner.peekCodePoint()).isEqualTo('\uDE0A');
        assertThat(scanner.peekPreviousCodePoint()).isEqualTo('\uD83D');
        scanner.prev();
        assertThat(scanner.peek()).isEqualTo('\uD83D');
        assertThat(scanner.peekCodePoint()).isEqualTo(128522);
        assertThat(scanner.peekPreviousCodePoint()).isEqualTo('A');

        scanner.next();
        assertThat(scanner.peek()).isEqualTo('\uDE0A');
        scanner.prev();
        assertThat(scanner.peek()).isEqualTo('\uD83D');
        assertThat(scanner.peekCodePoint()).isEqualTo(128522);

        while (scanner.hasNext()) {
            scanner.next();
        }
        assertThat(scanner.findPrev('\uDE0A')).isEqualTo(2);
        assertThat(scanner.peek()).isEqualTo('\uDE0A');
        assertThat(scanner.peekCodePoint()).isEqualTo('\uDE0A');
        while (scanner.hasNext()) {
            scanner.next();
        }
        assertThat(scanner.findPrev('\uD83D')).isEqualTo(3);
        assertThat(scanner.peek()).isEqualTo('\uD83D');
        assertThat(scanner.peekCodePoint()).isEqualTo(128522);
        assertThat(scanner.peekPreviousCodePoint()).isEqualTo('A');
    }

    @Test
    void testFindPrevSource() {
        Scanner scanner =
                new Scanner(
                        List.of(
                                SourceLine.of("ab", SourceSpan.of(10, 3, 13, 2)),
                                SourceLine.of("cde", SourceSpan.of(11, 4, 20, 3))),
                        0,
                        0);
        while (scanner.hasNext()) {
            scanner.next();
        }
        Position end = scanner.position();
        assertThat(scanner.peek()).isEqualTo('\0');
        assertThat(scanner.findPrev('b')).isEqualTo(5);
        assertThat(scanner.peek()).isEqualTo('b');
        assertSourceLines(
                scanner.getSource(scanner.position(), end),
                "b\ncde",
                SourceSpan.of(10, 4, 14, 1),
                SourceSpan.of(11, 4, 20, 3));
    }

    @Test
    void nextString() {
        Scanner scanner =
                Scanner.of(
                        SourceLines.of(
                                List.of(SourceLine.of("hey ya", null), SourceLine.of("hi", null))));
        assertThat(scanner.next("hoy")).isFalse();
        assertThat(scanner.next("hey")).isTrue();
        assertThat(scanner.next(' ')).isTrue();
        assertThat(scanner.next("yo")).isFalse();
        assertThat(scanner.next("ya")).isTrue();
        assertThat(scanner.next(" ")).isFalse();
    }
}
