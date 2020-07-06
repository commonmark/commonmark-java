package org.commonmark.experimental.identifier;

import org.commonmark.experimental.NodePatternIdentifier;
import org.commonmark.experimental.TextIdentifier;

public class TextIdentifierBaseTest {
    public static void readLine(String text, TextIdentifier textIdentifier, NodePatternIdentifier nodePatternIdentifier) {
        int index = 0;

        while (index < text.length()) {
            char character = text.charAt(index);

            textIdentifier.checkByCharacter(text, character, index, nodePatternIdentifier);

            index++;
        }
    }
}
