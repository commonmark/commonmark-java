package org.commonmark.text;

import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

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
            assertTrue("Expected to be punctuation: " + c, Characters.isPunctuationCodePoint(c));
        }
    }

    @Test
    public void isBlank() {
        assertTrue(Characters.isBlank(""));
        assertTrue(Characters.isBlank(" "));
        assertTrue(Characters.isBlank("\t"));
        assertTrue(Characters.isBlank(" \t"));
        assertFalse(Characters.isBlank("a"));
        assertFalse(Characters.isBlank("\f"));
    }
}
