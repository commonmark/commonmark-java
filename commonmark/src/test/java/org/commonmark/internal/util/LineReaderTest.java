package org.commonmark.internal.util;

import org.junit.jupiter.api.Test;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Objects;

import static java.util.stream.Collectors.joining;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.commonmark.internal.util.LineReader.CHAR_BUFFER_SIZE;

class LineReaderTest {

    @Test
    void testReadLine() throws IOException {
        assertLines();

        assertLines("", "\n");
        assertLines("foo", "\n", "bar", "\n");
        assertLines("foo", "\n", "bar", null);
        assertLines("", "\n", "", "\n");
        assertLines(repeat("a", CHAR_BUFFER_SIZE - 1), "\n");
        assertLines(repeat("a", CHAR_BUFFER_SIZE), "\n");
        assertLines(repeat("a", CHAR_BUFFER_SIZE) + "b", "\n");

        assertLines("", "\r\n");
        assertLines("foo", "\r\n", "bar", "\r\n");
        assertLines("foo", "\r\n", "bar", null);
        assertLines("", "\r\n", "", "\r\n");
        assertLines(repeat("a", CHAR_BUFFER_SIZE - 2), "\r\n");
        assertLines(repeat("a", CHAR_BUFFER_SIZE - 1), "\r\n");
        assertLines(repeat("a", CHAR_BUFFER_SIZE), "\r\n");
        assertLines(repeat("a", CHAR_BUFFER_SIZE) + "b", "\r\n");

        assertLines("", "\r");
        assertLines("foo", "\r", "bar", "\r");
        assertLines("foo", "\r", "bar", null);
        assertLines("", "\r", "", "\r");
        assertLines(repeat("a", CHAR_BUFFER_SIZE - 1), "\r");
        assertLines(repeat("a", CHAR_BUFFER_SIZE), "\r");
        assertLines(repeat("a", CHAR_BUFFER_SIZE) + "b", "\r");

        assertLines("", "\n", "", "\r", "", "\r\n", "", "\n");
        assertLines("what", "\r", "are", "\r", "", "\r", "you", "\r\n", "", "\r\n", "even", "\n", "doing", null);
    }

    @Test
    void testClose() throws IOException {
        var reader = new InputStreamReader(new ByteArrayInputStream("test".getBytes(StandardCharsets.UTF_8)));
        var lineReader = new LineReader(reader);
        lineReader.close();
        lineReader.close();
        assertThatThrownBy(reader::read).isInstanceOf(IOException.class);
    }

    private void assertLines(String... s) throws IOException {
        assertThat(s.length).as("Expected parts needs to be even (pairs of content and terminator)").isEven();
        var input = Arrays.stream(s).filter(Objects::nonNull).collect(joining(""));

        assertLines(new StringReader(input), s);
        assertLines(new SlowStringReader(input), s);
    }

    private static void assertLines(Reader reader, String... expectedParts) throws IOException {
        try (var lineReader = new LineReader(reader)) {
            var lines = new ArrayList<>();
            String line;
            while ((line = lineReader.readLine()) != null) {
                lines.add(line);
                lines.add(lineReader.getLineTerminator());
            }
            assertThat(lineReader.getLineTerminator()).isNull();
            assertThat(lines).containsExactly(expectedParts);
        }
    }

    private static String repeat(String s, int count) {
        StringBuilder sb = new StringBuilder(s.length() * count);
        for (int i = 0; i < count; i++) {
            sb.append(s);
        }
        return sb.toString();
    }

    /**
     * Reader that only reads 0 or 1 chars at a time to test the corner cases.
     */
    private static class SlowStringReader extends Reader {

        private final String s;
        private int position = 0;
        private boolean empty = false;

        private SlowStringReader(String s) {
            this.s = s;
        }

        @Override
        public int read(char[] cbuf, int off, int len) throws IOException {
            Objects.checkFromIndexSize(off, len, cbuf.length);
            if (len == 0) {
                return 0;
            }
            empty = !empty;
            if (empty) {
                // Return 0 every other time to test handling of 0.
                return 0;
            }
            if (position >= s.length()) {
                return -1;
            }
            cbuf[off] = s.charAt(position++);
            return 1;
        }

        @Override
        public void close() throws IOException {
        }
    }
}
