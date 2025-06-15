package org.commonmark.text;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

public class CharactersTest {

    @Test
    public void isPunctuation() {
        // From https://spec.commonmark.org/0.29/#ascii-punctuation-character
        char[] chars = {
                '!', '"', '#', '$', '%', '&', '\'', '(', ')', '*', '+', ',', '-', '.', '/', // (U+0021–2F)
                ':', ';', '<', '=', '>', '?', '@', // (U+003A–0040)
                '[', '\\', ']', '^', '_', '`', // (U+005B–0060)
                '{', '|', '}', '~' // (U+007B–007E)
        };

        for (char c : chars) {
            assertThat(Characters.isPunctuationCodePoint(c)).as("Expected to be punctuation: " + c).isTrue();
        }
    }

    @Test
    public void isBlank() {
        assertThat(Characters.isBlank("")).isTrue();
        assertThat(Characters.isBlank(" ")).isTrue();
        assertThat(Characters.isBlank("\t")).isTrue();
        assertThat(Characters.isBlank(" \t")).isTrue();
        assertThat(Characters.isBlank("a")).isFalse();
        assertThat(Characters.isBlank("\f")).isFalse();
    }
}
