package org.commonmark.experimental.extractor;

import org.commonmark.experimental.identifier.RepeatableSymbolContainerPattern;

public class RepeatableSymbolContainerExtractor {
    private RepeatableSymbolContainerExtractor() {
    }

    public static String from(String text, RepeatableSymbolContainerPattern repeatableSymbolContainerPattern) {
        if (text == null || text.length() < repeatableSymbolContainerPattern.getSize() * 2) {
            return "";
        }

        return text.substring(repeatableSymbolContainerPattern.getSize(),
                text.length() - repeatableSymbolContainerPattern.getSize());
    }
}
