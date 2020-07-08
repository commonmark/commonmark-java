package org.commonmark.experimental;

public interface TextNodeIdentifierSetup extends NodeSetup {
    TextIdentifier textIdentifier();

    int priority();

    class DefaultPriority {
        public static int DEFAULT = 0;
        public static int REPEATABLE_SYMBOL = 10;
        public static int BRACKET_SYMBOL = 20;
    }
}
