package org.commonmark.experimental.extractor;

public class SingleSymbolContainerExtractor {
    private SingleSymbolContainerExtractor() {
    }

    public static String from(String text) {
        if (text == null || text.length() < 2) {
            return "";
        }
        return text.substring(1, text.length() - 1);
    }
}
